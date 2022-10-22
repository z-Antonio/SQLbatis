package com.sqlbatis.android.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.sqlbatis.android.BuildConfig
import com.sqlbatis.android.SQLbatis
import com.sqlbatis.android.annotation.ColumnName
import com.sqlbatis.android.annotation.Database
import java.util.*
import java.util.regex.Pattern

enum class L {
    V, D, I, W, E
}

fun Any.printSQL(msg: String, l: L = L.I) {
    if (!BuildConfig.DEBUG) return
    val tag = this::class.java.simpleName
    when (l) {
        L.V -> Log.v(tag, msg)
        L.D -> Log.d(tag, msg)
        L.W -> Log.w(tag, msg)
        L.E -> Log.e(tag, msg)
        else -> Log.i(tag, msg)
    }
}

fun <K, V> MutableMap<K, V>.pull(key: K, create: (key: K) -> V): V =
    this[key] ?: create(key).apply { this@pull[key] = this }

inline fun <reified T> inject() = lazy {
    val clz = Class.forName("com.sqlbatis.android.impl.${T::class.java.simpleName}Impl")
    return@lazy clz.newInstance() as T
}

fun ContentValues.transfer(): ContentValues = ContentValues(this.size()).apply {
    this@transfer.keySet().forEach {
        val value = this@transfer[it]
        val key = it.humpToUnderline()
        when (value) {
            null -> this.putNull(key)
            is String -> this.put(key, value)
            is Byte -> this.put(key, value)
            is Short -> this.put(key, value)
            is Int -> this.put(key, value)
            is Long -> this.put(key, value)
            is Float -> this.put(key, value)
            is Double -> this.put(key, value)
            is Boolean -> this.put(key, value)
            is ByteArray -> this.put(key, value)
        }
    }
}

val LOCALE: Locale = Locale.getDefault()

/**
 * 将驼峰转为下划线
 */
fun String.humpToUnderline(transferFirst: Boolean = false): String {
    val compile = Pattern.compile("[A-Z]")
    val matcher = compile.matcher(this)
    val sb = StringBuffer()
    while (matcher.find()) {
        val str = matcher.group(0).lowercase(LOCALE)
        if (!transferFirst && matcher.start() == 0) {
            matcher.appendReplacement(sb, str)
        } else {
            matcher.appendReplacement(sb, "_$str")
        }
    }
    matcher.appendTail(sb)
    return sb.toString()
}

/**
 * 将下划线转为驼峰
 */
fun String.underlineToHump(upperFirst: Boolean = false): String {
    var str = this.lowercase(LOCALE)
    val compile = Pattern.compile("_[a-z]")
    val matcher = compile.matcher(str)
    val sb = StringBuffer()
    while (matcher.find()) {
        matcher.appendReplacement(
            sb,
            matcher.group(0).uppercase(LOCALE).replace("_", "")
        )
    }
    matcher.appendTail(sb)
    if (upperFirst) {
        val c = sb[0]
        if (c in 'a'..'z') {
            sb.setCharAt(0, c - 32)
        }
    }
    return sb.toString()
}

fun <T> findDatabase(
    context: Context,
    clazz: Class<*>,
    action: (db: SQLiteDatabase, table: String) -> T
): T {
    clazz.getAnnotation(Database::class.java)?.let { annotation ->
        SQLbatis.getDatabase(context, annotation.name)?.let { db ->
            return action(db, clazz.simpleName.humpToUnderline())
        }
    }
    throw RuntimeException("${clazz.canonicalName} must use @Database annotation")
}

fun Class<*>.transferUri(authorities: String): Uri {
    this.getAnnotation(Database::class.java)?.let { annotation ->
        return Uri.parse("content://$authorities/${annotation.name}/$simpleName").apply {
            printSQL("transferUri ===> $this")
        }
    }
    throw RuntimeException("$canonicalName must use @Database annotation")
}

fun <T> Cursor.convert(clazz: Class<T>): List<T> {
    if (clazz.getAnnotation(Database::class.java) == null) {
        throw RuntimeException("$clazz must use @Database annotation")
    }
    return mutableListOf<T>().apply {
        if (moveToFirst()) {
            do {
                clazz.newInstance()?.let {
                    if (it.fillCursor(this@convert)) {
                        add(it)
                    }
                }
            } while (moveToNext())
        }
    }
}

fun Any.fillCursor(cursor: Cursor): Boolean {
    this::class.java.declaredFields.forEach { field ->
        val index = cursor.getColumnIndex(field.name.humpToUnderline())
        if (index != -1) {
            try {
                field.isAccessible = true
                field.set(this, cursor.getValue(field.type, index))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return true
}

fun Cursor.getValue(type: Class<*>, index: Int): Any? =
    when (type) {
        String::class.java -> getString(index)
        Float::class.java -> getFloat(index)
        Double::class.java -> getDouble(index)
        Integer::class.java -> getInt(index)
        Int::class.java -> getInt(index)
        Long::class.java -> getLong(index)
        Short::class.java -> getShort(index)
        ByteArray::class.java -> getBlob(index)
        else -> null
    }

fun Any.toContentValues(): ContentValues {
    val cv = ContentValues()
    this@toContentValues::class.java.declaredFields.forEach { field ->
        field.isAccessible = true
        field.get(this@toContentValues)?.let { value ->
            val key = field.name.humpToUnderline()
            when (value) {
                is String -> cv.put(key, value)
                is Float -> cv.put(key, value)
                is Double -> cv.put(key, value)
                is Int -> cv.put(key, value)
                is Long -> cv.put(key, value)
                is Short -> cv.put(key, value)
                is ByteArray -> cv.put(key, value)
            }
        }
    }
    return cv
}

fun Any.findPrimaryKey(): Pair<String, Any?>? {
    return this::class.java.declaredFields.find { it.getAnnotation(ColumnName::class.java)?.primaryKey == true }
        ?.let { field ->
            field.isAccessible = true
            Pair(field.name.humpToUnderline(), field.get(this))
        }
}