package com.ucall.widget.lite

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.work.Worker
import androidx.work.WorkerParameters

class CallEndUpdateWorker(
    ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    override fun doWork(): Result {
        val source = inputData.getString("source") ?: "CallEndWorker"

        for ((index, delayMs) in ATTEMPT_DELAYS_MS.withIndex()) {
            try {
                if (delayMs > 0) Thread.sleep(delayMs)

                UpdateOrchestrator.requestRefreshAndRender(
                    applicationContext,
                    "EndCallWorker #${index + 1}"
                )
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                return Result.retry()
            } catch (_: Throwable) {
            }
        }

        maybeLaunchEndCallWakeActivity(applicationContext)
        return Result.success()
    }

    private fun maybeLaunchEndCallWakeActivity(context: Context) {
        if (!ReliabilityExperimentFlags.ENABLE_ENDCALL_WAKE_ACTIVITY) return

        val prefs = context.getSharedPreferences(WidgetSettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val now = SystemClock.elapsedRealtime()
        val lastLaunch = prefs.getLong(KEY_LAST_WAKE_ACTIVITY_ELAPSED, 0L)
        if (now - lastLaunch < END_CALL_WAKE_ACTIVITY_DEBOUNCE_MS) return

        try {
            val intent = Intent(context, EndCallWakeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            context.startActivity(intent)
            prefs.edit().putLong(KEY_LAST_WAKE_ACTIVITY_ELAPSED, now).apply()
        } catch (_: Throwable) {
        }
    }

    companion object {
        private val ATTEMPT_DELAYS_MS = longArrayOf(800L, 1000L, 1700L)
        private const val END_CALL_WAKE_ACTIVITY_DEBOUNCE_MS = 10_000L
        private const val KEY_LAST_WAKE_ACTIVITY_ELAPSED = "ucall_lite_last_endcall_wake_activity_elapsed"
    }
}
