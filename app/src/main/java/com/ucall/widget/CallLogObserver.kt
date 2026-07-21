package com.ucall.widget.lite

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog

class CallLogObserver : ContentObserver(null) {

    private var appContext: Context? = null

    // Дебаунсер: якщо подій багато підряд — оновимо один раз через короткий інтервал
    private val handler = Handler(Looper.getMainLooper())
    private val debounceMs = 150L
    private val updateRunnable = Runnable {
        appContext?.let { ctx ->
            // Без додаткових sleep у Оркестра — швидка реакція
            UpdateOrchestrator.updateFrom(ctx, source = "Observer", delayMs = 0)
        }
    }

    fun register(context: Context) {
        appContext = context.applicationContext
        appContext?.contentResolver?.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            this
        )
    }

    fun unregister() {
        handler.removeCallbacks(updateRunnable)
        appContext?.contentResolver?.unregisterContentObserver(this)
        appContext = null
    }

    override fun onChange(selfChange: Boolean) {
        // Скасувати попередній запуск і поставити новий — останній подія виграє
        handler.removeCallbacks(updateRunnable)
        handler.postDelayed(updateRunnable, debounceMs)
    }
}
