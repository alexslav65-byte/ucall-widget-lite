package com.ucall.widget.lite

import android.content.Context

object SharedPrefsHelper {
    const val PREFS_NAME = WidgetSettingsActivity.PREFS_NAME
    const val KEY_THEME = WidgetSettingsActivity.KEY_THEME

    fun saveTheme(context: Context, theme: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme)
            .apply()
    }

    fun loadTheme(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, WidgetSettingsActivity.THEME_DARK) ?: WidgetSettingsActivity.THEME_DARK
    }
}
