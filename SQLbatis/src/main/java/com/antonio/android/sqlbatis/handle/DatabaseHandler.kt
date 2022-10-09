package com.antonio.android.sqlbatis.handle

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.antonio.android.sqlbatis.SQLbatisHelper
import com.antonio.android.sqlbatis.util.humpToUnderline
import com.antonio.android.sqlbatis.util.pull

class DatabaseHandler(private val context: Context) {
    private val map = mutableMapOf<String, SQLbatisHelper>()

    fun register(dbName: String, tableInfo: TableInfo) {
        map.pull(dbName.humpToUnderline()) {
            SQLbatisHelper(context, it)
        }.addTableInfo(tableInfo)
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