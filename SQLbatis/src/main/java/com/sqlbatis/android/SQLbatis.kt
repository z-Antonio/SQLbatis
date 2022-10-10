package com.sqlbatis.android

import com.sqlbatis.android.handle.DatabaseHandler
import com.sqlbatis.android.handle.DatabaseInit
import com.sqlbatis.android.util.inject
import com.sqlbatis.android.util.printSQL

object SQLbatis {

    private val manager: DatabaseInit by inject()

    fun init(creator: SQLbatisCreator): DatabaseHandler =
        DatabaseHandler(creator).apply {
            manager.initial(this)
            this@SQLbatis.printSQL(this.toString())
        }

}