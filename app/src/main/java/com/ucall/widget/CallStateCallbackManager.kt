package com.ucall.widget.lite

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

object CallStateCallbackManager {
    private val handler = Handler(Looper.getMainLooper())
    private var appContext: Context? = null
    private var telephonyManager: TelephonyManager? = null
    private var callback: TelephonyCallback? = null
    private var wasInCall = false

    fun start(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val safeContext = context.applicationContext
        if (!hasPermission(safeContext)) return
        if (callback != null) return

        val manager = safeContext.getSystemService(TelephonyManager::class.java) ?: return
        val listener = CallStateListener()
        val executor = Executor { command -> handler.post(command) }

        try {
            manager.registerTelephonyCallback(executor, listener)
            appContext = safeContext
            telephonyManager = manager
            callback = listener
        } catch (_: Throwable) {
            callback = null
            telephonyManager = null
            appContext = null
        }
    }

    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = telephonyManager
            val currentCallback = callback
            if (manager != null && currentCallback != null) {
                try {
                    manager.unregisterTelephonyCallback(currentCallback)
                } catch (_: Throwable) {
                }
            }
        }
        handler.removeCallbacksAndMessages(null)
        callback = null
        telephonyManager = null
        appContext = null
        wasInCall = false
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private class CallStateListener : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            val context = appContext ?: return
            when (state) {
                TelephonyManager.CALL_STATE_RINGING,
                TelephonyManager.CALL_STATE_OFFHOOK -> wasInCall = true
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (!wasInCall) return
                    wasInCall = false
                    UpdateOrchestrator.requestRefreshAndRender(context, "TelephonyCallback IDLE")
                    handler.postDelayed({
                        UpdateOrchestrator.requestRefreshAndRender(context, "TelephonyCallback delayed")
                    }, 1_500L)
                    handler.postDelayed({
                        UpdateOrchestrator.requestRefreshAndRender(context, "TelephonyCallback delayed")
                    }, 4_000L)
                }
            }
        }
    }

    private fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
}
