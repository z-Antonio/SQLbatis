package com.sqlbatis.android

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.sqlbatis.android.handle.DatabaseHandler
import com.sqlbatis.android.handle.DatabaseInit
import com.sqlbatis.android.util.inject
import com.sqlbatis.android.util.printSQL

object SQLbatis {

    private lateinit var context: Context
    private val manager: DatabaseInit by inject()
    private var handler: DatabaseHandler? = null
    private var helperClass: Class<out SQLbatisHelper> = SQLbatisHelper::class.java

    fun registerSqlHelper(clazz: Class<out SQLbatisHelper>) {
        helperClass = clazz
    }

    @Synchronized
    fun init(context: Context): DatabaseHandler {
        this.context = context.applicationContext
        return handler ?: DatabaseHandler {
            creator(it)
        }.apply {
            manager.initial(this)
            handler = this
            this@SQLbatis.printSQL(this.toString())
        }
    }

    fun getDatabase(context: Context, dbName: String): SQLiteDatabase? = init(context).getDatabase(dbName)

    private fun creator(dbName: String): SQLbatisHelper {
        return helperClass.constructors[0].newInstance(context, dbName) as SQLbatisHelper
    }
}