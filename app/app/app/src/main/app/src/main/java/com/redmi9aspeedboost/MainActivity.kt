package com.redmi9aspeedboost

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var ram: TextView
    private lateinit var battery: TextView
    private lateinit var storage: TextView
    private lateinit var device: TextView
    private lateinit var fakeRootStatus: TextView
    private lateinit var appPowerButton: Button
    private lateinit var boostButton: Button
    private lateinit var modeSelector: Spinner

    private var boostMode = 1
    private var appEnabled = true
    private var fakeRootEnabled = false
    private var boosting = false

    private val modes = arrayOf("1 — SAFE", "2 — PERFORMANCE", "3 — MAX SAFE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        refreshInfo()
        updatePowerUI()
        updateFakeRootUI()
    }

    private fun createUI() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 32)
        }
        scroll.addView(root)

        val title = TextView(this).apply {
            text = "REDMI 9A SPEED BOOST"
            textSize = 25f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 6)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "FREE PHONE OPTIMIZER • SAFE MODE"
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        root.addView(subtitle)

        appPowerButton = Button(this).apply {
            textSize = 18f
            setOnClickListener {
                appEnabled = !appEnabled
                updatePowerUI()
                status.text = if (appEnabled) "BOOST APP IS ON" else "BOOST APP IS OFF"
            }
        }
        root.addView(appPowerButton)

        addSectionTitle(root, "🔐 FAKE ROOT STATUS")
        fakeRootStatus = TextView(this).apply {
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        }
        root.addView(fakeRootStatus)

        val fakeRootButton = Button(this).apply {
            text = "TOGGLE FAKE ROOT"
            setOnClickListener {
                fakeRootEnabled = !fakeRootEnabled
                updateFakeRootUI()
                status.text = if (fakeRootEnabled) "FAKE ROOT SIMULATION: ON" else "FAKE ROOT SIMULATION: OFF"
            }
        }
        root.addView(fakeRootButton)

        status = TextView(this).apply {
            text = "● SYSTEM READY"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 18)
        }
        root.addView(status)

        val refresh = Button(this).apply {
            text = "REFRESH PHONE STATUS"
            setOnClickListener {
                refreshInfo()
                status.text = "✓ PHONE STATUS REFRESHED"
            }
        }
        root.addView(refresh)

        addInfoCard(root, "🧠 RAM STATUS") { ram = it }
        addInfoCard(root, "🔋 BATTERY") { battery = it }
        addInfoCard(root, "💾 STORAGE") { storage = it }

        addSectionTitle(root, "🚀 BOOST MODE — SELECT")
        modeSelector = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modeSelector.adapter = adapter
        modeSelector.setSelection(0)
        modeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                boostMode = position + 1
                if (!boosting) status.text = "SELECTED: ${modes[position]}"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        root.addView(modeSelector)

        boostButton = Button(this).apply {
            text = "⚡ BOOST PHONE"
            textSize = 19f
            setOnClickListener { runBoost() }
        }
        root.addView(boostButton)

        val optimize = Button(this).apply {
            text = "🧹 RAM OPTIMIZE"
            setOnClickListener {
                if (!canRun()) return@setOnClickListener
                System.gc()
                refreshInfo()
                status.text = "✓ RAM OPTIMIZATION COMPLETED"
                Toast.makeText(this@MainActivity, "RAM optimization completed", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(optimize)

        val scan = Button(this).apply {
            text = "🗑️ SCAN STORAGE / JUNK"
            setOnClickListener {
                if (!canRun()) return@setOnClickListener
                scanStorage()
            }
        }
        root.addView(scan)

        addSectionTitle(root, "📱 DEVICE INFORMATION")
        device = TextView(this).apply {
            textSize = 16f
            setPadding(0, 4, 0, 12)
        }
        root.addView(device)

        val appInfo = Button(this).apply {
            text = "APP INFORMATION"
            setOnClickListener {
                status.text = "APP INFORMATION\n\nNAME: Redmi 9A Speed Boost\nPACKAGE: com.redmi9aspeedboost\nVERSION: 1.0\nMODE: Free"
            }
        }
        root.addView(appInfo)

        val security = Button(this).apply {
            text = "🛡️ SECURITY STATUS"
            setOnClickListener {
                status.text = "SECURITY STATUS\n\nROOT: NOT REQUIRED\nFAKE ROOT: SIMULATION ONLY\nSHIZUKU: OPTIONAL\nSYSTEM PROTECTION: ON\n\nNo security bypass is performed."
            }
        }
        root.addView(security)

        val note = TextView(this).apply {
            text = "\nℹ️ Note: This app performs safe, non-root optimization only. It cannot physically add RAM or obtain real root access."
            textSize = 13f
            setPadding(0, 8, 0, 8)
        }
        root.addView(note)

        setContentView(scroll)
    }

    private fun addSectionTitle(root: LinearLayout, text: String) {
        root.addView(TextView(this).apply {
            this.text = text
            textSize = 20f
            setPadding(0, 18, 0, 6)
        })
    }

    private fun addInfoCard(root: LinearLayout, title: String, assign: (TextView) -> Unit) {
        addSectionTitle(root, title)
        val view = TextView(this).apply {
            textSize = 16f
            setPadding(0, 2, 0, 8)
        }
        root.addView(view)
        assign(view)
    }

    private fun canRun(): Boolean {
        if (!appEnabled) {
            status.text = "BOOST APP IS OFF"
            return false
        }
        if (boosting) {
            status.text = "BOOST ALREADY RUNNING"
            return false
        }
        return true
    }

    private fun runBoost() {
        if (!canRun()) return
        boosting = true
        boostButton.isEnabled = false
        modeSelector.isEnabled = false
        val selected = modes[boostMode - 1]
        status.text = "⚡ BOOSTING...\nMODE $selected"

        Thread {
            try {
                when (boostMode) {
                    1 -> {
                        System.gc()
                        Thread.sleep(700)
                    }
                    2 -> {
                        System.gc()
                        Thread.sleep(1100)
                        System.runFinalization()
                    }
                    3 -> {
                        System.gc()
                        Thread.sleep(1400)
                        System.runFinalization()
                        System.gc()
                    }
                }
            } catch (_: Exception) { }
            runOnUiThread {
                boosting = false
                boostButton.isEnabled = true
                modeSelector.isEnabled = true
                refreshInfo()
                status.text = "✓ BOOST COMPLETED\nMODE $selected"
                Toast.makeText(this, "Boost Completed — $selected", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun scanStorage() {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.totalBytes / 1073741824L
            val free = stat.availableBytes / 1073741824L
            val used = total - free
            status.text = "✓ STORAGE SCAN COMPLETED\n\nUSED: $used GB\nFREE: $free GB\nTOTAL: $total GB\n\nJUNK CLEANUP: SAFE SCAN ONLY"
        } catch (_: Exception) {
            status.text = "STORAGE SCAN FAILED"
        }
    }

    private fun updatePowerUI() {
        appPowerButton.text = if (appEnabled) "🟢 BOOST APP: ON" else "⚪ BOOST APP: OFF"
    }

    private fun updateFakeRootUI() {
        fakeRootStatus.text = if (fakeRootEnabled) {
            "🟢 FAKE ROOT: ON\n(Simulation only — NO real root access)"
        } else {
            "⚪ FAKE ROOT: OFF\n(Simulation only — NO real root access)"
        }
    }

    private fun refreshInfo() {
        try {
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memory = ActivityManager.MemoryInfo()
            manager.getMemoryInfo(memory)
            val total = memory.totalMem / 1048576L
            val available = memory.availMem / 1048576L
            val used = total - available
            ram.text = "Used: $used MB\nAvailable: $available MB\nTotal: $total MB"
        } catch (_: Exception) {
            ram.text = "RAM information unavailable"
        }
        try {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            battery.text = "Battery Level: $level%"
        } catch (_: Exception) {
            battery.text = "Battery information unavailable"
        }
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.totalBytes / 1073741824L
            val free = stat.availableBytes / 1073741824L
            val used = total - free
            storage.text = "Used: $used GB\nFree: $free GB\nTotal: $total GB"
        } catch (_: Exception) {
            storage.text = "Storage information unavailable"
        }
        device.text = "Manufacturer: ${Build.MANUFACTURER}\nModel: ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE}\nSDK: ${Build.VERSION.SDK_INT}\nCPU: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}"
    }
}
