package com.antonio.android.sqlbatis.handle

import android.database.Cursor
import com.antonio.android.sqlbatis.annotation.DataType
import com.antonio.android.sqlbatis.util.humpToUnderline
import com.antonio.android.sqlbatis.util.printLog

data class TableInfo(val name: String, val columns: List<ColumnInfo>) {

    class Builder(private val tableName: String) {
        private val columns = mutableListOf<ColumnInfo>()

        fun addColumn(
            name: String,
            primaryKey: Boolean = false,
            autoIncr: Boolean = false,
            nonNull: Boolean = false,
            default: String? = null,
            dataType: String? = null
        ): Builder {
            columns.add(
                ColumnInfo(
                    name.humpToUnderline(),
                    primaryKey,
                    autoIncr,
                    nonNull,
                    default,
                    dataType
                )
            )
            return this
        }

        fun build(): TableInfo = TableInfo(tableName.humpToUnderline(), columns)
    }

    fun createSQL(): String =
        "CREATE TABLE $name(${columns.joinToString { it.addColumn() }})".apply {
            printLog("CREATE TABLE ====> $this")
        }

    fun alterSQL(c: Cursor): List<String> = mutableListOf<String>().apply {
        val columnNames = c.columnNames
        columns.forEach { columnInfo ->
            if (!columnNames.contains(columnInfo.name)) {
                add("ALTER TABLE $name ADD COLUMN ${columnInfo.addColumn(true)}".apply {
                    printLog("ALTER TABLE ====> $this")
                })
            }
        }
    }

    override fun toString(): String {
        return ">>> Table: $name\n $columns \n"
    }
}

data class ColumnInfo(
    val name: String,
    val primaryKey: Boolean = false,
    val autoIncr: Boolean = false,
    val nonNull: Boolean = false,
    val default: String? = null,
    private val typeString: String? = null
) {
    val dataType: DataType =
        if (typeString.isNullOrEmpty()) DataType.text else DataType.valueOf(typeString)

    fun addColumn(isAlter: Boolean = false): String {
        val sb = StringBuilder("$name $dataType")
        if (!isAlter) {
            if (primaryKey) {
                sb.append(" PRIMARY KEY")
            }
            if (autoIncr && dataType == DataType.integer) {
                sb.append(" AUTOINCREMENT")
            }
        }
        if (nonNull) {
            sb.append(" NOT NULL")
            if (!default.isNullOrEmpty()) {
                sb.append(" DEFAULT $default")
            } else {
                sb.append(" DEFAULT ${dataType.defaultValue()}")
            }
        } else if (!default.isNullOrEmpty()) {
            sb.append(" DEFAULT $default")
        }
        return sb.toString()
    }

    override fun toString(): String {
        return "column: $name(primaryKey=$primaryKey, autoIncrement=$autoIncr, nonNull=$nonNull, default=$default, dataType=$dataType)\n"
    }
}