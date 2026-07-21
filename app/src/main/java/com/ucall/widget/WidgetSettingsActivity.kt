package com.ucall.widget.lite

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class WidgetSettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "ucall_lite_widget_settings"

        const val EXTRA_SETTINGS_MODE = "extra_settings_mode"
        const val MODE_BASIC = "basic"
        const val MODE_FULL = "full"
        const val EXTRA_SHOW_ADD_WIDGET_STEP = "extra_show_add_widget_step"

        const val KEY_FONT_SIZE = "ucall_lite_font_size"
        const val FONT_SMALL = "small"
        const val FONT_MEDIUM = "medium"
        const val FONT_LARGE = "large"

        const val KEY_SETTINGS_LANG = "ucall_lite_settings_lang"
        const val KEY_SPY_MODE = "ucall_lite_spy_mode"

        const val KEY_THEME = "ucall_lite_widget_theme"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_STANDARD = "standard"
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var spyModeSwitch: Switch
    private lateinit var themeGroup: RadioGroup
    private lateinit var fontSizeGroup: RadioGroup
    private lateinit var langGroup: RadioGroup
    private lateinit var diagnosticsText: TextView

    private val runtimePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.all { it }) {
                preloadLastCallForPreviewThenRender()
            } else {
                Toast.makeText(this, getString(R.string.toast_runtime_denied), Toast.LENGTH_LONG).show()
            }
        }

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLang(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        if (PermissionUtils.needsAnyPermission(this)) {
            startActivity(Intent(this, PermissionsOnboardingActivity::class.java).apply {
                putExtra(EXTRA_SETTINGS_MODE, MODE_BASIC)
            })
            finish()
            return
        }

        setContentView(R.layout.activity_widget_settings)

        spyModeSwitch = findViewById(R.id.switch_spy_mode)
        themeGroup = findViewById(R.id.theme_radio_group)
        fontSizeGroup = findViewById(R.id.font_size_group)
        langGroup = findViewById(R.id.lang_group)
        diagnosticsText = findViewById(R.id.txt_refresh_diagnostics)

        findViewById<View>(R.id.btn_widget_help).setOnClickListener {
            startActivity(Intent(this, WidgetExplanationActivity::class.java))
        }

        findViewById<Button>(R.id.btn_add_widget).setOnClickListener {
            requestAddWidgetToHomeScreen()
        }

        findViewById<Button>(R.id.btn_done).setOnClickListener { finish() }

        setupLanguageToggle()
        setupSwitches()
        setupThemeRadios()
        setupFontSizeRadios()
        renderControls()
        preloadLastCallForPreviewThenRender()

        if (intent.getBooleanExtra(EXTRA_SHOW_ADD_WIDGET_STEP, false)) {
            window.decorView.post { showAddWidgetStepDialog() }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing && PermissionUtils.needsAnyPermission(this)) {
            startActivity(Intent(this, PermissionsOnboardingActivity::class.java))
            finish()
            return
        }
        preloadLastCallForPreviewThenRender()
    }

    private fun showAddWidgetStepDialog() {
        startActivity(Intent(this, AddWidgetStepActivity::class.java))
    }

    private fun setupLanguageToggle() {
        val current = LocaleHelper.getSavedLang(this)
        langGroup.setOnCheckedChangeListener(null)
        langGroup.check(if (current == "en") R.id.lang_en else R.id.lang_ua)

        langGroup.setOnCheckedChangeListener { _, checkedId ->
            val newLang = if (checkedId == R.id.lang_en) "en" else "uk"
            if (newLang == LocaleHelper.getSavedLang(this)) return@setOnCheckedChangeListener
            LocaleHelper.saveLang(this, newLang)
            recreate()
        }
    }

    private fun setupSwitches() {
        spyModeSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_SPY_MODE, checked).apply()
            applyChangesEverywhere()
        }
    }

    private fun setupThemeRadios() {
        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.radio_light -> THEME_LIGHT
                R.id.radio_standard -> THEME_STANDARD
                else -> THEME_DARK
            }
            prefs.edit().putString(KEY_THEME, theme).apply()
            applyChangesEverywhere()
        }
    }

    private fun setupFontSizeRadios() {
        fontSizeGroup.setOnCheckedChangeListener { _, checkedId ->
            val size = when (checkedId) {
                R.id.radio_font_small -> FONT_SMALL
                R.id.radio_font_large -> FONT_LARGE
                else -> FONT_MEDIUM
            }
            prefs.edit().putString(KEY_FONT_SIZE, size).apply()
            applyChangesEverywhere()
        }
    }

    private fun renderControls() {
        spyModeSwitch.isChecked = prefs.getBoolean(KEY_SPY_MODE, false)

        themeGroup.check(
            when (prefs.getString(KEY_THEME, THEME_DARK)) {
                THEME_LIGHT -> R.id.radio_light
                THEME_STANDARD -> R.id.radio_standard
                else -> R.id.radio_dark
            }
        )

        fontSizeGroup.check(
            when (prefs.getString(KEY_FONT_SIZE, FONT_MEDIUM)) {
                FONT_SMALL -> R.id.radio_font_small
                FONT_LARGE -> R.id.radio_font_large
                else -> R.id.radio_font_medium
            }
        )
    }

    private fun preloadLastCallForPreviewThenRender() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            runtimePermissionLauncher.launch(PermissionUtils.RUNTIME_PERMS)
            updatePreview()
            return
        }

        UpdateOrchestrator.requestRefreshAndRender(this, "Settings refresh")
        WidgetRefreshScheduler.scheduleIfUseful(this)
        updatePreview()
    }

    private fun updatePreview() {
        val root = findViewById<View>(R.id.widget_preview_host) ?: return
        WidgetPreviewBinder.bind(root, this, prefs)
        updateDiagnostics()
    }

    private fun updateDiagnostics() {
        diagnosticsText.text = RefreshDiagnosticsStore.buildSummary(this)
    }

    private fun applyChangesEverywhere() {
        UpdateOrchestrator.renderWidgetsFromStore(this)
        updatePreview()
    }

    private fun requestAddWidgetToHomeScreen() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val myProvider = ComponentName(this, MyWidgetProvider::class.java)

        var pinRequested = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            pinRequested = appWidgetManager.requestPinAppWidget(myProvider, null, null)
        }

        if (!pinRequested) showManualAddWidgetDialog()
    }

    private fun showManualAddWidgetDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.add_widget_manual_title)
            .setMessage(getString(R.string.add_widget_manual_message))
            .setPositiveButton(R.string.add_widget_manual_button) { _, _ -> openHomeScreen() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openHomeScreen() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        Toast.makeText(this, getString(R.string.add_widget_manual_toast_short), Toast.LENGTH_LONG).show()
    }
}
