package com.antonio.android.sqlbatis.util

import android.util.Log
import com.antonio.android.sqlbatis.BuildConfig
import java.util.*
import java.util.regex.Pattern

object Utils

enum class L {
    V, D, I, W, E
}

fun Any.printLog(msg: String, l: L = L.I) {
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
    val clz = Class.forName("com.antonio.android.sqlbatis.impl.${T::class.java.simpleName}Impl")
    return@lazy clz.newInstance() as T
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