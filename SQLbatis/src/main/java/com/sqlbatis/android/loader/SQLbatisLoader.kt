package com.sqlbatis.android.loader

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import androidx.core.os.OperationCanceledException
import androidx.loader.content.AsyncTaskLoader
import com.sqlbatis.android.util.convert
import com.sqlbatis.android.util.transferUri

class SQLbatisLoader<T>(context: Context, authorities: String, private val clazz: Class<T>) :
    AsyncTaskLoader<List<T>>(context) {

    companion object {
        inline fun <reified T> createLoader(
            context: Context,
            authorities: String
        ): SQLbatisLoader<T> = SQLbatisLoader(context, authorities, T::class.java)
    }

    private val mObserver = ForceLoadContentObserver()
    private val mUri = clazz.transferUri(authorities)

    private var mSelection: String? = null
    private var mSelectionArgs: Array<String>? = null
    private var mSortOrder: String? = null

    private var mCursor: Cursor? = null
    private var mCancellationSignal: CancellationSignal? = null

    /* Runs on a worker thread */
    override fun loadInBackground(): List<T> {
        synchronized(this) {
            if (isLoadInBackgroundCanceled) {
                throw OperationCanceledException()
            }
            mCancellationSignal = CancellationSignal()
        }
        val oldCursor = mCursor
        try {
            val cursor = ContentResolverCompat.query(
                context.contentResolver,
                mUri,
                null,
                mSelection,
                mSelectionArgs,
                mSortOrder,
                mCancellationSignal
            )
            try {
                cursor.count
                cursor.registerContentObserver(mObserver)
                mCursor = cursor
                return cursor.convert(clazz)
            } catch (e: RuntimeException) {
                cursor.close()
                throw e
            }
        } finally {
            if (oldCursor != null && oldCursor !== mCursor && !oldCursor.isClosed) {
                oldCursor.close()
            }
            synchronized(this) { mCancellationSignal = null }
        }
    }

    override fun cancelLoadInBackground() {
        super.cancelLoadInBackground()
        synchronized(this) {
            if (mCancellationSignal != null) {
                mCancellationSignal!!.cancel()
            }
        }
    }

    override fun deliverResult(list: List<T>?) {
        if (isReset) {
            // An async query came in while the loader is stopped
            mCursor?.close()
            return
        }
        if (isStarted) {
            super.deliverResult(list)
        }
    }

    override fun onStartLoading() {
        mCursor?.let {
            deliverResult(it.convert(clazz))
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad()
    }

    override fun onCanceled(list: List<T>?) {
        mCursor?.let { cursor ->
            if (!cursor.isClosed) {
                cursor.close()
            }
        }
    }

    override fun onReset() {
        super.onReset()

        // Ensure the loader is stopped
        onStopLoading()
        mCursor?.let {
            if (!it.isClosed) {
                it.close()
            }
            mCursor = null
        }
    }

    fun getUri(): Uri = mUri

    fun setSelection(selection: String?) {
        mSelection = selection
    }

    fun setSelectionArgs(selectionArgs: Array<String>?) {
        mSelectionArgs = selectionArgs
    }

    fun setSortOrder(sortOrder: String?) {
        mSortOrder = sortOrder
    }

}