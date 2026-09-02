package com.redmi9aspeedboost

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this)
        text.text = "REDMI 9A SPEED BOOST\n\nAPP STARTED"
        text.textSize = 24f
        text.setPadding(40, 40, 40, 40)

        setContentView(text)
    }
}
