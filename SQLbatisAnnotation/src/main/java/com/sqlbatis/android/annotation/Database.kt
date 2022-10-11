package com.sqlbatis.android.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Database(val name: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ColumnName(
    val primaryKey: Boolean = false,
    val autoIncr: Boolean = false,
    val nonNull: Boolean = false,
    val dataType: DataType = DataType.text,
    val defaultValue: String = ""
)