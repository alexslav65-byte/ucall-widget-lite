package com.ucall.widget.lite

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PeriodicWidgetRefreshWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!PermissionUtils.hasRuntimePermissions(applicationContext)) {
            return Result.success()
        }
        UpdateOrchestrator.requestRefreshAndRender(applicationContext, "Periodic fallback")
        return Result.success()
    }
}
