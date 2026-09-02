package com.redmi9aspeedboost

import android.app.Activity
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var ramText: TextView
    private lateinit var batteryText: TextView
    private lateinit var tempText: TextView
    private lateinit var rootText: TextView
    private lateinit var progressBar: ProgressBar

    private var appEnabled = true
    private var boostMode = 1

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildUI()
        startAutoRefresh()
    }

    private fun buildUI() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 24)
        root.gravity = Gravity.CENTER_HORIZONTAL

        // TITLE
        val title = TextView(this)
        title.text = "REDMI 9A SPEED BOOST"
        title.textSize = 23f
        title.gravity = Gravity.CENTER
        title.setPadding(0, 10, 0, 20)

        root.addView(title)

        // ON / OFF
        val powerButton = Button(this)
        powerButton.text = "● APP ON"
        powerButton.setOnClickListener {

            appEnabled = !appEnabled

            if (appEnabled) {
                powerButton.text = "● APP ON"
                statusText.text = "System Ready"
                Toast.makeText(this, "Boost System ON", Toast.LENGTH_SHORT).show()
            } else {
                powerButton.text = "○ APP OFF"
                statusText.text = "Boost System OFF"
                Toast.makeText(this, "Boost System OFF", Toast.LENGTH_SHORT).show()
            }
        }

        root.addView(
            powerButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // STATUS
        statusText = TextView(this)
        statusText.text = "System Ready"
        statusText.textSize = 17f
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 15, 0, 15)

        root.addView(statusText)

        // DEVICE STATUS
        val deviceTitle = TextView(this)
        deviceTitle.text = "DEVICE STATUS"
        deviceTitle.textSize = 18f
        deviceTitle.setPadding(0, 15, 0, 8)

        root.addView(deviceTitle)

        ramText = TextView(this)
        batteryText = TextView(this)
        tempText = TextView(this)
        rootText = TextView(this)

        root.addView(ramText)
        root.addView(batteryText)
        root.addView(tempText)
        root.addView(rootText)

        // FAKE ROOT STATUS
        val rootButton = Button(this)
        rootButton.text = "🛡 Fake Root Status"

        rootButton.setOnClickListener {
            rootText.text =
                "🛡 Root Status: Simulation Mode\nActual Root: Not Required"

            Toast.makeText(
                this,
                "Fake Root Status Checked",
                Toast.LENGTH_SHORT
            ).show()
        }

        root.addView(rootButton)

        // BOOST MODES
        val modeTitle = TextView(this)
        modeTitle.text = "PHONE BOOST MODE"
        modeTitle.textSize = 18f
        modeTitle.setPadding(0, 20, 0, 8)

        root.addView(modeTitle)

        val modeLayout = LinearLayout(this)
        modeLayout.orientation = LinearLayout.HORIZONTAL
        modeLayout.gravity = Gravity.CENTER

        val mode1 = Button(this)
        mode1.text = "1"

        val mode2 = Button(this)
        mode2.text = "2"

        val mode3 = Button(this)
        mode3.text = "3"

        mode1.setOnClickListener {
            boostMode = 1
            statusText.text = "Boost Mode 1 Selected"
        }

        mode2.setOnClickListener {
            boostMode = 2
            statusText.text = "Boost Mode 2 Selected"
        }

        mode3.setOnClickListener {
            boostMode = 3
            statusText.text = "Boost Mode 3 Selected"
        }

        modeLayout.addView(mode1)
        modeLayout.addView(mode2)
        modeLayout.addView(mode3)

        root.addView(modeLayout)

        // BOOST BUTTON
        val boostButton = Button(this)
        boostButton.text = "🚀 BOOST PHONE"
        boostButton.textSize = 18f

        boostButton.setOnClickListener {

            if (!appEnabled) {
                Toast.makeText(
                    this,
                    "Please turn APP ON first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            performBoost()
        }

        root.addView(
            boostButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // PROGRESS
       progressBar = ProgressBar(this)
progressBar.isIndeterminate = false
progressBar.max = 100
progressBar.progress = 0
        root.addView(progressBar)

        setContentView(root)
    }

    private fun performBoost() {

        statusText.text = "Boosting... Mode $boostMode"
        progressBar.progress = 0

        Thread {

            for (i in 0..100 step 10) {

                Thread.sleep(120)

                runOnUiThread {
                    progressBar.progress = i
                }
            }

            runOnUiThread {

                statusText.text =
                    "✓ BOOST COMPLETED\nMode $boostMode"

                Toast.makeText(
                    this,
                    "Boost Completed",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }.start()
    }

    private fun startAutoRefresh() {

        handler.post(object : Runnable {

            override fun run() {

                updateDeviceInfo()

                handler.postDelayed(
                    this,
                    2000
                )
            }
        })
    }

    private fun updateDeviceInfo() {

        val memoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryInfo)

        val usedRam =
            memoryInfo.totalPss / 1024

        ramText.text =
            "📊 RAM Usage: ${usedRam} MB"

        batteryText.text =
            "🔋 Battery: System Monitor"

        tempText.text =
            "🌡 CPU Temperature: System Monitor"

        rootText.text =
            "🛡 Root Status: Not Required"
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        super.onDestroy()
    }
}
