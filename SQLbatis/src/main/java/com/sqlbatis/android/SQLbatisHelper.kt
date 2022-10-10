package com.sqlbatis.android

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.system.ErrnoException
import android.system.Os
import com.sqlbatis.android.handle.TableInfo
import com.sqlbatis.android.util.L
import com.sqlbatis.android.util.printSQL

typealias SQLbatisCreator = (dbName: String) -> SQLbatisHelper

open class SQLbatisHelper(context: Context, name: String) {

    companion object {
        const val MEMORY_DB_PATH = ":memory:"

        const val S_IRUSR = 256
        const val S_IWUSR = 128
        const val S_IRGRP = 32
        const val S_IWGRP = 16
    }

    private val mContext: Context
    private val mName: String
    private val mTableInfos = mutableListOf<TableInfo>()
    private var mIsInitializing = false
    private var mDatabase: SQLiteDatabase? = null

    init {
        mContext = context.applicationContext
        mName = name
    }

    fun addTableInfo(tableInfo: TableInfo) {
        mTableInfos.add(tableInfo)
    }

    fun getContext(): Context = mContext

    fun getDatabaseName(): String = mName

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
                        onCreate(db, mTableInfos)
                        db.version = 1
                    } else {
                        checkAndUpgrade(db, mTableInfos).let {
                            if (it.isNotEmpty()) {
                                onUpgrade(db, it)
                                db.version = db.version + 1
                            }
                        }
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
            printSQL("Failed to chmod($dbPath): $e", L.W)
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

    private fun checkAndUpgrade(db: SQLiteDatabase, tableInfos: List<TableInfo>): List<TableInfo> {
        val upgrades = mutableListOf<TableInfo>()
        tableInfos.forEach { tableInfo ->
            try {
                db.rawQuery("SELECT * FROM ${tableInfo.name} LIMIT 1", null)?.let { c ->
                    tableInfo.alterSQL(c).apply {
                        if (this.isNotEmpty()) {
                            upgrades.add(tableInfo)
                        }
                    }.forEach { sql ->
                        db.execSQL(sql)
                    }
                    try {
                        c.close()
                    } catch (e: Exception) {
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                upgrades.add(tableInfo)
                db.execSQL(tableInfo.createSQL())
            }
        }
        return upgrades
    }

    open fun onUpgrade(db: SQLiteDatabase, upgradeTableInfos: List<TableInfo>) {}

    open fun onOpen(db: SQLiteDatabase) {}

    override fun toString(): String {
        val sb = StringBuilder("===> db name: ${mName}\n")
        mTableInfos.forEach { tableInfo ->
            sb.append(tableInfo.toString())
        }
        return sb.toString()
    }
}