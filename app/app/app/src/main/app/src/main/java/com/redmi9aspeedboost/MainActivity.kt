package com.redmi9aspeedboost

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 40, 32, 32)

        val title = TextView(this)
        title.text = "🚀 Redmi 9A Speed Boost"
        title.textSize = 24f

        val status = TextView(this)
        status.text = "Ready to Boost"
        status.textSize = 18f

        val boostButton = Button(this)
        boostButton.text = "⚡ BOOST PHONE"

        boostButton.setOnClickListener {
            status.text = "✅ Boost completed!"
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(boostButton)

        setContentView(layout)
    }
}
