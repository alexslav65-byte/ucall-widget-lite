package com.ucall.widget.lite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

class WidgetBlinkReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val data = intent.data?.toString().orEmpty()

        // очікуємо: ucall-lite://blink/<appWidgetId>
        val wid = try {
            val uri = Uri.parse(data)
            uri.lastPathSegment?.toIntOrNull() ?: -1
        } catch (_: Throwable) {
            -1
        }

        // Оновлюємо всі (простiше і надійно)
        UpdateOrchestrator.requestRefreshAndRender(context, "WidgetBlink")
    }
}
