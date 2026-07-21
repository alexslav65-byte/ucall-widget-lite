package com.ucall.widget.lite

import android.content.Context

object WidgetProviderRecovery {

    fun recover(context: Context) {
        val appCtx = context.applicationContext

        try {
            UCallWidgetV2.registerCallObserver(appCtx)
        } catch (_: Throwable) {
        }

        try {
            UpdateOrchestrator.requestRefreshAndRender(appCtx, "Provider recovery")
            WidgetRefreshScheduler.scheduleIfUseful(appCtx)
        } catch (_: Throwable) {
        }
    }
}
