package com.ucall.widget.lite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.telephony.TelephonyManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class CallEndReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val prefs = context.getSharedPreferences(WidgetSettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val wasInCall = prefs.getBoolean(KEY_WAS_IN_CALL, false)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING,
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (!wasInCall) {
                    prefs.edit().putBoolean(KEY_WAS_IN_CALL, true).apply()
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (!wasInCall) {
                    return
                }

                val now = SystemClock.elapsedRealtime()
                val last = prefs.getLong(KEY_LAST_IDLE_ELAPSED, 0L)
                if (now - last < 1500L) {
                    return
                }

                prefs.edit()
                    .putLong(KEY_LAST_IDLE_ELAPSED, now)
                    .putBoolean(KEY_WAS_IN_CALL, false)
                    .apply()

                UpdateOrchestrator.requestRefreshAndRender(context.applicationContext, "PHONE_STATE IDLE")
                scheduleCallEndUpdate(context.applicationContext)
            }
        }
    }

    private fun scheduleCallEndUpdate(context: Context) {
        val work = OneTimeWorkRequestBuilder<CallEndUpdateWorker>()
            .setInputData(workDataOf("source" to "CallEndReceiver"))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK, ExistingWorkPolicy.REPLACE, work)
    }

    companion object {
        private const val UNIQUE_WORK = "ucall_lite_call_end_update"
        private const val KEY_WAS_IN_CALL = "ucall_lite_was_in_call"
        private const val KEY_LAST_IDLE_ELAPSED = "ucall_lite_last_idle_elapsed"
    }
}
