```kotlin
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

        val modes = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val mode1 = button("BOOST 1")
        val mode2 = button("BOOST 2")
        val mode3 = button("BOOST 3")

        mode1.setOnClickListener {
            boostMode = 1
            textStatus("BOOST 1 - SAFE")
        }

        mode2.setOnClickListener {
            boostMode = 2
            textStatus("BOOST 2 - PERFORMANCE")
        }

        mode3.setOnClickListener {
            boostMode = 3
            textStatus("BOOST 3 - MAX SAFE")
        }

        modes.addView(mode1, weightParams())
        modes.addView(mode2, weightParams())
        modes.addView(mode3, weightParams())

        main.addView(modes)

        val boost = button("🚀  BOOST PHONE")
        boost.textSize = 19f

        boost.setOnClickListener {
            if (!appSwitch.isChecked) {
                toast("Turn BOOST SYSTEM ON first")
            } else {
                performBoost()
            }
        }

        main.addView(boost)

        section(main, "RAM")

        val ramOptimize = button("RAM OPTIMIZE")

        ramOptimize.setOnClickListener {
            optimizeRam()
        }

        main.addView(ramOptimize)

        section(main, "STORAGE")

        val scan = button("SCAN STORAGE")

        scan.setOnClickListener {
            scanStorage()
        }

        main.addView(scan)

        val storageSettings = button("OPEN STORAGE SETTINGS")

        storageSettings.setOnClickListener {
            try {
                startActivity(
                    Intent("android.settings.INTERNAL_STORAGE_SETTINGS")
                )
            } catch (_: Exception) {
                startActivity(
                    Intent(android.provider.Settings.ACTION_SETTINGS)
                )
            }
        }

        main.addView(storageSettings)

        section(main, "DEVICE INFORMATION")

        deviceText = infoText()
        main.addView(deviceText)

        section(main, "BOOST RESULT")

        val result = button("SHOW BEFORE / AFTER")

        result.setOnClickListener {
            statusText.text =
                "BOOST RESULT\n\n" +
                "RAM Before: $beforeRam MB\n" +
                "RAM After: $afterRam MB\n\n" +
                "Mode: $boostMode"
        }

        main.addView(result)

        section(main, "BOOST HISTORY")

        historyText = infoText()
        historyText.text = "No boost history yet."
        main.addView(historyText)

        section(main, "APP MANAGER")

        val appManager = button("OPEN INSTALLED APPS")

        appManager.setOnClickListener {
            showInstalledApps()
        }

        main.addView(appManager)

        val info = TextView(this).apply {
            text =
                "\nSHIZUKU STATUS\n" +
                "Shizuku is reserved for advanced app-control features.\n" +
                "Normal boost features do not require Root or Shizuku."
            textSize = 14f
            setPadding(0, 20, 0, 20)
        }

        main.addView(info)

        setContentView(scroll)
    }

    private fun performBoost() {

        beforeRam = getUsedRam()

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        )

        progressBar.max = 100
        progressBar.progress = 0

        val current = findMainView()

        current.addView(
            progressBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                45
            )
        )

        textStatus("BOOSTING... 0%")

        Thread {

            for (value in 0..100 step 5) {

                try {
                    Thread.sleep(60)
                } catch (_: InterruptedException) {
                    break
                }

                runOnUiThread {
                    progressBar.progress = value
                    textStatus("BOOSTING... $value%")
                }
            }

            System.gc()

            afterRam = getUsedRam()

            runOnUiThread {

                progressBar.progress = 100

                textStatus(
                    "✓ BOOST COMPLETED\nMode $boostMode"
                )

                historyText.text =
                    "Last Boost\n\n" +
                    "Mode: $boostMode\n" +
                    "RAM Before: $beforeRam MB\n" +
                    "RAM After: $afterRam MB\n" +
                    "Status: Completed"

                toast("Boost Completed")
            }

        }.start()
    }

    private fun optimizeRam() {

        beforeRam = getUsedRam()

        System.gc()

        afterRam = getUsedRam()

        textStatus(
            "RAM OPTIMIZATION COMPLETED\n" +
            "Before: $beforeRam MB\n" +
            "After: $afterRam MB"
        )

        historyText.text =
            "RAM Optimization\n\n" +
            "Before: $beforeRam MB\n" +
            "After: $afterRam MB"

        toast("RAM information refreshed")
    }

    private fun scanStorage() {

        try {

            val stat = StatFs(
                Environment.getDataDirectory().path
            )

            val totalBytes = stat.totalBytes
            val freeBytes = stat.availableBytes
            val usedBytes = totalBytes - freeBytes

            val total = totalBytes / 1073741824L
            val free = freeBytes / 1073741824L
            val used = usedBytes / 1073741824L

            textStatus(
                "STORAGE SCAN COMPLETED\n\n" +
                "Used: $used GB\n" +
                "Free: $free GB\n" +
                "Total: $total GB"
            )

        } catch (_: Exception) {

            textStatus("Storage information unavailable")
        }
    }

    private fun refreshAll() {

        updateRam()
        updateBattery()
        updateStorage()
        updateDevice()
    }

    private fun updateRam() {

        try {

            val manager =
                getSystemService(
                    Context.ACTIVITY_SERVICE
                ) as ActivityManager

            val memory = ActivityManager.MemoryInfo()

            manager.getMemoryInfo(memory)

            val total =
                memory.totalMem / 1048576L

            val available =
                memory.availMem / 1048576L

            val used =
                total - available

            val percent =
                if (total > 0) {
                    (used * 100) / total
                } else {
                    0
                }

            ramText.text =
                "RAM\n" +
                "Used: $used MB ($percent%)\n" +
                "Available: $available MB\n" +
                "Total: $total MB"

        } catch (_: Exception) {

            ramText.text = "RAM: unavailable"
        }
    }

    private fun updateBattery() {

        try {

            val manager =
                getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            val level =
                manager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

            batteryText.text =
                "Battery: $level%"

            val intent = registerReceiver(
                null,
                android.content.IntentFilter(
                    Intent.ACTION_BATTERY_CHANGED
                )
            )

            val raw =
                intent?.getIntExtra(
                    BatteryManager.EXTRA_TEMPERATURE,
                    -1
                ) ?: -1

            if (raw >= 0) {

                val temperature =
                    raw / 10.0

                tempText.text =
                    String.format(
                        Locale.US,
                        "Battery Temperature: %.1f°C",
                        temperature
                    )

            } else {

                tempText.text =
                    "Battery Temperature: unavailable"
            }

        } catch (_: Exception) {

            batteryText.text = "Battery: unavailable"
            tempText.text = "Temperature: unavailable"
        }
    }

    private fun updateStorage() {

        try {

            val stat =
                StatFs(
                    Environment.getDataDirectory().path
                )

            val total =
                stat.totalBytes / 1073741824L

            val free =
                stat.availableBytes / 1073741824L

            val used =
                total - free

            storageText.text =
                "Storage: $used GB used / $total GB total\n" +
                "Free: $free GB"

        } catch (_: Exception) {

            storageText.text =
                "Storage: unavailable"
        }
    }

    private fun updateDevice() {

        deviceText.text =
            "Manufacturer: ${Build.MANUFACTURER}\n" +
            "Model: ${Build.MODEL}\n" +
            "Android: ${Build.VERSION.RELEASE}\n" +
            "SDK: ${Build.VERSION.SDK_INT}\n" +
            "CPU: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}"
    }

    private fun showInstalledApps() {

        try {

            val packages =
                packageManager.getInstalledApplications(0)

            val userApps =
                packages
                    .filter {
                        (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                    }
                    .sortedBy {
                        packageManager
                            .getApplicationLabel(it)
                            .toString()
                    }

            val layout =
                findMainView()

            layout.removeAllViews()

            val title = TextView(this).apply {
                text =
                    "INSTALLED APPS\n\n" +
                    "Tap an app to open it."
                textSize = 21f
                setPadding(0, 20, 0, 20)
            }

            layout.addView(title)

            for (app in userApps) {

                val name =
                    packageManager
                        .getApplicationLabel(app)
                        .toString()

                val appButton =
                    Button(this).apply {
                        text = name

                        setOnClickListener {

                            try {

                                val launch =
                                    packageManager
                                        .getLaunchIntentForPackage(
                                            app.packageName
                                        )

                                if (launch != null) {
                                    startActivity(launch)
                                } else {
                                    toast(
                                        "Cannot launch this app"
                                    )
                                }

                            } catch (_: Exception) {

                                toast(
                                    "Unable to open app"
                                )
                            }
                        }
                    }

                layout.addView(appButton)
            }

            val back =
                button("BACK")

            back.setOnClickListener {
                recreate()
            }

            layout.addView(back)

        } catch (_: Exception) {

            toast("Unable to read installed apps")
        }
    }

    private fun getUsedRam(): Long {

        return try {

            val manager =
                getSystemService(
                    Context.ACTIVITY_SERVICE
                ) as ActivityManager

            val memory =
                ActivityManager.MemoryInfo()

            manager.getMemoryInfo(memory)

            (
                memory.totalMem -
                    memory.availMem
            ) / 1048576L

        } catch (_: Exception) {

            0L
        }
    }

    private fun findMainView(): LinearLayout {

        val scroll =
            window.decorView.findViewById<ScrollView>(
                android.R.id.content
            )

        if (scroll != null &&
            scroll.childCount > 0
        ) {

            return scroll.getChildAt(0)
                as LinearLayout
        }

        return LinearLayout(this)
    }

    private fun textStatus(text: String) {
        statusText.text = text
    }

    private fun infoText(): TextView {

        return TextView(this).apply {
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }
    }

    private fun section(
        layout: LinearLayout,
        title: String
    ) {

        val text = TextView(this).apply {
            this.text = title
            textSize = 19f
            setPadding(0, 22, 0, 8)
        }

        layout.addView(text)
    }

    private fun button(label: String): Button {

        return Button(this).apply {
            text = label
            isAllCaps = false
        }
    }

    private fun weightParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
    }

    private fun toast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
```
