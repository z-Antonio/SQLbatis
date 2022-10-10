package com.sqlbatis.android.util

import android.content.ContentValues
import android.util.Log
import com.sqlbatis.android.BuildConfig
import java.util.*
import java.util.regex.Pattern

object Utils

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