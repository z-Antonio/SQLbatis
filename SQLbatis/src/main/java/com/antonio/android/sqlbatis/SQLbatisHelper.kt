package com.antonio.android.sqlbatis

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.system.ErrnoException
import android.system.Os
import com.antonio.android.sqlbatis.handle.TableInfo
import com.antonio.android.sqlbatis.util.L
import com.antonio.android.sqlbatis.util.printLog

open class SQLbatisHelper(private val mContext: Context, private val mName: String? = null) {

    companion object {
        const val MEMORY_DB_PATH = ":memory:"

        const val S_IRUSR = 256
        const val S_IWUSR = 128
        const val S_IRGRP = 32
        const val S_IWGRP = 16
    }

    private val tableInfos = mutableListOf<TableInfo>()

    private var mIsInitializing = false
    private var mDatabase: SQLiteDatabase? = null

    fun addTableInfo(tableInfo: TableInfo) {
        tableInfos.add(tableInfo)
    }

    fun getDatabaseName(): String? = mName

    @Synchronized
    fun getDatabase(): SQLiteDatabase? {
        mDatabase?.let {
            if (!it.isOpen) {
                mDatabase = null
            } else {
                return it
            }
        }
        if (mIsInitializing) {
            throw IllegalStateException("getDatabase called recursively")
        }
        var db = mDatabase
        try {
            mIsInitializing = true
            if (db == null) {
                if (mName == null) {
                    db = SQLiteDatabase.openDatabase(
                        MEMORY_DB_PATH,
                        null,
                        SQLiteDatabase.CREATE_IF_NECESSARY,
                        null
                    )
                } else {
                    val filePath = mContext.getDatabasePath(mName)
                    try {
                        db = SQLiteDatabase.openDatabase(
                            filePath.path,
                            null,
                            SQLiteDatabase.CREATE_IF_NECESSARY,
                            null
                        )
                        // Keep pre-O-MR1 behavior by resetting file permissions to 660
                        setFilePermissionsForDb(filePath.path)
                    } catch (ex: SQLException) {
                        throw ex
                    }
                }
            }
            if (db != null) {
                onConfigure(db)
                db.beginTransaction()
                try {
                    if (db.version == 0) {
                        onCreate(db, tableInfos)
                        db.version = 1
                    } else if (beUpgrade(db, tableInfos)) {
                        db.version = db.version + 1
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                onOpen(db)
            }
            mDatabase = db
            return db
        } finally {
            mIsInitializing = false
            if (db != null && db != mDatabase) {
                db.close()
            }
        }
    }

    private fun setFilePermissionsForDb(dbPath: String) {
        val mode: Int = S_IRUSR or S_IWUSR or S_IRGRP or S_IWGRP
        try {
            Os.chmod(dbPath, mode)
        } catch (e: ErrnoException) {
            printLog("Failed to chmod($dbPath): $e", L.W)
        }
    }

    @Synchronized
    fun close() {
        if (mIsInitializing) throw IllegalStateException("Closed during initialization")
        if (mDatabase != null && mDatabase!!.isOpen) {
            mDatabase!!.close()
            mDatabase = null
        }
    }

    open fun onConfigure(db: SQLiteDatabase) {}

    open fun onCreate(db: SQLiteDatabase, tableInfos: List<TableInfo>) {
        tableInfos.forEach { tableInfo ->
            db.execSQL(tableInfo.createSQL())
        }
    }

    open fun beUpgrade(db: SQLiteDatabase, tableInfos: List<TableInfo>): Boolean {
        var upgrade = false
        tableInfos.forEach { tableInfo ->
            try {
                db.rawQuery("SELECT * FROM ${tableInfo.name} LIMIT 1", null)?.let { c ->
                    tableInfo.alterSQL(c).forEach { sql ->
                        db.execSQL(sql)
                        upgrade = true
                    }
                    c.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                db.execSQL(tableInfo.createSQL())
                upgrade = true
            }
        }
        return upgrade
    }

    open fun onOpen(db: SQLiteDatabase) {}

    override fun toString(): String {
        val sb = StringBuilder("===> db name: ${mName}\n")
        tableInfos.forEach { tableInfo ->
            sb.append(tableInfo.toString())
        }
        return sb.toString()
    }
}