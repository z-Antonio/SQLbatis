package com.sqlbatis.android

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.sqlbatis.android.handle.DatabaseHandler
import com.sqlbatis.android.handle.DatabaseInit
import com.sqlbatis.android.util.*

object SQLbatis {

    private lateinit var context: Context
    private val manager: DatabaseInit by inject()
    private var handler: DatabaseHandler? = null
    private var helperClass: Class<out SQLbatisHelper> = SQLbatisHelper::class.java

    private fun creator(dbName: String): SQLbatisHelper {
        return helperClass.constructors[0].newInstance(context, dbName) as SQLbatisHelper
    }

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

    fun getDatabase(context: Context, dbName: String): SQLiteDatabase? =
        init(context).getDatabase(dbName)

    inline fun <reified T> query(
        context: Context, selection: String? = null,
        selectionArgs: Array<out String>? = null,
        sortOrder: String? = null
    ): List<T> = mutableListOf<T>().apply {
        findDatabase<Cursor>(context, T::class.java) { db, table ->
            db.query(table, null, selection, selectionArgs, null, null, sortOrder)
        }?.let { cursor ->
            cursor.use { c ->
                if (c.moveToFirst()) {
                    do {
                        T::class.java.newInstance()?.let {
                            if (it.fillCursor(c)) {
                                add(it)
                            }
                        }
                    } while (c.moveToNext())
                }
            }
        }
    }

    fun insertOrUpdate(context: Context, obj: Any): Long {
        return findDatabase(context, obj.javaClass) { db, table ->
            val pKey = obj.findPrimaryKey()
            if (pKey?.second != null) {
                db.rawQuery("SELECT * FROM $table WHERE ${pKey.first}=?", arrayOf(pKey.second.toString()))?.let { cursor ->
                    cursor.use { c ->
                        if (c.count > 0) {
                            return@findDatabase db.update(
                                table,
                                obj.toContentValues(),
                                "${pKey.first}=?",
                                arrayOf(pKey.second.toString())
                            ).toLong()
                        }
                    }
                }
            }
            try {
                return@findDatabase db.insert(table, null, obj.toContentValues())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@findDatabase -1
        }
    }

    inline fun <reified T> insertList(context: Context, list: List<T>): Int {
        var size = 0
        findDatabase(context, T::class.java) { db, table ->
            db.beginTransaction()
            try {
                list.forEach { item ->
                    try {
                        if (item != null && db.insert(table, null, item.toContentValues()) > -1) {
                            size++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        return size
    }

    inline fun <reified T> updateList(context: Context, list: List<T>): Int {
        var size = 0
        findDatabase(context, T::class.java) { db, table ->
            db.beginTransaction()
            try {
                list.forEach { item ->
                    try {
                        if (item != null) {
                            val pKey = item.findPrimaryKey()
                            if (pKey?.second != null) {
                                size += db.update(
                                    table,
                                    item.toContentValues(),
                                    "${pKey.first}=?",
                                    arrayOf(pKey.second.toString())
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        return size
    }

    fun delete(context: Context, obj: Any): Int {
        return findDatabase(context, obj.javaClass) { db, table ->
            val pKey = obj.findPrimaryKey()
            if (pKey?.second != null) {
                return@findDatabase db.delete(
                    table,
                    "${pKey.first}=?",
                    arrayOf(pKey.second.toString())
                )
            } else {
                val cv = obj.toContentValues()
                if (cv.size() > 0) {
                    val keyList = mutableListOf<String>()
                    val valueList = mutableListOf<String>()
                    cv.keySet().forEach { key ->
                        keyList.add(key)
                        valueList.add(cv.get(key).toString())
                    }
                    val whareClause = keyList.joinToString(" AND ") { "$it=?" }
                    return@findDatabase db.delete(table, whareClause, valueList.toTypedArray())
                }
                return@findDatabase 0
            }
        }
    }

    inline fun <reified T> deleteList(context: Context, list: List<T>): Int {
        var size = 0
        findDatabase(context, T::class.java) { db, table ->
            db.beginTransaction()
            try {
                list.forEach { item ->
                    try {
                        if (item != null) {
                            val pKey = item.findPrimaryKey()
                            if (pKey?.second != null) {
                                size += db.delete(
                                    table,
                                    "${pKey.first}=?",
                                    arrayOf(pKey.second.toString())
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        return size
    }

}