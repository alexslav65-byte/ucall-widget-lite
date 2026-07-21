package com.ucall.widget.lite

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionsOnboardingActivity : AppCompatActivity() {

    private var btnContinue: Button? = null
    private var permissionMessage: TextView? = null
    private var openSettingsRequired = false
    private var flowCompleted = false
    private var fallbackShown = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.all { it }) {
                continueAfterPermissions()
            } else {
                openSettingsRequired = hasPermanentlyDeniedPermission()
                showFallbackUi()
            }
        }

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLang(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PermissionUtils.hasRuntimePermissions(this)) {
            continueAfterPermissions()
        } else if (savedInstanceState == null) {
            Handler(Looper.getMainLooper()).post { requestMissingPermissions() }
        } else {
            showFallbackUi()
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.hasRuntimePermissions(this)) {
            continueAfterPermissions()
        } else if (fallbackShown) {
            updateUiState()
        }
    }

    private fun handleContinue() {
        when {
            PermissionUtils.hasRuntimePermissions(this) -> continueAfterPermissions()
            openSettingsRequired -> openAppSettings()
            else -> requestMissingPermissions()
        }
    }

    private fun continueAfterPermissions() {
        if (flowCompleted) return
        flowCompleted = true
        UpdateOrchestrator.requestRefreshAndRender(this, "Permission grant")
        WidgetRefreshScheduler.scheduleIfUseful(this)
        (application as? UCallLiteApplication)?.startProcessRefreshTriggers()
        startActivity(Intent(this, OnboardingActivity::class.java).apply {
            putExtra(WidgetSettingsActivity.EXTRA_SETTINGS_MODE, WidgetSettingsActivity.MODE_BASIC)
        })
        finish()
    }

    private fun requestMissingPermissions() {
        val missing = PermissionUtils.missingRuntimePermissions(this)
        if (missing.isEmpty()) {
            continueAfterPermissions()
        } else {
            permissionLauncher.launch(missing)
        }
    }

    private fun hasPermanentlyDeniedPermission(): Boolean {
        return PermissionUtils.missingRuntimePermissions(this).any { permission ->
            !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
    }

    private fun openAppSettings() {
        val uri = Uri.fromParts("package", packageName, null)
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
    }

    private fun showFallbackUi() {
        if (!fallbackShown) {
            fallbackShown = true
            setContentView(R.layout.activity_permissions_onboarding)
            btnContinue = findViewById<Button>(R.id.btnContinue).apply {
                setOnClickListener { handleContinue() }
            }
            permissionMessage = findViewById(R.id.permissionMessage)
        }
        updateUiState()
    }

    private fun updateUiState() {
        val runtimeGranted = PermissionUtils.hasRuntimePermissions(this)
        permissionMessage?.setText(
            if (runtimeGranted) R.string.perm_runtime_desc else R.string.toast_runtime_denied
        )
        btnContinue?.setText(
            if (openSettingsRequired) R.string.perm_open_settings else R.string.perm_continue
        )
    }
}
