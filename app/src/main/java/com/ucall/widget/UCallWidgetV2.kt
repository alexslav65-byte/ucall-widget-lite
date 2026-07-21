package com.ucall.widget.lite

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.text.format.DateFormat
import android.util.TypedValue
import android.widget.RemoteViews
import com.ucall.widget.lite.WidgetSettingsActivity.Companion.KEY_SPY_MODE
import com.ucall.widget.lite.WidgetSettingsActivity.Companion.KEY_THEME
import com.ucall.widget.lite.WidgetSettingsActivity.Companion.PREFS_NAME
import com.ucall.widget.lite.WidgetSettingsActivity.Companion.THEME_DARK
import com.ucall.widget.lite.WidgetSettingsActivity.Companion.THEME_STANDARD
import java.util.Date

object UCallWidgetV2 {

    // ======== Observer (як було) ========
    private var observer: CallLogObserver? = null

    fun registerCallObserver(context: Context) {
        unregisterCallObserver()
        observer = CallLogObserver().apply { register(context) }
    }

    fun unregisterCallObserver() {
        observer?.unregister()
        observer = null
    }

    fun updateAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val providerClasses = listOf(
            MyWidgetProvider::class.java,
            MyWideWidgetProvider::class.java
        )
        val callData = LocalCallLogStore.getLastCall(context) // може бути null
        for (providerClass in providerClasses) {
            val ids = manager.getAppWidgetIds(ComponentName(context, providerClass))
            for (id in ids) {
                updateAppWidget(context, manager, id, callData)
            }
        }
    }

    fun getWidgetCount(context: Context): Int {
        val manager = AppWidgetManager.getInstance(context)
        return listOf(
            MyWidgetProvider::class.java,
            MyWideWidgetProvider::class.java
        ).sumOf { providerClass ->
            manager.getAppWidgetIds(ComponentName(context, providerClass)).size
        }
    }

    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        callData: CallData?
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_last_call)

        // 📐 Шрифти
        val (nameTextSizeSp, numberTextSizeSp) = WidgetFontUtils.calcNameNumberSizesSp(context)
        views.setTextViewTextSize(R.id.name_button, TypedValue.COMPLEX_UNIT_SP, nameTextSizeSp)
        views.setTextViewTextSize(R.id.number_button, TypedValue.COMPLEX_UNIT_SP, numberTextSizeSp)

        // 🎨 Тема + spy-mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString(KEY_THEME, THEME_DARK)
        val spyMode = prefs.getBoolean(KEY_SPY_MODE, false)

        // ✅ Завжди стандартний фон "кнопки часу"
        views.setInt(R.id.call_time, "setBackgroundResource", R.drawable.bg_call_time_pill)

        // ======= ДАНІ (тільки нормальний режим) =======
        if (callData == null) {
            views.setTextViewText(R.id.name_button, context.getString(R.string.unknown))
            views.setTextViewText(R.id.number_button, context.getString(R.string.unknown_number))
            views.setTextViewText(R.id.call_time, "--:--")
            views.setImageViewResource(R.id.call_button, R.drawable.call_slash)
            views.setTextColor(R.id.number_button, 0xFFFFFFFF.toInt())
        } else {
            val hasName = callData.contactName.isNotBlank()

            val displayName = callData.contactName
                .takeIf { it.isNotBlank() }
                ?.take(20)
                ?: context.getString(R.string.unknown)

            val displayNumber = when {
                spyMode && hasName -> context.getString(R.string.hidden_number)
                callData.phoneNumber.isNotBlank() -> PhoneFormatter.formatPhoneNumber(callData.phoneNumber)
                else -> context.getString(R.string.unknown_number)
            }

            views.setTextViewText(R.id.name_button, displayName)
            views.setTextViewText(R.id.number_button, displayNumber)

            val iconRes = when (callData.callType) {
                CallLog.Calls.INCOMING_TYPE -> R.drawable.call_arrow_down_left
                CallLog.Calls.OUTGOING_TYPE -> R.drawable.call_arrow_up_right
                CallLog.Calls.MISSED_TYPE,
                CallLog.Calls.REJECTED_TYPE -> R.drawable.call_xmark
                else -> R.drawable.call_slash
            }
            views.setImageViewResource(R.id.call_button, iconRes)

            val timeFormatted = DateFormat.format("HH:mm", Date(callData.timestamp)).toString()
            views.setTextViewText(R.id.call_time, timeFormatted)

            val missedTextColor = 0xFFFFFF00.toInt()
            val normalTextColor = 0xFFFFFFFF.toInt()
            val numberColor =
                if (callData.callType == CallLog.Calls.MISSED_TYPE || callData.callType == CallLog.Calls.REJECTED_TYPE)
                    missedTextColor
                else normalTextColor
            views.setTextColor(R.id.number_button, numberColor)
        }

        // ======= ТЕМА (як було) =======
        when (theme) {
            THEME_STANDARD -> {
                views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_background_blue)
                views.setInt(R.id.text_container, "setBackgroundResource", R.drawable.bg_text_container_blue)
                views.setInt(R.id.call_button_container, "setBackgroundResource", R.drawable.bg_call_button_blue)
            }
            THEME_DARK -> {
                views.setInt(R.id.widget_root, "setBackgroundResource", android.R.color.transparent)
                views.setInt(R.id.text_container, "setBackgroundResource", R.drawable.bg_text_container)
                views.setInt(R.id.call_button_container, "setBackgroundResource", R.drawable.bg_call_button)
            }
            else -> {
                views.setInt(R.id.widget_root, "setBackgroundResource", android.R.color.transparent)
                views.setInt(R.id.text_container, "setBackgroundResource", R.drawable.bg_text_container_light)
                views.setInt(R.id.call_button_container, "setBackgroundResource", R.drawable.bg_call_button_light)
            }
        }

        // ✅ КЛІКИ ЗАВЖДИ
        bindClicks(context, views, appWidgetId, callData)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun bindClicks(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        callData: CallData?
    ) {
        // swipe_zone -> DIAL
        run {
            val intent = Intent(context, WidgetActionActivity::class.java).apply {
                action = WidgetActionReceiver.ACTION_DIAL
                data = Uri.parse("ucall-lite://dial/$appWidgetId?ts=${System.currentTimeMillis()}")
            }
            val pi = PendingIntent.getActivity(
                context,
                10_000 + appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.swipe_zone, pi)
        }

        // text_container -> CALL_LOG
        run {
            val intent = Intent(context, WidgetActionActivity::class.java).apply {
                action = WidgetActionReceiver.ACTION_CALL_LOG
                data = Uri.parse("ucall-lite://calllog/$appWidgetId?ts=${System.currentTimeMillis()}")
            }
            val pi = PendingIntent.getActivity(
                context,
                20_000 + appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.text_container, pi)
        }

        // call_button -> CALL
        run {
            val intent = Intent(context, WidgetActionActivity::class.java).apply {
                action = WidgetActionReceiver.ACTION_CALL
                putExtra(WidgetActionReceiver.EXTRA_NUMBER, callData?.phoneNumber)
                data = Uri.parse("ucall-lite://call/$appWidgetId?ts=${System.currentTimeMillis()}")
            }
            val pi = PendingIntent.getActivity(
                context,
                30_000 + appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.call_button, pi)
        }

        // call_time + settings_hitbox_global -> SETTINGS
        run {
            val intent = Intent(context, WidgetActionActivity::class.java).apply {
                action = WidgetActionReceiver.ACTION_SETTINGS
                data = Uri.parse("ucall-lite://settings/$appWidgetId?ts=${System.currentTimeMillis()}")
            }
            val pi = PendingIntent.getActivity(
                context,
                40_000 + appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.call_time, pi)
            views.setOnClickPendingIntent(R.id.settings_hitbox_global, pi)
        }
    }
}
