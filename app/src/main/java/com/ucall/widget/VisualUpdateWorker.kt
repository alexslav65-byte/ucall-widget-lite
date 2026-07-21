package com.ucall.widget.lite

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class VisualUpdateWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        UpdateOrchestrator.updateFrom(applicationContext, "VisualWorker", delayMs = 0)
        return Result.success()
    }



    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<VisualUpdateWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
