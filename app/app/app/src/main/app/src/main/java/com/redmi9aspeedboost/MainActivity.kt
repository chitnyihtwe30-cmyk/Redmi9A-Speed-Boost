package com.redmi9aspeedboost

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = TextView(this)

        view.text = """
            REDMI 9A SPEED BOOST

            APP STARTED

            Test Version
        """.trimIndent()

        view.textSize = 24f
        view.setPadding(40, 40, 40, 40)

        setContentView(view)
    }
}
