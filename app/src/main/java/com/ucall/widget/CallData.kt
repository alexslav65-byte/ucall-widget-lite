package com.ucall.widget.lite

import android.database.Cursor
import android.provider.CallLog

data class CallData(
    val contactName: String,
    val phoneNumber: String,
    val callType: Int,
    val timestamp: Long
) {
    companion object {
        fun fromCursor(cursor: Cursor): CallData {
            val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)) ?: ""
            val name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)) ?: ""
            val type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
            val date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
            return CallData(name, number, type, date)
        }
    }
}
