package com.antonio.android.sqlbatis.app

import android.content.ContentValues
import android.database.ContentObserver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.antonio.android.sqlbatis.util.printLog

class MainActivity : AppCompatActivity() {
    companion object {
        const val URI_A = "content://com.antonio.test/testDb1"
        const val URI_B = "content://com.antonio.test/testDb2"

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
            this@MainActivity.printLog("onChange ====> selfChange=$selfChange, uri=$uri")
        }
    }

    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contentResolver.registerContentObserver(uri1, true, observer)
        contentResolver.registerContentObserver(uri2, true, observer)

        findViewById<TextView>(R.id.tableA).setOnClickListener {
            val result = contentResolver.insert(Uri.parse("$URI_A/$TABLE_A2"), ContentValues().apply {
                index++
                put("int_null", index)
                put("real_null", index.toFloat())
                put("text_null", "AAA${index}")
                put("blob_null", byteArrayOf(index.toByte()))
            })
            printLog("insert ====> result=$result")
        }
        findViewById<TextView>(R.id.tableB).setOnClickListener {
            val result = contentResolver.insert(Uri.parse("$URI_B/$TABLE_B2"), ContentValues().apply {
                index++
                put("price_value_b", index.toFloat())
                put("msg_value_b", "BBB$index")
            })
            printLog("insert ====> result=$result")
        }
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(observer)
        super.onDestroy()
    }

}