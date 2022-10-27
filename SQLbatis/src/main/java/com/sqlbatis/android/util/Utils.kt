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
    this@transfer.keySet().forEach { item ->
        val key = item.humpToUnderline()
        this@transfer[item].isNull {
            this.putNull(key)
        }.isInt {
            this.put(key, it)
        }.isLong {
            this.put(key, it)
        }.isFloat {
            this.put(key, it)
        }.isDouble {
            this.put(key, it)
        }.isByte {
            this.put(key, it)
        }.isChar {
            this.put(key, it.code)
        }.isShort {
            this.put(key, it)
        }.isBoolean {
            this.put(key, it)
        }.isString {
            this.put(key, it)
        }.isTypeOf<ByteArray> {
            this.put(key, it)
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

fun Cursor.getValue(type: Class<*>, index: Int): Any? {
    if (type == ByteArray::class.java) {
        return getBlob(index)
    }
    when (type.toClassType()) {
        ClassType.STRING -> return getString(index)
        ClassType.FLOAT -> return getFloat(index)
        ClassType.DOUBLE -> return getDouble(index)
        ClassType.INT -> return getInt(index)
        ClassType.LONG -> return getLong(index)
        ClassType.BYTE -> return getInt(index).toByte()
        ClassType.CHAR -> return getInt(index).toChar()
        ClassType.SHORT -> return getShort(index)
        ClassType.BOOLEAN -> return getInt(index) == 1
    }
    return null
}

fun Any.toContentValues(): ContentValues {
    val cv = ContentValues()
    this@toContentValues::class.java.declaredFields.forEach { field ->
        field.isAccessible = true
        field.get(this@toContentValues)?.let { value ->
            val key = field.name.humpToUnderline()
            value.classType().isString {
                cv.put(key, it)
            }.isFloat {
                cv.put(key, it)
            }.isDouble {
                cv.put(key, it)
            }.isInt {
                cv.put(key, it)
            }.isLong {
                cv.put(key, it)
            }.isShort {
                cv.put(key, it)
            }.isBoolean {
                cv.put(key, it)
            }.isByte {
                cv.put(key, it)
            }.isTypeOf<ByteArray> {
                cv.put(key, it)
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