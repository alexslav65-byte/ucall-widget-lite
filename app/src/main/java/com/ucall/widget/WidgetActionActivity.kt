package com.ucall.widget.lite

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class WidgetActionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Widget taps are foreground user actions; refresh data and render before acting.
        UpdateOrchestrator.requestRefreshAndRender(this, "WidgetTap")
        WidgetRefreshScheduler.scheduleIfUseful(this)
        handleAction(intent)
        finish()
    }

    private fun handleAction(intent: Intent) {
        when (intent.action) {
            WidgetActionReceiver.ACTION_CALL -> {
                val number = LocalCallLogStore.getLastCall(this)?.phoneNumber
                    ?: intent.getStringExtra(WidgetActionReceiver.EXTRA_NUMBER)
                if (!number.isNullOrBlank()) {
                    CallUtils.makeCall(this, number)
                } else {
                    CallUtils.openDialer(this)
                }
            }
            WidgetActionReceiver.ACTION_DIAL -> {
                CallUtils.openDialer(this, intent.getStringExtra(WidgetActionReceiver.EXTRA_NUMBER))
            }
            WidgetActionReceiver.ACTION_CALL_LOG -> {
                val opened = CallUtils.openCallLog(this)
                if (!opened) CallUtils.openDialer(this)
            }
            WidgetActionReceiver.ACTION_SETTINGS -> {
                startActivity(Intent(this, WidgetSettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(WidgetSettingsActivity.EXTRA_SETTINGS_MODE, WidgetSettingsActivity.MODE_BASIC)
                })
            }
        }
    }
}
