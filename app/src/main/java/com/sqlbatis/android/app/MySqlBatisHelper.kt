package com.sqlbatis.android.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.sqlbatis.android.SQLbatisHelper
import com.sqlbatis.android.handle.TableInfo

class MySqlBatisHelper(context: Context, name: String? = null): SQLbatisHelper(context, name) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
    }

    override fun onCreate(db: SQLiteDatabase, tableInfos: List<TableInfo>) {
        super.onCreate(db, tableInfos)
    }

    override fun onUpgrade(db: SQLiteDatabase, upgradeTableInfos: List<TableInfo>) {
        super.onUpgrade(db, upgradeTableInfos)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
    }
}