package com.ucall.widget.lite

import android.content.Context
import android.content.SharedPreferences
import android.provider.CallLog
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.util.Date

object WidgetPreviewBinder {

    fun bind(root: View, context: Context, prefs: SharedPreferences) {

        // Дані
        val call = LocalCallLogStore.getLastCall(context)

        val nameView   = root.findViewById<TextView>(R.id.name_button)
        val numberView = root.findViewById<TextView>(R.id.number_button)
        val timeView   = root.findViewById<TextView>(R.id.call_time)
        val callIcon   = root.findViewById<ImageView>(R.id.call_button)

        val textContainer = root.findViewById<View>(R.id.text_container)
        val callBtnContainer = root.findViewById<View>(R.id.call_button_container)
        val widgetRoot = root.findViewById<View>(R.id.widget_root)

        // Spy mode
        val spyMode = prefs.getBoolean(WidgetSettingsActivity.KEY_SPY_MODE, false)

        val contactName = call?.contactName.orEmpty()
        val hasName = contactName.isNotBlank()

        val displayName = contactName.takeIf { it.isNotBlank() }?.take(20)
            ?: context.getString(R.string.unknown)

        val displayNumber = when {
            call == null -> context.getString(R.string.unknown_number)
            spyMode && hasName -> context.getString(R.string.hidden_number)
            call.phoneNumber.isNotBlank() -> PhoneFormatter.formatPhoneNumber(call.phoneNumber)
            else -> context.getString(R.string.unknown_number)
        }

        nameView?.text = displayName
        numberView?.text = displayNumber

        // Час
        val ts = call?.timestamp ?: System.currentTimeMillis()
        timeView?.text = DateFormat.format("HH:mm", Date(ts)).toString()

        // Іконка
        val iconRes = when (call?.callType) {
            CallLog.Calls.INCOMING_TYPE -> R.drawable.call_arrow_down_left
            CallLog.Calls.OUTGOING_TYPE -> R.drawable.call_arrow_up_right
            CallLog.Calls.MISSED_TYPE,
            CallLog.Calls.REJECTED_TYPE -> R.drawable.call_xmark
            else -> R.drawable.call_slash
        }
        callIcon?.setImageResource(iconRes)

        // Колір номера як у віджеті
        val missedTextColor = 0xFFFFFF00.toInt()
        val normalTextColor = 0xFFFFFFFF.toInt()
        val isMissed = call?.callType == CallLog.Calls.MISSED_TYPE || call?.callType == CallLog.Calls.REJECTED_TYPE
        numberView?.setTextColor(if (isMissed) missedTextColor else normalTextColor)

        val theme = prefs.getString(WidgetSettingsActivity.KEY_THEME, WidgetSettingsActivity.THEME_DARK)


        val callButtonContainer = root.findViewById<View>(R.id.call_button_container)

        if (theme == WidgetSettingsActivity.THEME_STANDARD) {
            widgetRoot?.setBackgroundResource(R.drawable.widget_background_blue)
            textContainer?.setBackgroundResource(R.drawable.bg_text_container_blue)
            callButtonContainer?.setBackgroundResource(R.drawable.bg_call_button_blue)
        } else {
            val textBg = if (theme == WidgetSettingsActivity.THEME_DARK)
                R.drawable.bg_text_container
            else
                R.drawable.bg_text_container_light

            val buttonBg = if (theme == WidgetSettingsActivity.THEME_DARK)
                R.drawable.bg_call_button
            else
                R.drawable.bg_call_button_light

            widgetRoot?.setBackgroundResource(android.R.color.transparent)
            textContainer?.setBackgroundResource(textBg)
            callButtonContainer?.setBackgroundResource(buttonBg)
        }



        val (nameSp, numberSp) = WidgetFontUtils.calcNameNumberSizesSp(context)
        nameView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, nameSp)
        numberView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, numberSp)
        timeView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, (numberSp * 0.7f).coerceAtLeast(9f))
    }
}
