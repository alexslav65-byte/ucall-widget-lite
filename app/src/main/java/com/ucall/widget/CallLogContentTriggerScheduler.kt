package com.ucall.widget.lite

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object CallLogContentTriggerScheduler {
    private const val UNIQUE_WORK = "ucall_calllog_content_uri_trigger"

    fun scheduleIfUseful(context: Context) {
        val appContext = context.applicationContext
        if (!hasPermission(appContext) || UCallWidgetV2.getWidgetCount(appContext) == 0) {
            cancel(appContext)
            return
        }

        val constraints = Constraints.Builder()
            .addContentUriTrigger(CallLog.Calls.CONTENT_URI, true)
            .build()

        val request = OneTimeWorkRequestBuilder<CallLogContentTriggerWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(appContext)
            .enqueueUniqueWork(UNIQUE_WORK, ExistingWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK)
    }

    private fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED
}
