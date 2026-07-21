package com.ucall.widget.lite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import androidx.core.content.ContextCompat

class _WidgetClickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            ACTION_OPEN_DIALER -> {
                // Відкрити системний номеронабирач
                val dial = Intent(Intent.ACTION_DIAL).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(context, dial)
            }

            ACTION_OPEN_CALLLOG -> {
                // Спроба відкрити журнал дзвінків
                val callLogIntent = Intent(Intent.ACTION_VIEW).apply {
                    type = CallLog.Calls.CONTENT_TYPE
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (!safeStartActivity(context, callLogIntent)) {
                    // Fallback: номеронабирач
                    val dial = Intent(Intent.ACTION_DIAL).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    safeStartActivity(context, dial)
                }
            }

            ACTION_CALL_NUMBER -> {
                val number = intent.getStringExtra(EXTRA_NUMBER)?.trim().orEmpty()
                if (number.isNotBlank()) {
                    // Якщо немає дозволу CALL_PHONE — відкриємо DIAL
                    val hasCallPerm = ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.CALL_PHONE
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    val callIntent = if (hasCallPerm) {
                        Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:${Uri.encode(number)}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    } else {
                        Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${Uri.encode(number)}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    }
                    safeStartActivity(context, callIntent)
                }
            }
        }
    }

    private fun safeStartActivity(context: Context, intent: Intent): Boolean {
        return try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    companion object {
        const val ACTION_OPEN_DIALER = "com.ucall.widget.lite.action.OPEN_DIALER"
        const val ACTION_OPEN_CALLLOG = "com.ucall.widget.lite.action.OPEN_CALLLOG"
        const val ACTION_CALL_NUMBER = "com.ucall.widget.lite.action.CALL_NUMBER"
        const val EXTRA_NUMBER = "extra_number"
    }
}
