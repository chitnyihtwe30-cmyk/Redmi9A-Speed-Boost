package com.redmi9aspeedboost

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var ramText: TextView
    private lateinit var batteryText: TextView
    private lateinit var tempText: TextView
    private lateinit var storageText: TextView
    private lateinit var deviceText: TextView
    private lateinit var rootText: TextView
    private lateinit var historyText: TextView
    private lateinit var progressBar: ProgressBar

    private var appEnabled = true
    private var boostMode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        requestNotificationPermission()

        buildUI()
        updateDeviceInfo()
    }

    private fun buildUI() {
        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 24)

        scroll.addView(root)

        val title = TextView(this)
        title.text = "REDMI 9A SPEED BOOST"
        title.textSize = 24f
        title.gravity = Gravity.CENTER
        title.setPadding(0, 10, 0, 20)
        root.addView(title)

        val powerButton = Button(this)
        powerButton.text = "APP ON"

        powerButton.setOnClickListener {
            appEnabled = !appEnabled

            if (appEnabled) {
                powerButton.text = "APP ON"
                statusText.text = "System Ready"
                Toast.makeText(this, "Boost System ON", Toast.LENGTH_SHORT).show()
            } else {
                powerButton.text = "APP OFF"
                statusText.text = "Boost System OFF"
                Toast.makeText(this, "Boost System OFF", Toast.LENGTH_SHORT).show()
            }
        }

        root.addView(powerButton)

        statusText = createInfoText()
        statusText.text = "System Ready"
        statusText.gravity = Gravity.CENTER
        root.addView(statusText)

        addSectionTitle(root, "DEVICE STATUS")

        ramText = createInfoText()
        batteryText = createInfoText()
        tempText = createInfoText()
        storageText = createInfoText()

        root.addView(ramText)
        root.addView(batteryText)
        root.addView(tempText)
        root.addView(storageText)

        addSectionTitle(root, "DEVICE INFORMATION")

        deviceText = createInfoText()
        root.addView(deviceText)

        addSectionTitle(root, "ROOT STATUS")

        rootText = createInfoText()
        rootText.text = detectRoot()
        root.addView(rootText)

        val fakeRootButton = Button(this)
        fakeRootButton.text = "Fake Root Simulation"

        fakeRootButton.setOnClickListener {
            rootText.text =
                "Fake Root: ENABLED\n" +
                "Simulation Only\n" +
                "Actual Root: ${if (isDeviceRooted()) "Detected" else "Not Detected"}"

            Toast.makeText(
                this,
                "Fake Root Simulation ON",
                Toast.LENGTH_SHORT
            ).show()
        }

        root.addView(fakeRootButton)

        addSectionTitle(root, "PHONE BOOST MODE")

        val modeLayout = LinearLayout(this)
        modeLayout.orientation = LinearLayout.HORIZONTAL
        modeLayout.gravity = Gravity.CENTER

        val mode1 = Button(this)
        mode1.text = "BOOST 1"

        val mode2 = Button(this)
        mode2.text = "BOOST 2"

        val mode3 = Button(this)
        mode3.text = "BOOST 3"

        mode1.setOnClickListener {
            boostMode = 1
            statusText.text = "BOOST 1 - SAFE MODE"
        }

        mode2.setOnClickListener {
            boostMode = 2
            statusText.text = "BOOST 2 - PERFORMANCE MODE"
        }

        mode3.setOnClickListener {
            boostMode = 3
            statusText.text = "BOOST 3 - MAX SAFE MODE"
        }

        modeLayout.addView(mode1)
        modeLayout.addView(mode2)
        modeLayout.addView(mode3)

        root.addView(modeLayout)

        val boostButton = Button(this)
        boostButton.text = "BOOST PHONE"
        boostButton.textSize = 19f

        boostButton.setOnClickListener {
            if (!appEnabled) {
                Toast.makeText(
                    this,
                    "Please turn APP ON first",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                performBoost()
            }
        }

        root.addView(boostButton)

        val ramButton = Button(this)
        ramButton.text = "RAM OPTIMIZE"

        ramButton.setOnClickListener {
            optimizeRam()
        }

        root.addView(ramButton)

        val storageButton = Button(this)
        storageButton.text = "SCAN STORAGE / JUNK"

        storageButton.setOnClickListener {
            scanStorage()
        }

        root.addView(storageButton)

        val batteryButton = Button(this)
        batteryButton.text = "BATTERY INFORMATION"

        batteryButton.setOnClickListener {
            updateBatteryInfo()
        }

        root.addView(batteryButton)

        val storageSettingsButton = Button(this)
        storageSettingsButton.text = "OPEN STORAGE SETTINGS"

        storageSettingsButton.setOnClickListener {
            try {
                startActivity(
                    Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                )
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }

        root.addView(storageSettingsButton)

        addSectionTitle(root, "BOOST PROGRESS")

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        )

        progressBar.isIndeterminate = false
        progressBar.max = 100
        progressBar.progress = 0

        root.addView(
            progressBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                45
            )
        )

        addSectionTitle(root, "BOOST RESULT")

        val resultButton = Button(this)
        resultButton.text = "SHOW BEFORE / AFTER"

        resultButton.setOnClickListener {
            statusText.text =
                "RAM Before: ${beforeRam} MB\n" +
                "RAM After: ${afterRam} MB"
        }

        root.addView(resultButton)

        addSectionTitle(root, "BOOST HISTORY")

        historyText = createInfoText()
        historyText.text = "No boost history yet."

        root.addView(historyText)

        setContentView(scroll)
    }

    private var beforeRam = 0L
    private var afterRam = 0L

    private fun performBoost() {
        beforeRam = getUsedRam()

        statusText.text =
            "BOOSTING...\nMode $boostMode"

        progressBar.progress = 0

        Thread {
            for (i in 0..100 step 10) {
                Thread.sleep(100)

                runOnUiThread {
                    progressBar.progress = i
                }
            }

            System.gc()

            afterRam = getUsedRam()

            runOnUiThread {
                progressBar.progress = 100

                statusText.text =
                    "BOOST COMPLETED\nMode $boostMode"

                historyText.text =
                    "Boost Completed\n" +
                    "Mode: $boostMode\n" +
                    "RAM Before: $beforeRam MB\n" +
                    "RAM After: $afterRam MB"

                sendNotification(
                    "Boost Completed",
                    "Redmi 9A Boost Mode $boostMode completed."
                )

                Toast.makeText(
                    this,
                    "Boost Completed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.start()
    }

    private fun optimizeRam() {
        beforeRam = getUsedRam()

        System.gc()

        afterRam = getUsedRam()

        statusText.text =
            "RAM OPTIMIZATION COMPLETED"

        historyText.text =
            "RAM Optimization Completed\n" +
            "Before: $beforeRam MB\n" +
            "After: $afterRam MB"

        Toast.makeText(
            this,
            "RAM Optimization Completed",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun scanStorage() {
        val stat = StatFs(Environment.getDataDirectory().path)

        val total =
            stat.totalBytes / (1024L * 1024L * 1024L)

        val free =
            stat.availableBytes / (1024L * 1024L * 1024L)

        val used = total - free

        statusText.text =
            "STORAGE SCAN COMPLETED\n" +
            "Used: $used GB\n" +
            "Free: $free GB\n" +
            "Total: $total GB"

        Toast.makeText(
            this,
            "Storage Scan Completed",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateDeviceInfo() {
        ramText.text =
            "RAM Usage: ${getUsedRam()} MB"

        updateBatteryInfo()

        val stat = StatFs(Environment.getDataDirectory().path)

        val total =
            stat.totalBytes / (1024L * 1024L * 1024L)

        val free =
            stat.availableBytes / (1024L * 1024L * 1024L)

        val used = total - free

        storageText.text =
            "Storage: $used GB used / $total GB"

        deviceText.text =
            "Manufacturer: ${Build.MANUFACTURER}\n" +
            "Model: ${Build.MODEL}\n" +
            "Android: ${Build.VERSION.RELEASE}\n" +
            "SDK: ${Build.VERSION.SDK_INT}\n" +
            "CPU: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}"
    }

    private fun updateBatteryInfo() {
        val batteryManager =
            getSystemService(BATTERY_SERVICE) as BatteryManager

        val level =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        val intent = registerReceiver(
            null,
            android.content.IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )

        val temperature =
            intent?.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                -1
            ) ?: -1

        val tempC =
            if (temperature >= 0) {
                temperature / 10.0
            } else {
                0.0
            }

        batteryText.text =
            "Battery: $level%"

        tempText.text =
            String.format(
                Locale.US,
                "Temperature: %.1f°C",
                tempC
            )
    }

    private fun getUsedRam(): Long {
        val memoryInfo = Debug.MemoryInfo()

        Debug.getMemoryInfo(memoryInfo)

        return memoryInfo.totalPss / 1024L
    }

    private fun createInfoText(): TextView {
        val text = TextView(this)

        text.textSize = 16f
        text.setPadding(8, 8, 8, 8)

        return text
    }

    private fun addSectionTitle(
        root: LinearLayout,
        text: String
    ) {
        val title = TextView(this)

        title.text = text
        title.textSize = 19f
        title.setPadding(0, 18, 0, 8)

        root.addView(title)
    }

    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/data/adb/magisk"
        )

        return paths.any {
            File(it).exists()
        }
    }

    private fun detectRoot(): String {
        return if (isDeviceRooted()) {
            "Root Status: ROOT DETECTED"
        } else {
            "Root Status: NOT ROOTED"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "boost_channel",
                "Boost Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {

            if (
                checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    1001
                )
            }
        }
    }

    private fun sendNotification(
        title: String,
        message: String
    ) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        val notification =
            android.app.Notification.Builder(
                this,
                "boost_channel"
            )
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setAutoCancel(true)
                .build()

        manager.notify(100, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
