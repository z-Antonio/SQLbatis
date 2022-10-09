package com.antonio.android.sqlbatis.app.db

import com.antonio.android.sqlbatis.annotation.ColumnName
import com.antonio.android.sqlbatis.annotation.DataType
import com.antonio.android.sqlbatis.annotation.Database

@Database("TestDb1")
class TableA1 {

    @ColumnName(primaryKey = true, dataType = DataType.integer)
    var idA: Int = 0

    @ColumnName()
    var textValueA: String? = null
}

@Database("TestDb1")
class TableA2 {

    @ColumnName(primaryKey = true, autoIncr = true, dataType = DataType.integer)
    var idA: Int = 0

    @ColumnName(dataType = DataType.integer)
    var intNull: Int = 0

    @ColumnName(dataType = DataType.real)
    var realNull: Float = 0F

    @ColumnName(dataType = DataType.text)
    var textNull: String = ""

    @ColumnName(dataType = DataType.blob)
    var blobNull: Array<Byte> = arrayOf(0)

    @ColumnName(nonNull = true, dataType = DataType.integer)
    var intNoNull: Int = 0

    @ColumnName(nonNull = true, dataType = DataType.real)
    var realNoNull: Float = 0F

    @ColumnName(nonNull = true, dataType = DataType.text)
    var textNoNull: String = ""

    @ColumnName(nonNull = true, dataType = DataType.blob)
    var blobNoNull: ByteArray = byteArrayOf(0)

    @ColumnName(nonNull = true, dataType = DataType.integer, defaultValue = "1")
    var intNoNullDef: Int = 0

    @ColumnName(nonNull = true, dataType = DataType.real, defaultValue = "1.0")
    var realNoNullDef: Float = 0F

    @ColumnName(nonNull = true, dataType = DataType.text, defaultValue = "\"-\"")
    var textNoNullDef: String = ""

    @ColumnName(nonNull = true, dataType = DataType.blob, defaultValue = "1")
    var blobNoNullDef: Array<Byte> = arrayOf(0)

}

@Database("TestDb1")
class TableA3 {
    @ColumnName(primaryKey = true, dataType = DataType.integer)
    var idA: Int = 0

    @ColumnName(nonNull = true, dataType = DataType.integer)
    var intNoNull: Int = 0

    @ColumnName(nonNull = true, dataType = DataType.real)
    var realNoNull: Float = 0F

    @ColumnName(nonNull = true, dataType = DataType.text)
    var textNoNull: String = ""

    @ColumnName(nonNull = true, dataType = DataType.blob)
    var blobNoNull: ByteArray = byteArrayOf(0)
}