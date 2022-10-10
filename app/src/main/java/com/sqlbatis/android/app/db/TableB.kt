package com.sqlbatis.android.app.db

import com.sqlbatis.android.annotation.ColumnName
import com.sqlbatis.android.annotation.DataType
import com.sqlbatis.android.annotation.Database

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