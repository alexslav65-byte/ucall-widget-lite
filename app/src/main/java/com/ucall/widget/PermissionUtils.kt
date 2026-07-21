package com.ucall.widget.lite

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {
    val RUNTIME_PERMS = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE
    )

    fun hasRuntimePermissions(ctx: Context): Boolean =
        RUNTIME_PERMS.all { ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED }

    fun missingRuntimePermissions(ctx: Context): Array<String> =
        RUNTIME_PERMS.filter {
            ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

    fun needsAnyPermission(ctx: Context): Boolean = !hasRuntimePermissions(ctx)
}
