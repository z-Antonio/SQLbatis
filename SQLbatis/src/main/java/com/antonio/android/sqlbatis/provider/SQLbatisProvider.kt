package com.antonio.android.sqlbatis.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.antonio.android.sqlbatis.SQLbatis
import com.antonio.android.sqlbatis.handle.DatabaseHandler
import com.antonio.android.sqlbatis.util.humpToUnderline

class SQLbatisProvider : ContentProvider() {

    private lateinit var handler: DatabaseHandler

    override fun onCreate(): Boolean {
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
            val result = db.insert(table, null, values)
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
        db.update(table, values, selection, selectionArgs)
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