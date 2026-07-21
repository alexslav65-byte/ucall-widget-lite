package com.ucall.widget.lite

import android.content.Context

object OnboardingPrefs {
    private const val PREFS = "ucall_lite_onboarding"
    private const val KEY_SHOWN = "ucall_lite_onboarding_shown"

    fun isShown(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOWN, false)
    }

    fun markShown(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOWN, true)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
