package com.redmi9aspeedboost

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.BatteryManager
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var ramText: TextView
    private lateinit var batteryText: TextView
    private lateinit var tempText: TextView
    private lateinit var storageText: TextView
    private lateinit var deviceText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var historyText: TextView

    private var boostMode = 1
    private var beforeRam = 0L
    private var afterRam = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
        refreshAll()
    }

    private fun buildUI() {

        val scroll = ScrollView(this)

        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
        }

        scroll.addView(main)

        val title = TextView(this).apply {
            text = "REDMI 9A SPEED BOOST"
            textSize = 25f
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 25)
        }

        main.addView(title)

        val appSwitch = Switch(this).apply {
            text = "BOOST SYSTEM ON"
            textSize = 18f
            isChecked = true
        }

        appSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                textStatus("System Ready")
            } else {
                textStatus("Boost System OFF")
            }
        }

        main.addView(appSwitch)

        statusText = infoText()
        statusText.text = "System Ready"
        statusText.gravity = Gravity.CENTER
        main.addView(statusText)

        section(main, "PHONE STATUS")

        ramText = infoText()
        batteryText = infoText()
        tempText = infoText()
        storageText = infoText()

        main.addView(ramText)
        main.addView(batteryText)
        main.addView(tempText)
        main.addView(storageText)

        val refresh = button("REFRESH STATUS")
        refresh.setOnClickListener {
            refreshAll()
        }
        main.addView(refresh)

        section(main, "BOOST MODE")

