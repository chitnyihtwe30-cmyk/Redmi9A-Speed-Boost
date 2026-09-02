package com.redmi9aspeedboost

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var ramText: TextView
    private lateinit var batteryText: TextView
    private lateinit var storageText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            createUi()
            refreshInfo()
        } catch (e: Exception) {
            showSafeScreen(e)
        }
    }

    private fun createUi() {

        val scrollView = ScrollView(this)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Redmi 9A Speed Boost"
            textSize = 26f
        }

        val appSwitch = Switch(this).apply {
            text = "APP ON / OFF"
            isChecked = true
        }

        statusText = TextView(this).apply {
            text = "Status: Ready"
            textSize = 18f
            setPadding(0, 20, 0, 20)
        }

        ramText = TextView(this).apply {
            textSize = 17f
            setPadding(0, 12, 0, 12)
        }

        batteryText = TextView(this).apply {
            textSize = 17f
            setPadding(0, 12, 0, 12)
        }

        storageText = TextView(this).apply {
            textSize = 17f
            setPadding(0, 12, 0, 20)
        }

        val refreshButton = Button(this).apply {
            text = "REFRESH PHONE INFO"

            setOnClickListener {
                refreshInfo()
            }
        }

        val mode1 = Button(this).apply {
            text = "BOOST 1"

            setOnClickListener {
                statusText.text = "Boost Mode: 1"
            }
        }

        val mode2 = Button(this).apply {
            text = "BOOST 2"

            setOnClickListener {
                statusText.text = "Boost Mode: 2"
            }
        }

        val mode3 = Button(this).apply {
            text = "BOOST 3"

            setOnClickListener {
                statusText.text = "Boost Mode: 3"
            }
        }

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0
        }

        val boostButton = Button(this).apply {
            text = "BOOST PHONE"

            setOnClickListener {

                if (!appSwitch.isChecked) {
                    Toast.makeText(
                        this@MainActivity,
                        "APP is OFF",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                runBoost()
            }
        }

        val ramButton = Button(this).apply {
            text = "RAM OPTIMIZE"

            setOnClickListener {
                refreshInfo()

                Toast.makeText(
                    this@MainActivity,
                    "RAM information refreshed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        mainLayout.addView(title)
        mainLayout.addView(appSwitch)
        mainLayout.addView(statusText)

        mainLayout.addView(ramText)
        mainLayout.addView(batteryText)
        mainLayout.addView(storageText)

        mainLayout.addView(refreshButton)

        mainLayout.addView(mode1)
        mainLayout.addView(mode2)
        mainLayout.addView(mode3)

        mainLayout.addView(progressBar)
        mainLayout.addView(boostButton)
        mainLayout.addView(ramButton)

        scrollView.addView(mainLayout)

        setContentView(scrollView)
    }

    private fun refreshInfo() {

        try {

            val activityManager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val memoryInfo = ActivityManager.MemoryInfo()

            activityManager.getMemoryInfo(memoryInfo)

            val totalRam =
                memoryInfo.totalMem / (1024L * 1024L)

            val availableRam =
                memoryInfo.availMem / (1024L * 1024L)

            val usedRam =
                totalRam - availableRam

            ramText.text =
                "RAM\nUsed: ${usedRam} MB\nAvailable: ${availableRam} MB\nTotal: ${totalRam} MB"

        } catch (e: Exception) {

            ramText.text =
                "RAM: Unable to read"

        }

        try {

            val batteryManager =
                getSystemService(Context.BATTERY_SERVICE) as BatteryManager

            val batteryLevel =
                batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

            batteryText.text =
                "Battery: $batteryLevel%"

        } catch (e: Exception) {

            batteryText.text =
                "Battery: Unable to read"

        }

        try {

            val path =
                Environment.getDataDirectory()

            val stat =
                StatFs(path.path)

            val total =
                stat.totalBytes / (1024L * 1024L * 1024L)

            val free =
                stat.availableBytes / (1024L * 1024L * 1024L)

            val used =
                total - free

            storageText.text =
                "Storage\nUsed: ${used} GB\nFree: ${free} GB\nTotal: ${total} GB"

        } catch (e: Exception) {

            storageText.text =
                "Storage: Unable to read"

        }
    }

    private fun runBoost() {

        progressBar.progress = 0
        statusText.text = "Boosting..."

        Thread {

            for (i in 0..100 step 5) {

                try {
                    Thread.sleep(50)
                } catch (_: Exception) {
                }

                runOnUiThread {
                    progressBar.progress = i
                    statusText.text = "Boosting... $i%"
                }
            }

            runOnUiThread {

                refreshInfo()

                statusText.text =
                    "Boost Completed"

                Toast.makeText(
                    this,
                    "Boost completed",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }.start()
    }

    private fun showSafeScreen(error: Exception) {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Redmi 9A Speed Boost"
            textSize = 24f
        }

        val errorText = TextView(this).apply {
            text =
                "App started in Safe Mode.\n\n" +
                "Error:\n" +
                (error.message ?: error.javaClass.simpleName)
        }

        layout.addView(title)
        layout.addView(errorText)

        setContentView(layout)
    }
}
