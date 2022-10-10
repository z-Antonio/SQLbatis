package com.sqlbatis.android.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.sqlbatis.android.SQLbatis
import com.sqlbatis.android.SQLbatisHelper
import com.sqlbatis.android.handle.DatabaseHandler
import com.sqlbatis.android.util.humpToUnderline
import com.sqlbatis.android.util.transfer

open class SQLbatisProvider : ContentProvider() {

    private lateinit var handler: DatabaseHandler

    open fun supportSqlHelper(): Class<out SQLbatisHelper> = SQLbatisHelper::class.java

    override fun onCreate(): Boolean {
        SQLbatis.registerSqlHelper(supportSqlHelper())
        context?.let { ctx ->
            handler = SQLbatis.init(ctx)
        }
        return true
    }

    override fun getType(uri: Uri): String? = null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = doDatabase<Cursor>(uri) { db, table ->
        db.query(table, projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        doDatabase(uri, true) { db, table ->
            val result = db.insert(table, null, values?.transfer())
            return@doDatabase ContentUris.withAppendedId(uri, result)
        }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        doDatabase(uri, true) { db, table ->
            db.delete(table, selection, selectionArgs)
        } ?: 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = doDatabase(uri, true) { db, table ->
        db.update(table, values?.transfer(), selection, selectionArgs)
    } ?: 0

    private fun <T> doDatabase(
        uri: Uri,
        notify: Boolean = false,
        action: (db: SQLiteDatabase, table: String) -> T
    ): T? {
        val paths = uri.pathSegments
        if (paths.size > 1) {
            val dbName = paths[0].humpToUnderline()
            val tableName = paths[1].humpToUnderline()
            handler.getDatabase(dbName)?.let { db ->
                try {
                    return action(db, tableName)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (notify)
                        context?.contentResolver?.notifyChange(uri, null)
                }
            }
        }
        return null
    }
}