package com.ucall.widget.lite

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class CallUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        UpdateOrchestrator.updateFrom(context, "CallUpdateWorker", delayMs = 1000)
        return Result.success()
    }
}

