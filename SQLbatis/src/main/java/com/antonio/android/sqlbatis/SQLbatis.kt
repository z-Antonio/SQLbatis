package com.antonio.android.sqlbatis

import android.content.Context
import com.antonio.android.sqlbatis.handle.DatabaseHandler
import com.antonio.android.sqlbatis.handle.DatabaseInit
import com.antonio.android.sqlbatis.util.inject
import com.antonio.android.sqlbatis.util.printLog

object SQLbatis {

    private val manager: DatabaseInit by inject()

    fun init(context: Context): DatabaseHandler = DatabaseHandler(context).apply {
        manager.initial(this)
        this@SQLbatis.printLog(this.toString())
    }

}