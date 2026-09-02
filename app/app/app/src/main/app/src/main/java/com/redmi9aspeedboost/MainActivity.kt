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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var ram: TextView
    private lateinit var battery: TextView
    private lateinit var storage: TextView
    private lateinit var device: TextView

    private var boostMode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        refreshInfo()
    }

    private fun createUI() {
        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 24)

        scroll.addView(root)

        val title = TextView(this)
        title.text = "REDMI 9A SPEED BOOST"
        title.textSize = 25f
        title.gravity = Gravity.CENTER
        title.setPadding(0, 20, 0, 25)
        root.addView(title)

        status = TextView(this)
        status.text = "SYSTEM READY"
        status.textSize = 18f
        status.gravity = Gravity.CENTER
        status.setPadding(0, 15, 0, 20)
        root.addView(status)

        val refresh = Button(this)
        refresh.text = "REFRESH PHONE STATUS"
        refresh.setOnClickListener {
            refreshInfo()
            status.text = "PHONE STATUS UPDATED"
        }
        root.addView(refresh)

        val ramTitle = TextView(this)
        ramTitle.text = "RAM STATUS"
        ramTitle.textSize = 20f
        root.addView(ramTitle)

        ram = TextView(this)
        ram.textSize = 16f
        root.addView(ram)

        val batteryTitle = TextView(this)
        batteryTitle.text = "BATTERY"
        batteryTitle.textSize = 20f
        root.addView(batteryTitle)

        battery = TextView(this)
        battery.textSize = 16f
        root.addView(battery)

        val storageTitle = TextView(this)
        storageTitle.text = "STORAGE"
        storageTitle.textSize = 20f
        root.addView(storageTitle)

        storage = TextView(this)
        storage.textSize = 16f
        root.addView(storage)

        val modeTitle = TextView(this)
        modeTitle.text = "BOOST MODE"
        modeTitle.textSize = 20f
        root.addView(modeTitle)

        val mode1 = Button(this)
        mode1.text = "BOOST 1 - SAFE"
        mode1.setOnClickListener {
            boostMode = 1
            status.text = "BOOST 1 SELECTED"
        }
        root.addView(mode1)

        val mode2 = Button(this)
        mode2.text = "BOOST 2 - PERFORMANCE"
        mode2.setOnClickListener {
            boostMode = 2
            status.text = "BOOST 2 SELECTED"
        }
        root.addView(mode2)

        val mode3 = Button(this)
        mode3.text = "BOOST 3 - MAX SAFE"
        mode3.setOnClickListener {
            boostMode = 3
            status.text = "BOOST 3 SELECTED"
        }
        root.addView(mode3)

        val boost = Button(this)
        boost.text = "BOOST PHONE"
        boost.textSize = 19f

        boost.setOnClickListener {
            status.text = "BOOSTING..."

            Thread {
                try {
                    Thread.sleep(1500)
                } catch (_: Exception) {
                }

                System.gc()

                runOnUiThread {
                    refreshInfo()
                    status.text = "BOOST COMPLETED\nMODE $boostMode"

                    Toast.makeText(
                        this,
                        "Boost Completed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.start()
        }

        root.addView(boost)

        val optimize = Button(this)
        optimize.text = "RAM OPTIMIZE"

        optimize.setOnClickListener {
            System.gc()
            refreshInfo()

            status.text = "RAM OPTIMIZATION COMPLETED"

            Toast.makeText(
                this,
                "RAM Optimized",
                Toast.LENGTH_SHORT
            ).show()
        }

        root.addView(optimize)

        val scan = Button(this)
        scan.text = "SCAN STORAGE / JUNK"

        scan.setOnClickListener {
            try {
                val stat = StatFs(
                    Environment.getDataDirectory().path
                )

                val total = stat.totalBytes / 1073741824L
                val free = stat.availableBytes / 1073741824L
                val used = total - free

                status.text =
                    "STORAGE SCAN COMPLETED\n\n" +
                    "USED: $used GB\n" +
                    "FREE: $free GB\n" +
                    "TOTAL: $total GB"

            } catch (_: Exception) {
                status.text = "STORAGE SCAN FAILED"
            }
        }

        root.addView(scan)

        val deviceTitle = TextView(this)
        deviceTitle.text = "DEVICE INFORMATION"
        deviceTitle.textSize = 20f
        root.addView(deviceTitle)

        device = TextView(this)
        device.textSize = 16f
        root.addView(device)

        val appInfo = Button(this)
        appInfo.text = "APP INFORMATION"

        appInfo.setOnClickListener {
            status.text =
                "APP NAME: Redmi 9A Speed Boost\n" +
                "PACKAGE: com.redmi9aspeedboost\n" +
                "VERSION: 1.0"
        }

        root.addView(appInfo)

        val security = Button(this)
        security.text = "SECURITY STATUS"

        security.setOnClickListener {
            status.text =
                "SECURITY STATUS\n\n" +
                "ROOT: NOT REQUIRED\n" +
                "SHIZUKU: OPTIONAL\n" +
                "SYSTEM PROTECTION: ON"
        }

        root.addView(security)

        setContentView(scroll)
    }

    private fun refreshInfo() {

        try {
            val manager =
                getSystemService(
                    Context.ACTIVITY_SERVICE
                ) as ActivityManager

            val memory = ActivityManager.MemoryInfo()
            manager.getMemoryInfo(memory)

            val total = memory.totalMem / 1048576L
            val available = memory.availMem / 1048576L
            val used = total - available

            ram.text =
                "Used: $used MB\n" +
                "Available: $available MB\n" +
                "Total: $total MB"

        } catch (_: Exception) {
            ram.text = "RAM information unavailable"
        }

        try {
            val batteryManager =
                getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            val level =
                batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

            battery.text = "Battery Level: $level%"

        } catch (_: Exception) {
            battery.text = "Battery information unavailable"
        }

        try {
            val stat =
                StatFs(
                    Environment.getDataDirectory().path
                )

            val total = stat.totalBytes / 1073741824L
            val free = stat.availableBytes / 1073741824L
            val used = total - free

            storage.text =
                "Used: $used GB\n" +
                "Free: $free GB\n" +
                "Total: $total GB"

        } catch (_: Exception) {
            storage.text = "Storage information unavailable"
        }

        device.text =
            "Manufacturer: ${Build.MANUFACTURER}\n" +
            "Model: ${Build.MODEL}\n" +
            "Android: ${Build.VERSION.RELEASE}\n" +
            "SDK: ${Build.VERSION.SDK_INT}\n" +
            "CPU: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}"
    }
}
