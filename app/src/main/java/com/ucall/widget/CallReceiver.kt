package com.ucall.widget.lite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        UpdateOrchestrator.updateFrom(context, "CallReceiver", delayMs = 1000)
    }
}


