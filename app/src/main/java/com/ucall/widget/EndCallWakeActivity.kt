package com.ucall.widget.lite

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class EndCallWakeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UpdateOrchestrator.requestRefreshAndRender(this, "EndCallWakeActivity")
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, FINISH_DELAY_MS)
    }

    companion object {
        private const val FINISH_DELAY_MS = 300L
    }
}
