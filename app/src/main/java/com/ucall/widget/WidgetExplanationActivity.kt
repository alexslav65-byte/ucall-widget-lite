package com.ucall.widget.lite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WidgetExplanationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_explanation)
        supportActionBar?.hide()

    }
}
