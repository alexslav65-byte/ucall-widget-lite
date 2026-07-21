package com.ucall.widget.lite

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class CallLogTimeCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        UpdateOrchestrator.updateFrom(applicationContext, "TimeCheckWorker", delayMs = 1000)
        return Result.success()
    }

    companion object {
        fun enqueueDelayed(context: Context, delaySeconds: Long = 1L) {
            val request = OneTimeWorkRequestBuilder<CallLogTimeCheckWorker>()
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}

