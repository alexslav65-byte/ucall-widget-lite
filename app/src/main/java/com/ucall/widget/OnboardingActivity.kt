package com.ucall.widget.lite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val pager = findViewById<ViewPager2>(R.id.pager)
        val dots = findViewById<TextView>(R.id.dotsText)
        val btnNext = findViewById<Button>(R.id.btnNext)

        val settingsMode = intent.getStringExtra(WidgetSettingsActivity.EXTRA_SETTINGS_MODE)
            ?: WidgetSettingsActivity.MODE_BASIC

        val pages = listOf(
            R.drawable.onboarding_1,
            R.drawable.onboarding_2,
            R.drawable.onboarding_3,

        )

        pager.adapter = OnboardingAdapter(pages)

        fun render(position: Int) {
            dots.text = buildDots(position, pages.size)
            btnNext.text = if (position == pages.lastIndex) {
                getString(R.string.onboarding_done)
            } else {
                getString(R.string.onboarding_next)
            }
        }

        render(0)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                render(position)
            }
        })

        btnNext.setOnClickListener {
            val pos = pager.currentItem
            if (pos < pages.lastIndex) {
                pager.currentItem = pos + 1
            } else {
                OnboardingPrefs.markShown(this)

                startActivity(
                    Intent(this, WidgetSettingsActivity::class.java).apply {
                        putExtra(WidgetSettingsActivity.EXTRA_SETTINGS_MODE, WidgetSettingsActivity.MODE_BASIC)
                        putExtra(WidgetSettingsActivity.EXTRA_SHOW_ADD_WIDGET_STEP, true)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )

                finish()
            }
        }
    }

    private fun buildDots(current: Int, total: Int): String {
        return buildString {
            for (i in 0 until total) {
                append(if (i == current) "●" else "○")
                if (i != total - 1) append(" ")
            }
        }
    }
}
