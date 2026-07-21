package com.ucall.widget.lite

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CallLog
import androidx.core.content.ContextCompat

object CallUtils {

    fun openDialer(context: Context, number: String? = null) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (!number.isNullOrBlank()) data = Uri.parse("tel:${Uri.encode(number)}")
        }
        safeStart(context, intent)
    }

    fun makeCall(context: Context, number: String) {
        val hasCallPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPerm) {
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
        safeStart(context, intent)
    }

    /**
     * ✅ Повертає true тільки якщо call log реально відкрився.
     * MIUI/Samsung: часто треба explicit package = default dialer.
     */
    fun openCallLog(context: Context): Boolean {
        val pm = context.packageManager

        // ✅ Найчастіше працює саме так (як у тебе)
        val i1 = Intent(Intent.ACTION_VIEW).apply {
            type = CallLog.Calls.CONTENT_TYPE
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val r1 = i1.resolveActivity(pm)
        if (r1 != null && safeStart(context, i1)) return true

        // 🔁 Другий шанс (інколи працює, але часто ні)
        val i2 = Intent(Intent.ACTION_VIEW).apply {
            data = CallLog.Calls.CONTENT_URI
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val r2 = i2.resolveActivity(pm)
        if (r2 != null && safeStart(context, i2)) return true

        // 🧯 Fallback
        openDialer(context)
        return false
    }

    private fun safeStart(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }
}
