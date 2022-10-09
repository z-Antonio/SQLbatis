package com.antonio.android.sqlbatis.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Database(val name: String)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class ColumnName(
    val primaryKey: Boolean = false,
    val autoIncr: Boolean = false,
    val nonNull: Boolean = false,
    val dataType: DataType = DataType.text,
    val defaultValue: String = ""
)