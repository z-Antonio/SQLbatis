package com.sqlbatis.android.handle

import android.database.sqlite.SQLiteDatabase
import com.sqlbatis.android.SQLbatisCreator
import com.sqlbatis.android.SQLbatisHelper
import com.sqlbatis.android.util.humpToUnderline
import com.sqlbatis.android.util.pull

class DatabaseHandler(private val creator: SQLbatisCreator) {
    private val map = mutableMapOf<String, SQLbatisHelper>()

    fun register(dbName: String, tableInfo: TableInfo) {
        map.pull(dbName.humpToUnderline(), creator).addTableInfo(tableInfo)
    }

    fun getDatabase(dbName: String): SQLiteDatabase? = map[dbName]?.getDatabase()

    override fun toString(): String {
        val sb = StringBuilder("database handler --->\n")
        map.values.forEach { table ->
            sb.append(table.toString())
        }
        return sb.toString()
    }
}