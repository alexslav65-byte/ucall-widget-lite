package com.ucall.widget.lite

import android.content.Context

object WidgetFontUtils {

    fun calcNameNumberSizesSp(context: Context): Pair<Float, Float> {
        val res = context.resources
        val prefs = context.getSharedPreferences(
            WidgetSettingsActivity.PREFS_NAME,
            Context.MODE_PRIVATE
        )

        // 🔹 базові розміри з dimen (ті, що ти хочеш вважати "еталоном")
        val density = res.displayMetrics.density
        val fontScale = res.configuration.fontScale

        val baseNameSp = res.getDimension(R.dimen.widget_name_text_size) /
                (density * fontScale)
        val baseNumberSp = res.getDimension(R.dimen.widget_number_text_size) /
                (density * fontScale)

        // 🔹 scale від користувача
        val mode = prefs.getString(WidgetSettingsActivity.KEY_FONT_SIZE,
            WidgetSettingsActivity.FONT_MEDIUM)

        val userScale = when (mode) {
            WidgetSettingsActivity.FONT_SMALL  -> 1.2f
            WidgetSettingsActivity.FONT_LARGE  -> 1.5f
            else                               -> 1.3f   // стандарт трохи більший за "голий" dimen
        }

        val nameTextSizeSp = baseNameSp * userScale
        val numberTextSizeSp = baseNumberSp * userScale

        return nameTextSizeSp to numberTextSizeSp
    }
}
