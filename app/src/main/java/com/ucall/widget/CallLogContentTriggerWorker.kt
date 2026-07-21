package com.ucall.widget.lite

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class CallLogContentTriggerWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!hasPermission(applicationContext)) {
            CallLogContentTriggerScheduler.cancel(applicationContext)
            return Result.success()
        }

        UpdateOrchestrator.requestRefreshAndRender(applicationContext, "CallLog URI Work")
        delay(DELAYED_REFRESH_MS)
        UpdateOrchestrator.requestRefreshAndRender(applicationContext, "CallLog URI Work delayed")
        CallLogContentTriggerScheduler.scheduleIfUseful(applicationContext)
        return Result.success()
    }

    private fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED

    companion object {
        private const val DELAYED_REFRESH_MS = 4_000L
    }
}
