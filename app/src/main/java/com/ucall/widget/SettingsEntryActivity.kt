package com.ucall.widget.lite

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PermissionUtils.needsAnyPermission(this)) {
            startActivity(Intent(this, PermissionsOnboardingActivity::class.java))
            finish()
            return
        }

        openMainSettings()
    }

    private fun openMainSettings() {
        startActivity(
            Intent(this, WidgetSettingsActivity::class.java).apply {
                putExtra(WidgetSettingsActivity.EXTRA_SETTINGS_MODE, WidgetSettingsActivity.MODE_BASIC)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
        finish()
    }
}