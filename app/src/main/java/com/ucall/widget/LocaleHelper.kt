package com.ucall.widget.lite

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val KEY_LANG = "ucall_lite_app_lang"

    fun getSavedLang(context: Context): String {
        val prefs = context.getSharedPreferences(WidgetSettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "uk") ?: "uk"
    }

    fun saveLang(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(WidgetSettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    fun wrap(base: Context, lang: String): ContextWrapper {
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        val ctx = base.createConfigurationContext(config)
        return ContextWrapper(ctx)
    }
}
