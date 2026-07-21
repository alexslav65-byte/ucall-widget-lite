package com.ucall.widget.lite

import android.content.Context
import android.database.Cursor
import android.provider.CallLog

object CallLogReader {

    fun readLastCall(context: Context): CallData? {
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE
        )

        // ❗ Без LIMIT
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val number = it.getString(0) ?: ""
                val name = it.getString(1) ?: ""
                val type = it.getInt(2)
                val date = it.getLong(3)
                return CallData(name, number, type, date)
            }
        }

        return null
    }
}
