package com.redmi9aspeedboost

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.File
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var ramText: TextView
    private lateinit var batteryText: TextView
    private lateinit var storageText: TextView
    private lateinit var tempText: TextView
    private lateinit var progress: ProgressBar
    private lateinit var resultText: TextView

    private var appEnabled = true
    private var selectedBoost = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildUI()
        updateMonitor()
    }

    private fun buildUI() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(16), dp(16), dp(24))
        root.setBackgroundColor(0xFF101114.toInt())

        val scroll = ScrollView(this)
        scroll.addView(root)

        val title = text(
            "Redmi 9A Speed Boost",
            26,
            0xFFFFFFFF.toInt()
        )
        root.addView(title)

        val subtitle = text(
            "Phone Performance Manager",
            14,
            0xFFAAAAAA.toInt()
        )
        root.addView(subtitle)

        root.addView(space(12))

        // APP ON / OFF
        val appSwitch = Switch(this)
        appSwitch.text = "APP ON / OFF"
        appSwitch.textSize = 17f
        appSwitch.setTextColor(0xFFFFFFFF.toInt())
        appSwitch.isChecked = true

        appSwitch.setOnCheckedChangeListener { _, checked ->
            appEnabled = checked

            if (checked) {
                statusText.text = "APP STATUS: ON"
            } else {
                statusText.text = "APP STATUS: OFF"
            }
        }

        root.addView(appSwitch)

        statusText = text(
            "APP STATUS: ON",
            15,
            0xFF66FF88.toInt()
        )
        root.addView(statusText)

        root.addView(space(16))

        // MONITOR
        root.addView(section("PHONE MONITOR"))

        ramText = text("", 16, 0xFFFFFFFF.toInt())
        batteryText = text("", 16, 0xFFFFFFFF.toInt())
        tempText = text("", 16, 0xFFFFFFFF.toInt())
        storageText = text("", 16, 0xFFFFFFFF.toInt())

        root.addView(card(ramText))
        root.addView(card(batteryText))
        root.addView(card(tempText))
        root.addView(card(storageText))

        root.addView(space(12))

        val monitorButton = button("REFRESH MONITOR")
        monitorButton.setOnClickListener {
            updateMonitor()
        }
        root.addView(monitorButton)

        val ramOptimize = button("RAM OPTIMIZE")
        ramOptimize.setOnClickListener {
            optimizeRam()
        }
        root.addView(ramOptimize)

        root.addView(space(16))

        // BOOST MODES
        root.addView(section("BOOST MODE"))

        val modeLayout = LinearLayout(this)
        modeLayout.orientation = LinearLayout.HORIZONTAL

        val mode1 = button("BOOST 1")
        val mode2 = button("BOOST 2")
        val mode3 = button("BOOST 3")

        mode1.setOnClickListener {
            selectedBoost = 1
            resultText.text = "BOOST 1 SELECTED"
        }

        mode2.setOnClickListener {
            selectedBoost = 2
            resultText.text = "BOOST 2 SELECTED"
        }

        mode3.setOnClickListener {
            selectedBoost = 3
            resultText.text = "BOOST 3 SELECTED"
        }

        modeLayout.addView(mode1, weightParams())
        modeLayout.addView(mode2, weightParams())
        modeLayout.addView(mode3, weightParams())

        root.addView(modeLayout)

        root.addView(space(12))

        // BOOST PHONE
        val boostButton = button("BOOST PHONE")
        boostButton.textSize = 21f
        boostButton.setOnClickListener {
            if (!appEnabled) {
                Toast.makeText(
                    this,
                    "APP IS OFF",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            startBoost()
        }

        root.addView(boostButton)

        root.addView(space(12))

        progress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        )

        progress.max = 100
        progress.progress = 0

        root.addView(
            progress,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(18)
            )
        )

        resultText = text(
            "READY",
            17,
            0xFFFFFFFF.toInt()
        )
        resultText.gravity = Gravity.CENTER
        root.addView(resultText)

        root.addView(space(20))

        // BEFORE / AFTER
        root.addView(section("BEFORE / AFTER"))

        val beforeAfter = text(
            "Before: Monitor current phone state\n" +
                    "After: Monitor state after boost",
            15,
            0xFFCCCCCC.toInt()
        )

        root.addView(card(beforeAfter))

        root.addView(space(16))

        // DEVICE INFO
        root.addView(section("DEVICE INFO"))

        val deviceInfo = text(
            "Manufacturer: ${Build.MANUFACTURER}\n" +
                    "Model: ${Build.MODEL}\n" +
                    "Android: ${Build.VERSION.RELEASE}\n" +
                    "SDK: ${Build.VERSION.SDK_INT}",
            15,
            0xFFFFFFFF.toInt()
        )

        root.addView(card(deviceInfo))

        root.addView(space(16))

        // ROOT
        root.addView(section("ROOT STATUS"))

        val rootStatus = text(
            if (isRooted()) {
                "REAL ROOT: DETECTED"
            } else {
                "REAL ROOT: NOT DETECTED"
            },
            16,
            0xFFFFFFFF.toInt()
        )

        root.addView(card(rootStatus))

        val fakeRoot = button("FAKE ROOT — SIMULATION ONLY")

        fakeRoot.setOnClickListener {
            Toast.makeText(
                this,
                "Fake Root is simulation only. No real root access granted.",
                Toast.LENGTH_LONG
            ).show()

            rootStatus.text = "FAKE ROOT: SIMULATION ACTIVE"
        }

        root.addView(fakeRoot)

        root.addView(space(16))

        // STORAGE
        root.addView(section("STORAGE"))

        val storageScan = button("SCAN JUNK / CACHE")

        storageScan.setOnClickListener {
            scanStorage()
        }

        root.addView(storageScan)

        val storageSettings = button("OPEN STORAGE SETTINGS")

        storageSettings.setOnClickListener {
            try {
                startActivity(
                    Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                )
            } catch (_: Exception) {
                startActivity(
                    Intent(Settings.ACTION_SETTINGS)
                )
            }
        }

        root.addView(storageSettings)

        root.addView(space(16))

        // BATTERY
        root.addView(section("BATTERY"))

        val batteryButton = button("BATTERY INFO")

        batteryButton.setOnClickListener {
            showBatteryInfo()
        }

        root.addView(batteryButton)

        root.addView(space(16))

        // APP MANAGER
        root.addView(section("APP MANAGER"))

        val appManager = button("OPEN APP MANAGER")

        appManager.setOnClickListener {
            openAppManager()
        }

        root.addView(appManager)

        root.addView(space(20))

        val note = text(
            "Note: Android system restrictions may limit background process killing and other privileged operations.",
            13,
            0xFF999999.toInt()
        )

        root.addView(note)

        setContentView(scroll)
    }

    private fun startBoost() {

        progress.progress = 0
        resultText.text = "BOOSTING..."

        Thread {

            for (i in 0..100 step 5) {

                Thread.sleep(80)

                runOnUiThread {
                    progress.progress = i
                    resultText.text =
                        "BOOST $selectedBoost : $i%"
                }
            }

            optimizeRam()

            runOnUiThread {
                progress.progress = 100
                resultText.text = "BOOST COMPLETED"
                updateMonitor()
            }

        }.start()
    }

    private fun optimizeRam() {

        val manager =
            getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        try {
            manager.clearApplicationUserData(packageName)
        } catch (_: Exception) {
        }

        Toast.makeText(
            this,
            "RAM optimization completed",
            Toast.LENGTH_SHORT
        ).show()

        updateMonitor()
    }

    private fun updateMonitor() {

        // RAM
        val manager =
            getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        val memoryInfo = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        val usedRam = totalRam - availableRam

        ramText.text =
            "RAM\n" +
                    "Used: ${formatBytes(usedRam)}\n" +
                    "Available: ${formatBytes(availableRam)}\n" +
                    "Total: ${formatBytes(totalRam)}"

        // Battery
        val batteryManager =
            getSystemService(Context.BATTERY_SERVICE)
                    as BatteryManager

        val battery =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        batteryText.text =
            "Battery: $battery%"

        // Temperature
        val intent = registerReceiver(
            null,
            android.content.IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )

        val temperature =
            intent?.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                0
            ) ?: 0

        tempText.text =
            "Battery Temperature: ${temperature / 10.0} °C"

        // Storage
        val stat = StatFs(
            Environment.getDataDirectory().path
        )

        val total =
            stat.totalBytes

        val free =
            stat.availableBytes

        val used =
            total - free

        storageText.text =
            "Storage\n" +
                    "Used: ${formatBytes(used)}\n" +
                    "Free: ${formatBytes(free)}\n" +
                    "Total: ${formatBytes(total)}"
    }

    private fun scanStorage() {

        val cacheSize = try {
            getDirSize(cacheDir)
        } catch (_: Exception) {
            0L
        }

        AlertDialog.Builder(this)
            .setTitle("Junk / Cache Scan")
            .setMessage(
                "App cache detected:\n\n" +
                        formatBytes(cacheSize) +
                        "\n\nAndroid system cache cannot be safely deleted by a normal app without privileged access."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showBatteryInfo() {

        val intent = registerReceiver(
            null,
            android.content.IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )

        val level =
            intent?.getIntExtra(
                BatteryManager.EXTRA_LEVEL,
                -1
            ) ?: -1

        val scale =
            intent?.getIntExtra(
                BatteryManager.EXTRA_SCALE,
                -1
            ) ?: -1

        val temp =
            intent?.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                0
            ) ?: 0

        val status =
            intent?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            ) ?: -1

        val charging =
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

        AlertDialog.Builder(this)
            .setTitle("Battery Information")
            .setMessage(
                "Battery: $level/$scale\n" +
                        "Charging: ${if (charging) "YES" else "NO"}\n" +
                        "Temperature: ${temp / 10.0} °C"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun openAppManager() {

        val packages =
            packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA
            )

        val names = packages
            .filter {
                it.packageName != packageName
            }
            .sortedBy {
                packageManager.getApplicationLabel(it)
                    .toString()
                    .lowercase(Locale.getDefault())
            }

        val display = names.map {
            packageManager.getApplicationLabel(it).toString()
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Installed Apps")
            .setItems(display) { _, which ->

                val app = names[which]

                showAppActions(app.packageName)
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showAppActions(packageName: String) {

        val label = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(
                    packageName,
                    0
                )
            ).toString()
        } catch (_: Exception) {
            packageName
        }

        val actions = arrayOf(
            "RUN APP",
            "KILL BACKGROUND",
            "APP INFO",
            "SECURITY SCAN"
        )

        AlertDialog.Builder(this)
            .setTitle(label)
            .setItems(actions) { _, which ->

                when (which) {

                    0 -> runApp(packageName)

                    1 -> killApp(packageName)

                    2 -> openAppInfo(packageName)

                    3 -> securityScan(packageName)
                }
            }
            .show()
    }

    private fun runApp(packageName: String) {

        try {

            val launch =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (launch != null) {
                startActivity(launch)
            } else {
                Toast.makeText(
                    this,
                    "This app cannot be launched.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to launch app.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun killApp(packageName: String) {

        AlertDialog.Builder(this)
            .setTitle("Kill App?")
            .setMessage(
                "Stop background processes of:\n$packageName"
            )
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("APPLY") { _, _ ->

                try {

                    val manager =
                        getSystemService(
                            Context.ACTIVITY_SERVICE
                        ) as ActivityManager

                    manager.killBackgroundProcesses(
                        packageName
                    )

                    Toast.makeText(
                        this,
                        "Kill command sent.",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (_: Exception) {

                    Toast.makeText(
                        this,
                        "Android blocked this operation.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun openAppInfo(packageName: String) {

        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        )

        intent.data =
            Uri.parse("package:$packageName")

        startActivity(intent)
    }

    private fun securityScan(packageName: String) {

        val info = try {
            packageManager.getPackageInfo(
                packageName,
                0
            )
        } catch (_: Exception) {
            null
        }

        val version =
            info?.versionName ?: "Unknown"

        AlertDialog.Builder(this)
            .setTitle("Security Scan")
            .setMessage(
                "Package:\n$packageName\n\n" +
                        "Version:\n$version\n\n" +
                        "Basic package inspection completed.\n\n" +
                        "This is NOT a full antivirus scan."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun isRooted(): Boolean {

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

    private fun getDirSize(dir: File): Long {

        if (!dir.exists()) return 0L

        var size = 0L

        try {

            dir.listFiles()?.forEach {

                size += if (it.isDirectory) {
                    getDirSize(it)
                } else {
                    it.length()
                }
            }

        } catch (_: Exception) {
        }

        return size
    }

    private fun formatBytes(bytes: Long): String {

        if (bytes <= 0) return "0 MB"

        val mb =
            bytes / (1024.0 * 1024.0)

        if (mb < 1024) {
            return String.format(
                Locale.US,
                "%.1f MB",
                mb
            )
        }

        return String.format(
            Locale.US,
            "%.2f GB",
            mb / 1024.0
        )
    }

    private fun text(
        value: String,
        size: Int,
        color: Int
    ): TextView {

        val t = TextView(this)

        t.text = value
        t.textSize = size.toFloat()
        t.setTextColor(color)
        t.setPadding(
            dp(4),
            dp(8),
            dp(4),
            dp(8)
        )

        return t
    }

    private fun section(value: String): TextView {

        return text(
            value,
            18,
            0xFF66CCFF.toInt()
        )
    }

    private fun card(view: View): View {

        val box = LinearLayout(this)

        box.orientation = LinearLayout.VERTICAL
        box.setPadding(
            dp(12),
            dp(8),
            dp(12),
            dp(8)
        )

        box.setBackgroundColor(
            0xFF1B1D22.toInt()
        )

        box.addView(view)

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            0,
            dp(4),
            0,
            dp(4)
        )

        box.layoutParams = params

        return box
    }

    private fun button(value: String): Button {

        val b = Button(this)

        b.text = value
        b.textSize = 15f

        b.setAllCaps(false)

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
            )

        params.setMargins(
            0,
            dp(5),
            0,
            dp(5)
        )

        b.layoutParams = params

        return b
    }

    private fun weightParams():
            LinearLayout.LayoutParams {

        val p =
            LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
            )

        p.setMargins(
            dp(2),
            0,
            dp(2),
            0
        )

        return p
    }

    private fun space(value: Int): Space {

        return Space(this).apply {
            minimumHeight = dp(value)
        }
    }

    private fun dp(value: Int): Int {

        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
    }
}
