package com.ucall.widget.lite

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WidgetRefreshScheduler {
    private const val UNIQUE_WORK = "ucall_lite_periodic_widget_refresh"

    fun scheduleIfUseful(context: Context) {
        val appContext = context.applicationContext
        if (!hasRequiredPermissions(appContext) || UCallWidgetV2.getWidgetCount(appContext) == 0) {
            cancel(appContext)
            return
        }

        val request = PeriodicWorkRequestBuilder<PeriodicWidgetRefreshWorker>(
            15L,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork(UNIQUE_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
        CallLogContentTriggerScheduler.scheduleIfUseful(appContext)
    }

    fun cancel(context: Context) {
        val appContext = context.applicationContext
        WorkManager.getInstance(appContext).cancelUniqueWork(UNIQUE_WORK)
        CallLogContentTriggerScheduler.cancel(appContext)
    }

    private fun hasRequiredPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE
    )
}
