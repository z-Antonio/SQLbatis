package com.sqlbatis.android.app

import android.content.Context
import com.sqlbatis.android.SQLbatisHelper
import com.sqlbatis.android.provider.SQLbatisProvider

class MySqlBatisProvider: SQLbatisProvider() {

    override fun create(context: Context, dbName: String): SQLbatisHelper {
        return MySqlBatisHelper(context, dbName)
    }
}