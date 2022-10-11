package com.sqlbatis.android.app

import android.content.ContentValues
import android.database.ContentObserver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.sqlbatis.android.SQLbatis
import com.sqlbatis.android.app.db.TableA1
import com.sqlbatis.android.util.printSQL

class MainActivity : AppCompatActivity() {
    companion object {
        const val URI_A = "content://com.sqlbatis.test/testDb1"
        const val URI_B = "content://com.sqlbatis.test/testDb2"

        const val TABLE_A1 = "tableA1"
        const val TABLE_A2 = "tableA2"

        const val TABLE_B1 = "tableB1"
        const val TABLE_B2 = "tableB2"
    }

    private val uri1 = Uri.parse(URI_A)
    private val uri2 = Uri.parse(URI_B)

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            this@MainActivity.printSQL("onChange ====> selfChange=$selfChange, uri=$uri")
        }
    }

    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contentResolver.registerContentObserver(uri1, true, observer)
        contentResolver.registerContentObserver(uri2, true, observer)

        findViewById<TextView>(R.id.tableA).setOnClickListener {
            val list = mutableListOf<TableA1>().apply {
                for (i in 0..10) {
                    add(TableA1().apply {
                        textValue = "a$i"
                        intValue = i
                    })
                }
            }
            printSQL("insertList ===> ${SQLbatis.insertList(this, list)}")
            printSQL("delete ===> ${SQLbatis.delete(this, list[3])}")
            printSQL("query ===> ${SQLbatis.query<TableA1>(this)}")
            printSQL("insertOrUpdate ===> ${SQLbatis.insertOrUpdate(this, TableA1().apply {
                id = 5
                textValue = "b5"
                intValue = 50
            })}")
            printSQL("query ===> ${SQLbatis.query<TableA1>(this)}")
            printSQL("insertOrUpdate ===> ${SQLbatis.insertOrUpdate(this, TableA1().apply {
                id = 111
                textValue = "insert111"
                intValue = 111
            })}")
            val query = SQLbatis.query<TableA1>(this)
            printSQL("query ===> $query")
            printSQL("updateList ===> ${SQLbatis.updateList(this, query.map { TableA1().apply {
                this.id = it.id
                this.textValue = it.textValue?.replace("a", "c")
                this.intValue = it.intValue?.plus(10)
            } })}")
            printSQL("query ===> ${SQLbatis.query<TableA1>(this)}")
            printSQL("deleteList ===> ${SQLbatis.deleteList(this, query.filter { it.id?.rem(2) == 0 })}")
            printSQL("query ===> ${SQLbatis.query<TableA1>(this)}")
        }
        findViewById<TextView>(R.id.tableB).setOnClickListener {
            val result =
                contentResolver.insert(Uri.parse("$URI_B/$TABLE_B2"), ContentValues().apply {
                    index++
                    put("price_value_b", index.toFloat())
                    put("msg_value_b", "BBB$index")
                })
            printSQL("insert ====> result=$result")
        }
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(observer)
        super.onDestroy()
    }

}