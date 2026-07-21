package com.ucall.widget.lite

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AddWidgetStepActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLang(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_widget_step)

        findViewById<View>(R.id.add_widget_step_root).setOnClickListener {
            finish()
        }
        findViewById<View>(R.id.add_widget_card).setOnClickListener {
            // Keep taps inside the card from closing the dialog-style screen.
        }
        findViewById<Button>(R.id.btn_add_widget_confirm).setOnClickListener {
            requestAddWidgetToHomeScreen()
        }
        findViewById<TextView>(R.id.btn_add_widget_later).setOnClickListener {
            finish()
        }
    }

    private fun requestAddWidgetToHomeScreen() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val myProvider = ComponentName(this, MyWidgetProvider::class.java)

        var pinRequested = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            pinRequested = appWidgetManager.requestPinAppWidget(myProvider, null, null)
        }

        if (pinRequested) {
            finish()
        } else {
            showManualAddWidgetDialog()
        }
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
        finish()
    }
}
