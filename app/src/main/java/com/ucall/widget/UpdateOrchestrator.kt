package com.ucall.widget.lite

import android.content.Context
import android.os.SystemClock
import com.ucall.widget.lite.UCallWidgetV2.updateAllWidgets

object UpdateOrchestrator {

    private const val DEBOUNCE_MS = 750L
    private const val CATCH_UP_PAUSE_MS = 300L

    private val lock = Any()
    private var refreshRunning = false
    private var pendingRefresh = false
    private var debounceScheduled = false
    private var lastRunStartedAt = 0L

    fun updateFrom(context: Context, source: String, delayMs: Long = 0) {
        Thread {
            try {
                if (delayMs > 0) Thread.sleep(delayMs)
                requestRefreshAndRender(context, source)
            } catch (_: Exception) {
            }
        }.start()
    }

    fun requestRefreshAndRender(context: Context, source: String) {
        val appContext = context.applicationContext
        RefreshDiagnosticsStore.markTrigger(appContext, source)
        DebugRefreshToast.show(appContext, source)

        val now = SystemClock.elapsedRealtime()
        synchronized(lock) {
            if (refreshRunning) {
                pendingRefresh = true
                return
            }

            val remainingDebounce = DEBOUNCE_MS - (now - lastRunStartedAt)
            if (remainingDebounce > 0L) {
                pendingRefresh = true
                if (!debounceScheduled) {
                    debounceScheduled = true
                    Thread {
                        try {
                            Thread.sleep(remainingDebounce)
                        } catch (_: InterruptedException) {
                        }
                        runPendingRefresh(appContext)
                    }.start()
                }
                return
            }

            refreshRunning = true
            lastRunStartedAt = now
        }

        Thread { runRefreshLoop(appContext) }.start()
    }

    private fun runPendingRefresh(context: Context) {
        synchronized(lock) {
            debounceScheduled = false
            if (refreshRunning) {
                pendingRefresh = true
                return
            }
            refreshRunning = true
            lastRunStartedAt = SystemClock.elapsedRealtime()
        }
        runRefreshLoop(context)
    }

    private fun runRefreshLoop(context: Context) {
        try {
            do {
                synchronized(lock) {
                    pendingRefresh = false
                }

                refreshLatestThenRender(context)

                try {
                    Thread.sleep(CATCH_UP_PAUSE_MS)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return
                }

                val shouldRunAgain = synchronized(lock) {
                    pendingRefresh
                }
            } while (shouldRunAgain)
        } finally {
            synchronized(lock) {
                refreshRunning = false
                if (pendingRefresh && !debounceScheduled) {
                    debounceScheduled = true
                    Thread {
                        try {
                            Thread.sleep(DEBOUNCE_MS)
                        } catch (_: InterruptedException) {
                        }
                        runPendingRefresh(context)
                    }.start()
                }
            }
        }
    }

    @Synchronized
    fun refreshLatestIfNew(context: Context, source: String = ""): Boolean {
        val latest = CallLogReader.readLastCall(context)

        if (latest == null) {
            return false
        }

        val current = LocalCallLogStore.getLastCall(context)

        return if (shouldUpdate(current, latest)) {
            LocalCallLogStore.saveLastCall(context, latest)
            true
        } else {
            false
        }
    }

    fun renderWidgetsFromStore(context: Context) {
        updateAllWidgets(context)
    }

    fun refreshLatestThenRender(context: Context, source: String = ""): Boolean {
        val changed = refreshLatestIfNew(context, source)
        // Latest unchanged still needs a RemoteViews render for launcher reliability.
        renderWidgetsFromStore(context)
        RefreshDiagnosticsStore.markSuccessfulRefresh(context)
        return changed
    }

    fun updateLatestIfNew(context: Context, source: String): Boolean =
        refreshLatestThenRender(context, source)

    fun forceInit(context: Context) {
        try {
            requestRefreshAndRender(context, "ForceInit")
        } catch (_: Exception) {
        }
    }

    private fun shouldUpdate(current: CallData?, latest: CallData): Boolean {
        return current == null ||
            latest.timestamp != current.timestamp ||
            latest.phoneNumber != current.phoneNumber ||
            latest.callType != current.callType
    }
}
