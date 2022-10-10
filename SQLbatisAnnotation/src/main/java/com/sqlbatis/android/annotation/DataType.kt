package com.sqlbatis.android.annotation

enum class DataType {
    integer {
        override fun defaultValue(): String = "0"
    },
    real {
        override fun defaultValue(): String = "0.0"
    },
    text {
        override fun defaultValue(): String = "\"\""
    },
    blob {
        override fun defaultValue(): String = "0"
    };

    abstract fun defaultValue(): String
}