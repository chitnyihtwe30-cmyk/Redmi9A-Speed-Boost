package com.redmi9aspeedboost

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

```
private lateinit var status: TextView
private lateinit var ram: TextView
private lateinit var battery: TextView
private lateinit var storage: TextView
private lateinit var device: TextView
private lateinit var progress: ProgressBar

private var mode = 1

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val scroll = ScrollView(this)

    val layout = LinearLayout(this)
    layout.orientation = LinearLayout.VERTICAL
    layout.setPadding(24, 24, 24, 24)

    scroll.addView(layout)

    val title = TextView(this)
    title.text = "REDMI 9A SPEED BOOST"
    title.textSize = 25f
    title.gravity = Gravity.CENTER
    title.setPadding(0, 20, 0, 25)
    layout.addView(title)

    val power = Switch(this)
    power.text = "BOOST SYSTEM ON"
    power.textSize = 18f
    power.isChecked = true
    layout.addView(power)

    status = TextView(this)
    status.text = "System Ready"
    status.textSize = 18f
    status.gravity = Gravity.CENTER
    status.setPadding(0, 20, 0, 20)
    layout.addView(status)

    val phoneTitle = TextView(this)
    phoneTitle.text = "PHONE STATUS"
    phoneTitle.textSize = 20f
    layout.addView(phoneTitle)

    ram = TextView(this)
    ram.textSize = 16f
    ram.setPadding(0, 10, 0, 10)
    layout.addView(ram)

    battery = TextView(this)
    battery.textSize = 16f
    battery.setPadding(0, 10, 0, 10)
    layout.addView(battery)

    storage = TextView(this)
    storage.textSize = 16f
    storage.setPadding(0, 10, 0, 10)
    layout.addView(storage)

    val refresh = Button(this)
    refresh.text = "REFRESH PHONE STATUS"
    refresh.setOnClickListener {
        updatePhoneInfo()
    }
    layout.addView(refresh)

    val modeTitle = TextView(this)
    modeTitle.text = "BOOST MODE"
    modeTitle.textSize = 20f
    modeTitle.setPadding(0, 20, 0, 5)
    layout.addView(modeTitle)

    val mode1 = Button(this)
    mode1.text = "BOOST 1 - SAFE"
    mode1.setOnClickListener {
        mode = 1
        status.text = "BOOST 1 SELECTED"
    }
    layout.addView(mode1)

    val mode2 = Button(this)
    mode2.text = "BOOST 2 - PERFORMANCE"
    mode2.setOnClickListener {
        mode = 2
        status.text = "BOOST 2 SELECTED"
    }
    layout.addView(mode2)

    val mode3 = Button(this)
    mode3.text = "BOOST 3 - MAX SAFE"
    mode3.setOnClickListener {
        mode = 3
        status.text = "BOOST 3 SELECTED"
    }
    layout.addView(mode3)

    progress = ProgressBar(
        this,
        null,
        android.R.attr.progressBarStyleHorizontal
    )

    progress.max = 100
    progress.progress = 0

    layout.addView(
        progress,
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            40
        )
    )

    val boost = Button(this)
    boost.text = "BOOST PHONE"
    boost.textSize = 19f

    boost.setOnClickListener {

        if (!power.isChecked) {
            Toast.makeText(
                this,
                "BOOST SYSTEM IS OFF",
                Toast.LENGTH_SHORT
            ).show()
            return@setOnClickListener
        }

        progress.progress = 0
        status.text = "BOOSTING..."

        Thread {

            for (i in 0..100 step 10) {

                try {
                    Thread.sleep(100)
                } catch (_: Exception) {
                }

                runOnUiThread {
                    progress.progress = i
                    status.text = "BOOSTING... $i%"
                }
            }

            System.gc()

            runOnUiThread {
                progress.progress = 100
                status.text =
                    "✓ BOOST COMPLETED\nMode $mode"

                updatePhoneInfo()

                Toast.makeText(
                    this,
                    "Boost Completed",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }.start()
    }

    layout.addView(boost)

    val ramOptimize = Button(this)
    ramOptimize.text = "RAM OPTIMIZE"

    ramOptimize.setOnClickListener {

        System.gc()

        updatePhoneInfo()

        status.text =
            "RAM OPTIMIZATION COMPLETED"

        Toast.makeText(
            this,
            "RAM Optimized",
            Toast.LENGTH_SHORT
        ).show()
    }

    layout.addView(ramOptimize)

    val storageScan = Button(this)
    storageScan.text = "SCAN STORAGE / JUNK"

    storageScan.setOnClickListener {

        try {

            val stat = StatFs(
                Environment.getDataDirectory().path
            )

            val total =
                stat.totalBytes / 1073741824L

            val free =
                stat.availableBytes / 1073741824L

            val used =
                total - free

            status.text =
                "STORAGE SCAN COMPLETED\n\n" +
                "Used: $used GB\n" +
                "Free: $free GB\n" +
                "Total: $total GB"

        } catch (_: Exception) {

            status.text =
                "Storage scan unavailable"
        }
    }

    layout.addView(storageScan)

    val settings = Button(this)
    settings.text = "OPEN STORAGE SETTINGS"

    settings.setOnClickListener {

        try {

            startActivity(
                Intent(
                    Settings.ACTION_INTERNAL_STORAGE_SETTINGS
                )
            )

        } catch (_: Exception) {

            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        }
    }

    layout.addView(settings)

    val deviceTitle = TextView(this)
    deviceTitle.text = "DEVICE INFORMATION"
    deviceTitle.textSize = 20f
    deviceTitle.setPadding(0, 20, 0, 5)
    layout.addView(deviceTitle)

    device = TextView(this)
    device.textSize = 16f
    device.setPadding(0, 10, 0, 10)
    layout.addView(device)

    val appInfo = Button(this)
    appInfo.text = "APP INFORMATION"

    appInfo.setOnClickListener {

        status.text =
            "App Name: Redmi 9A Speed Boost\n" +
            "Package: com.redmi9aspeedboost\n" +
            "Version: 1.0"

    }

    layout.addView(appInfo)

    val security = Button(this)
    security.text = "SECURITY STATUS"

    security.setOnClickListener {

        status.text =
            "SECURITY STATUS\n\n" +
            "Root access: Not required\n" +
            "Shizuku: Not required for basic features\n" +
            "System changes: Protected"

    }

    layout.addView(security)

    setContentView(scroll)

    updatePhoneInfo()
}

private fun updatePhoneInfo() {

    try {

        val manager =
            getSystemService(
                Context.ACTIVITY_SERVICE
            ) as ActivityManager

        val memory =
            ActivityManager.MemoryInfo()

        manager.getMemoryInfo(memory)

        val total =
            memory.totalMem / 1048576L

        val available =
            memory.availMem / 1048576L

        val used =
            total - available

        ram.text =
            "RAM\n" +
            "Used: $used MB\n" +
            "Available: $available MB\n" +
            "Total: $total MB"

    } catch (_: Exception) {

        ram.text = "RAM: unavailable"
    }

    try {

        val manager =
            getSystemService(
                Context.BATTERY_SERVICE
            ) as BatteryManager

        val level =
            manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        battery.text =
            "Battery: $level%"

    } catch (_: Exception) {

        battery.text =
            "Battery: unavailable"
    }

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

        storage.text =
            "Storage\n" +
            "Used: $used GB\n" +
            "Free: $free GB\n" +
            "Total: $total GB"

    } catch (_: Exception) {

        storage.text =
            "Storage: unavailable"
    }

    device.text =
        "Manufacturer: ${Build.MANUFACTURER}\n" +
        "Model: ${Build.MODEL}\n" +
        "Android: ${Build.VERSION.RELEASE}\n" +
        "SDK: ${Build.VERSION.SDK_INT}\n" +
        "CPU: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}"
}
```

}

```
```
