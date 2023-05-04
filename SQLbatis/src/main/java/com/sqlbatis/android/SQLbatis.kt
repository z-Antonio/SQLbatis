package com.sqlbatis.android

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.sqlbatis.android.annotation.ColumnName
import com.sqlbatis.android.annotation.Database
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

    inline fun <reified T> getUri(authorities: String): Uri = T::class.java.transferUri(authorities)

    inline fun <reified T> queryByAuthorities(
        context: Context, authorities: String, selection: String? = null,
        selectionArgs: Array<out String>? = null,
        sortOrder: String? = null
    ): List<T> = queryByUri(context, getUri<T>(authorities), selection, selectionArgs, sortOrder)

    inline fun <reified T> queryByUri(
        context: Context, uri: Uri, selection: String? = null,
        selectionArgs: Array<out String>? = null,
        sortOrder: String? = null
    ): List<T> {
        context.contentResolver.query(uri, null, selection, selectionArgs, sortOrder)
            ?.use { cursor ->
                return cursor.transform()
            }
        return emptyList()
    }

    inline fun <reified T> query(
        context: Context, selection: String? = null,
        selectionArgs: Array<out String>? = null,
        sortOrder: String? = null
    ): List<T> {
        findDatabase<Cursor>(context, T::class.java) { db, table ->
            db.query(table, null, selection, selectionArgs, null, null, sortOrder)
        }?.use { cursor ->
            return cursor.transform()
        }
        return emptyList()
    }

    @Deprecated("replace with #batchReplace", ReplaceWith("batchReplace(context, list)"))
    inline fun <reified T> batchInsertOrUpdate(context: Context, list: List<T>): Int {
        var size = 0
        findDatabase(context, T::class.java) { db, table ->
            db.beginTransaction()
            try {
                val updates = mutableListOf<T>()
                T::class.java.declaredFields.find { it.getAnnotation(ColumnName::class.java)?.primaryKey == true }
                    ?.let { field ->
                        field.isAccessible = true
                        val key = field.name.humpToUnderline()
                        val map = mutableMapOf<String, T>()
                        val values =
                            list.mapNotNull { field.get(it)?.toString()?.apply { map[this] = it } }
                        if (values.isNotEmpty()) {
                            db.rawQuery(
                                "SELECT $key FROM $table WHERE $key IN (${values.joinToString { "?" }})",
                                values.toTypedArray()
                            )?.use { c ->
                                if (c.moveToFirst()) {
                                    do {
                                        map.remove(c.getString(0))?.let { updates.add(it) }
                                    } while (c.moveToNext())
                                }
                            }
                        }
                    }
                list.forEach { item ->
                    if (item != null) {
                        try {
                            if (updates.contains(item)) {
                                val pKey = item.findPrimaryKey()
                                if (pKey?.second != null) {
                                    db.update(
                                        table,
                                        item?.toContentValues(),
                                        "${pKey.first}=?",
                                        arrayOf(pKey.second.toString())
                                    )
                                    size++
                                }
                            } else {
                                db.insert(table, null, item?.toContentValues())
                                size++
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        return size
    }

    inline fun <reified T> batchReplace(context: Context, list: List<T>): Int {
        var size = 0
        findDatabase(context, T::class.java) { db, table ->
            db.beginTransaction()
            try {
                list.forEach { item ->
                    try {
                        if (item != null && db.replace(table, null, item.toContentValues()) > -1) {
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

    @Deprecated("replace with #replace", ReplaceWith("replace(context, obj)"))
    fun insertOrUpdate(context: Context, obj: Any): Long {
        return findDatabase(context, obj.javaClass) { db, table ->
            val pKey = obj.findPrimaryKey()
            if (pKey?.second != null) {
                db.rawQuery(
                    "SELECT * FROM $table WHERE ${pKey.first}=?",
                    arrayOf(pKey.second.toString())
                )?.use { c ->
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
            try {
                return@findDatabase db.insert(table, null, obj.toContentValues())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@findDatabase -1
        }
    }

    fun replace(context: Context, obj: Any): Long {
        return findDatabase(context, obj.javaClass) { db, table ->
            try {
                return@findDatabase db.replace(table, null, obj.toContentValues())
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
                        if (item != null && db.replace(table, null, item.toContentValues()) > -1) {
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
        return insertList(context, list)
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

    inline fun <reified T> Cursor.transform(): List<T> = convert(T::class.java)
}