package com.ucall.widget.lite

import android.app.Activity
import android.app.Application
import android.os.Bundle

class UCallLiteApplication : Application() {
    private var startedActivities = 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                startedActivities += 1
                if (startedActivities == 1) {
                    startProcessRefreshTriggers()
                }
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivities = (startedActivities - 1).coerceAtLeast(0)
                if (startedActivities == 0) {
                    stopProcessRefreshTriggers()
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }

    fun startProcessRefreshTriggers() {
        CallLogChangeObserverManager.start(this)
        CallStateCallbackManager.start(this)
        WidgetRefreshScheduler.scheduleIfUseful(this)
    }

    private fun stopProcessRefreshTriggers() {
        CallLogChangeObserverManager.stop()
        CallStateCallbackManager.stop()
    }
}
