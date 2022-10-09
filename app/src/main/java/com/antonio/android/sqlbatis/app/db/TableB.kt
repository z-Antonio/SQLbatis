package com.antonio.android.sqlbatis.app.db

import com.antonio.android.sqlbatis.annotation.ColumnName
import com.antonio.android.sqlbatis.annotation.DataType
import com.antonio.android.sqlbatis.annotation.Database

@Database("TestDb2")
class TableB1 {

    @ColumnName(primaryKey = true, dataType = DataType.integer)
    var idB: Int = 0

    @ColumnName()
    var textValueB: String? = null
}

@Database("TestDb2")
class TableB2 {

    @ColumnName(primaryKey = true, autoIncr = true, dataType = DataType.integer)
    var idB: Int = 0

    @ColumnName(nonNull = true, dataType = DataType.real)
    var priceValueB: Float = 0F

    @ColumnName(nonNull = true)
    var msgValueB: String = "t"
}