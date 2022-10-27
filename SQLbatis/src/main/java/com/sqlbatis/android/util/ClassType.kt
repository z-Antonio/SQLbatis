package com.sqlbatis.android.util

enum class ClassType {
    BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, STRING, NULL, OTHER
}

data class Value<T>(var value: T)

class Type private constructor(private val any: Any?) {

    companion object {
        fun check(any: Any?): Type = Type(any)
    }

    private var value: Value<*> = Value("null")

    val type: ClassType get() = any.checkType()

    fun <T> getValue(): T = value.value as T

    private fun Any?.checkType(): ClassType {
        if (this == null) {
            return ClassType.NULL
        }
        when (this) {
            is Byte -> {
                value = Value(this)
                return ClassType.BYTE
            }
            is java.lang.Byte -> {
                value = Value(this.toByte())
                return ClassType.BYTE
            }
            is Char -> {
                value = Value(this)
                return ClassType.CHAR
            }
            is Character -> {
                value = Value(this.charValue())
                return ClassType.CHAR
            }
            is Short -> {
                value = Value(this)
                return ClassType.SHORT
            }
            is java.lang.Short -> {
                value = Value(this.toShort())
                return ClassType.SHORT
            }
            is Int -> {
                value = Value(this)
                return ClassType.INT
            }
            is Integer -> {
                value = Value(this.toInt())
                return ClassType.INT
            }
            is Long -> {
                value = Value(this)
                return ClassType.LONG
            }
            is java.lang.Long -> {
                value = Value(this.toLong())
                return ClassType.LONG
            }
            is Float -> {
                value = Value(this)
                return ClassType.FLOAT
            }
            is java.lang.Float -> {
                value = Value(this.toFloat())
                return ClassType.FLOAT
            }
            is Double -> {
                value = Value(this)
                return ClassType.DOUBLE
            }
            is java.lang.Double -> {
                value = Value(this.toDouble())
                return ClassType.DOUBLE
            }
            is Boolean -> {
                value = Value(this)
                return ClassType.BOOLEAN
            }
            is java.lang.Boolean -> {
                value = Value(this.booleanValue())
                return ClassType.BOOLEAN
            }
            is String -> {
                value = Value(this)
                return ClassType.STRING
            }
            is java.lang.String -> {
                value = Value(this.toString())
                return ClassType.STRING
            }
        }
        value = Value(this)
        return ClassType.OTHER
    }
}

fun Any?.classType(): Type {
    return Type.check(this)
}

fun Any?.isNull(action: () -> Unit): Type {
    if (this == null) {
        action()
    }
    return Type.check(this)
}

inline fun <reified T> Any?.typeOf(action: (T) -> Unit): Type {
    if (this is T) {
        action(this)
    }
    return Type.check(this)
}

fun Type.isByte(action: (Byte) -> Unit): Type {
    if (type == ClassType.BYTE) action(getValue())
    return this
}

fun Type.isChar(action: (Char) -> Unit): Type {
    if (type == ClassType.CHAR) action(getValue())
    return this
}

fun Type.isShort(action: (Short) -> Unit): Type {
    if (type == ClassType.SHORT) action(getValue())
    return this
}

fun Type.isInt(action: (Int) -> Unit): Type {
    if (type == ClassType.INT) action(getValue())
    return this
}

fun Type.isLong(action: (Long) -> Unit): Type {
    if (type == ClassType.LONG) action(getValue())
    return this
}

fun Type.isFloat(action: (Float) -> Unit): Type {
    if (type == ClassType.FLOAT) action(getValue())
    return this
}

fun Type.isDouble(action: (Double) -> Unit): Type {
    if (type == ClassType.DOUBLE) action(getValue())
    return this
}

fun Type.isBoolean(action: (Boolean) -> Unit): Type {
    if (type == ClassType.BOOLEAN) action(getValue())
    return this
}

fun Type.isString(action: (String) -> Unit): Type {
    if (type == ClassType.STRING) action(getValue())
    return this
}

inline fun <reified T> Type.isTypeOf(action: (T) -> Unit): Type {
    runCatching { action(getValue()) }
    return this
}

fun Class<*>.toClassType(): ClassType {
    return when (this) {
        String::class.java -> ClassType.STRING
        java.lang.String::class.java -> ClassType.STRING
        Float::class.java -> ClassType.FLOAT
        java.lang.Float::class.java -> ClassType.FLOAT
        Double::class.java -> ClassType.DOUBLE
        java.lang.Double::class.java -> ClassType.DOUBLE
        Int::class.java -> ClassType.INT
        Integer::class.java -> ClassType.INT
        Long::class.java -> ClassType.LONG
        java.lang.Long::class.java -> ClassType.LONG
        Byte::class.java -> ClassType.BYTE
        java.lang.Byte::class.java -> ClassType.BYTE
        Char::class.java -> ClassType.CHAR
        Character::class.java -> ClassType.CHAR
        Short::class.java -> ClassType.SHORT
        java.lang.Short::class.java -> ClassType.SHORT
        Boolean::class.java -> ClassType.BOOLEAN
        java.lang.Boolean::class.java -> ClassType.BOOLEAN
        else -> ClassType.OTHER
    }
}
