package com.ucall.widget.lite

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object DebugRefreshToast {
    const val ENABLE_REFRESH_TOASTS = false
    private const val SAME_SOURCE_SUPPRESS_MS = 1_000L
    private val lastShownBySource = mutableMapOf<String, Long>()

    fun show(context: Context, source: String) {
        if (!ENABLE_REFRESH_TOASTS) return

        val now = android.os.SystemClock.elapsedRealtime()
        synchronized(lastShownBySource) {
            val last = lastShownBySource[source] ?: 0L
            if (now - last < SAME_SOURCE_SUPPRESS_MS) return
            lastShownBySource[source] = now
        }

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                context.applicationContext,
                "UCall: $source",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
