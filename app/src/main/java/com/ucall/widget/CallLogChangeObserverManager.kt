package com.ucall.widget.lite

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import androidx.core.content.ContextCompat

object CallLogChangeObserverManager {
    private const val DEBOUNCE_MS = 800L
    private const val SECOND_REFRESH_DELAY_MS = 4_000L

    private val handler = Handler(Looper.getMainLooper())
    private var appContext: Context? = null
    private var observer: ContentObserver? = null
    private var refreshRunning = false

    private val refreshRunnable = Runnable {
        val context = appContext ?: return@Runnable
        refreshRunning = false
        UpdateOrchestrator.requestRefreshAndRender(context, "CallLogObserver")
        handler.removeCallbacks(secondRefreshRunnable)
        handler.postDelayed(secondRefreshRunnable, SECOND_REFRESH_DELAY_MS)
    }

    private val secondRefreshRunnable = Runnable {
        val context = appContext ?: return@Runnable
        UpdateOrchestrator.requestRefreshAndRender(context, "CallLogObserver delayed")
    }

    fun start(context: Context) {
        val safeContext = context.applicationContext
        if (!hasPermission(safeContext)) return
        if (observer != null) return

        appContext = safeContext
        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                scheduleDebouncedRefresh()
            }
        }.also { contentObserver ->
            safeContext.contentResolver.registerContentObserver(
                CallLog.Calls.CONTENT_URI,
                true,
                contentObserver
            )
        }
    }

    fun stop() {
        val context = appContext
        val currentObserver = observer
        if (context != null && currentObserver != null) {
            try {
                context.contentResolver.unregisterContentObserver(currentObserver)
            } catch (_: Throwable) {
            }
        }
        handler.removeCallbacks(refreshRunnable)
        handler.removeCallbacks(secondRefreshRunnable)
        observer = null
        appContext = null
        refreshRunning = false
    }

    private fun scheduleDebouncedRefresh() {
        if (refreshRunning) return
        refreshRunning = true
        handler.removeCallbacks(refreshRunnable)
        handler.postDelayed(refreshRunnable, DEBOUNCE_MS)
    }

    private fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED
}
