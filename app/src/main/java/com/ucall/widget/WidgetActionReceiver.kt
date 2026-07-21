package com.ucall.widget.lite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val appContext = context.applicationContext
        UpdateOrchestrator.requestRefreshAndRender(appContext, "WidgetActionReceiver")
        val num = intent.getStringExtra(EXTRA_NUMBER)

        when (action) {
            ACTION_CALL -> {
                val number = num ?: LocalCallLogStore.getLastCall(appContext)?.phoneNumber
                if (!number.isNullOrBlank()) {
                    CallUtils.makeCall(appContext, number)
                } else {
                    CallUtils.openDialer(appContext)
                }
            }
            ACTION_DIAL -> CallUtils.openDialer(appContext, num)
            ACTION_CALL_LOG -> {
                val ok = CallUtils.openCallLog(appContext)
                if (!ok) CallUtils.openDialer(appContext)
            }
            ACTION_SETTINGS -> {
                val i = Intent(appContext, WidgetSettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(WidgetSettingsActivity.EXTRA_SETTINGS_MODE, WidgetSettingsActivity.MODE_BASIC)
                }
                appContext.startActivity(i)
            }
        }
    }

    companion object {
        const val ACTION_CALL = "com.ucall.widget.lite.ACTION_CALL"
        const val ACTION_DIAL = "com.ucall.widget.lite.ACTION_DIAL"
        const val ACTION_CALL_LOG = "com.ucall.widget.lite.ACTION_CALL_LOG"
        const val ACTION_SETTINGS = "com.ucall.widget.lite.ACTION_SETTINGS"

        const val EXTRA_NUMBER = "extra_number"
    }
}
