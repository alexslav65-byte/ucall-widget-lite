package com.ucall.widget.lite

import android.content.Context

object LocalCallLogStore {

    private const val PREFS_NAME = "ucall_lite_last_call"
    private const val KEY_CONTACT_NAME = "ucall_lite_contact_name"
    private const val KEY_PHONE_NUMBER = "ucall_lite_phone_number"
    private const val KEY_CALL_TYPE = "ucall_lite_call_type"
    private const val KEY_TIMESTAMP = "ucall_lite_timestamp"

    fun saveLastCall(context: Context, callData: CallData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_CONTACT_NAME, callData.contactName)
            putString(KEY_PHONE_NUMBER, callData.phoneNumber)
            putInt(KEY_CALL_TYPE, callData.callType)
            putLong(KEY_TIMESTAMP, callData.timestamp)
            apply()
        }
    }

    fun getLastCall(context: Context): CallData? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val number = prefs.getString(KEY_PHONE_NUMBER, null) ?: return null
        return CallData(
            contactName = prefs.getString(KEY_CONTACT_NAME, "") ?: "",
            phoneNumber = number,
            callType = prefs.getInt(KEY_CALL_TYPE, -1),
            timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        )
    }
}
