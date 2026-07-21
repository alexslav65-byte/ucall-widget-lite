package com.ucall.widget.lite

import android.content.Context
import android.provider.CallLog
import java.text.DateFormat
import java.util.Date

object RefreshDiagnosticsStore {
    private const val PREFS_NAME = "ucall_lite_refresh_diagnostics"
    private const val KEY_PHONE_STATE = "last_phone_state_trigger"
    private const val KEY_CALL_LOG_OBSERVER = "last_call_log_observer_trigger"
    private const val KEY_TELEPHONY_CALLBACK = "last_telephony_callback_trigger"
    private const val KEY_PERIODIC_FALLBACK = "last_periodic_fallback_trigger"
    private const val KEY_CALL_LOG_URI_WORK = "last_call_log_uri_work_trigger"
    private const val KEY_SUCCESSFUL_REFRESH = "last_successful_refresh"
    private const val KEY_RENDERED_CALL_TIMESTAMP = "last_rendered_call_timestamp"
    private const val KEY_RENDERED_CALL_TYPE = "last_rendered_call_type"

    fun markPhoneStateTrigger(context: Context) = putNow(context, KEY_PHONE_STATE)

    fun markCallLogObserverTrigger(context: Context) = putNow(context, KEY_CALL_LOG_OBSERVER)

    fun markTelephonyCallbackTrigger(context: Context) = putNow(context, KEY_TELEPHONY_CALLBACK)

    fun markPeriodicFallbackTrigger(context: Context) = putNow(context, KEY_PERIODIC_FALLBACK)

    fun markCallLogUriWorkTrigger(context: Context) = putNow(context, KEY_CALL_LOG_URI_WORK)

    fun markTrigger(context: Context, source: String) {
        when {
            source == "PHONE_STATE IDLE" -> markPhoneStateTrigger(context)
            source.startsWith("CallLogObserver") -> markCallLogObserverTrigger(context)
            source.startsWith("TelephonyCallback") -> markTelephonyCallbackTrigger(context)
            source == "Periodic fallback" -> markPeriodicFallbackTrigger(context)
            source.startsWith("CallLog URI Work") -> markCallLogUriWorkTrigger(context)
        }
    }

    fun markSuccessfulRefresh(context: Context) {
        val callData = LocalCallLogStore.getLastCall(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putLong(KEY_SUCCESSFUL_REFRESH, System.currentTimeMillis())
            .putLong(KEY_RENDERED_CALL_TIMESTAMP, callData?.timestamp ?: 0L)
            .putInt(KEY_RENDERED_CALL_TYPE, callData?.callType ?: -1)
            .apply()
    }

    fun buildSummary(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val widgetCount = UCallWidgetV2.getWidgetCount(context)
        val renderedTimestamp = prefs.getLong(KEY_RENDERED_CALL_TIMESTAMP, 0L)
        val renderedType = prefs.getInt(KEY_RENDERED_CALL_TYPE, -1)
        return buildString {
            appendLine("Diagnostics")
            appendLine("PHONE_STATE: ${formatTime(prefs.getLong(KEY_PHONE_STATE, 0L))}")
            appendLine("CallLog observer: ${formatTime(prefs.getLong(KEY_CALL_LOG_OBSERVER, 0L))}")
            appendLine("TelephonyCallback: ${formatTime(prefs.getLong(KEY_TELEPHONY_CALLBACK, 0L))}")
            appendLine("Periodic fallback: ${formatTime(prefs.getLong(KEY_PERIODIC_FALLBACK, 0L))}")
            appendLine("CallLog URI Work: ${formatTime(prefs.getLong(KEY_CALL_LOG_URI_WORK, 0L))}")
            appendLine("Last refresh: ${formatTime(prefs.getLong(KEY_SUCCESSFUL_REFRESH, 0L))}")
            appendLine("Rendered call: ${formatDateTime(renderedTimestamp)} / ${formatCallType(renderedType)}")
            append("Widget IDs: $widgetCount")
        }
    }

    private fun putNow(context: Context, key: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putLong(key, System.currentTimeMillis())
            .apply()
    }

    private fun formatTime(value: Long): String =
        if (value > 0L) DateFormat.getTimeInstance(DateFormat.MEDIUM).format(Date(value)) else "-"

    private fun formatDateTime(value: Long): String =
        if (value > 0L) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date(value)) else "-"

    private fun formatCallType(type: Int): String = when (type) {
        CallLog.Calls.INCOMING_TYPE -> "incoming"
        CallLog.Calls.OUTGOING_TYPE -> "outgoing"
        CallLog.Calls.MISSED_TYPE -> "missed"
        CallLog.Calls.REJECTED_TYPE -> "rejected"
        else -> "-"
    }
}
