package com.ucall.widget.lite

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle

class MyWideWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        recoverWidget(context)
    }

    override fun onEnabled(context: Context) {
        recoverWidget(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        UpdateOrchestrator.requestRefreshAndRender(context, "Resize/options")
    }

    override fun onDisabled(context: Context) {
        UCallWidgetV2.unregisterCallObserver()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> recoverWidget(context)
        }
    }

    private fun recoverWidget(context: Context) {
        WidgetProviderRecovery.recover(context)
    }
}
