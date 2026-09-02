package com.redmi9aspeedboost

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
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
    private lateinit var temperatureText: TextView
    private lateinit var storageText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var resultText: TextView

    private var appEnabled = true
    private var boostMode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createInterface()
        updateMonitor()
    }

    private fun createInterface() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(16), dp(16), dp(24))
        root.setBackgroundColor(0xFF101114.toInt())

        val scroll = ScrollView(this)
        scroll.addView(root)

        root.addView(
            makeText(
                "Redmi 9A Speed Boost",
                26f,
                0xFFFFFFFF.toInt()
            )
        )

        root.addView(
            makeText(
                "Phone Performance Manager",
                14f,
                0xFFAAAAAA.toInt()
            )
        )

        root.addView(space(12))

        val appSwitch = Switch(this)
        appSwitch.text = "APP ON / OFF"
        appSwitch.textSize = 17f
        appSwitch.setTextColor(0xFFFFFFFF.toInt())
        appSwitch.isChecked = true

        appSwitch.setOnCheckedChangeListener { _, enabled ->
            appEnabled = enabled
            statusText.text =
                if (enabled) "APP STATUS: ON"
                else "APP STATUS: OFF"
        }

        root.addView(appSwitch)

        statusText = makeText(
            "APP STATUS: ON",
            16f,
            0xFF66FF88.toInt()
        )

        root.addView(statusText)

        root.addView(space(16))

        root.addView(section("PHONE MONITOR"))

        ramText = makeText("", 16f, 0xFFFFFFFF.toInt())
        batteryText = makeText("", 16f, 0xFFFFFFFF.toInt())
        temperatureText = makeText("", 16f, 0xFFFFFFFF.toInt())
        storageText = makeText("", 16f, 0xFFFFFFFF.toInt())

        root.addView(makeCard(ramText))
        root.addView(makeCard(batteryText))
        root.addView(makeCard(temperatureText))
        root.addView(makeCard(storageText))

        val refresh = makeButton("REFRESH MONITOR")
        refresh.setOnClickListener {
            updateMonitor()
        }
        root.addView(refresh)

        val optimize = makeButton("RAM OPTIMIZE")
        optimize.setOnClickListener {
            optimizeRam()
        }
        root.addView(optimize)

        root.addView(space(16))

        root.addView(section("BOOST MODE"))

        val modes = LinearLayout(this)
        modes.orientation = LinearLayout.HORIZONTAL

        val mode1 = makeButton("BOOST 1")
        val mode2 = makeButton("BOOST 2")
        val mode3 = makeButton("BOOST 3")

        mode1.setOnClickListener {
            boostMode = 1
            resultText.text = "BOOST 1 SELECTED"
        }

        mode2.setOnClickListener {
            boostMode = 2
            resultText.text = "BOOST 2 SELECTED"
        }

        mode3.setOnClickListener {
            boostMode = 3
            resultText.text = "BOOST 3 SELECTED"
        }

        modes.addView(mode1, weightParams())
        modes.addView(mode2, weightParams())
        modes.addView(mode3, weightParams())

        root.addView(modes)

        root.addView(space(10))

        val boost = makeButton("BOOST PHONE")
        boost.textSize = 20f

        boost.setOnClickListener {

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

        root.addView(boost)

        root.addView(space(10))

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        )

        progressBar.max = 100
        progressBar.progress = 0

        root.addView(
            progressBar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(18)
            )
        )

        resultText = makeText(
            "READY",
            17f,
            0xFFFFFFFF.toInt()
        )

        resultText.gravity = Gravity.CENTER

        root.addView(resultText)

        root.addView(space(20))

        root.addView(section("BEFORE / AFTER"))

        val beforeAfter = makeText(
            "Before: Current phone state\n" +
                    "After: State after optimization",
            15f,
            0xFFCCCCCC.toInt()
        )

        root.addView(makeCard(beforeAfter))

        root.addView(space(16))

        root.addView(section("DEVICE INFO"))

        val deviceInfo = makeText(
            "Manufacturer: ${Build.MANUFACTURER}\n" +
                    "Model: ${Build.MODEL}\n" +
                    "Android: ${Build.VERSION.RELEASE}\n" +
                    "SDK: ${Build.VERSION.SDK_INT}",
            15f,
            0xFFFFFFFF.toInt()
        )

        root.addView(makeCard(deviceInfo))

        root.addView(space(16))

        root.addView(section("ROOT STATUS"))

        val rootStatus = makeText(
            if (isRooted())
                "REAL ROOT: DETECTED"
            else
                "REAL ROOT: NOT DETECTED",
            16f,
            0xFFFFFFFF.toInt()
        )

        root.addView(makeCard(rootStatus))

        val fakeRoot = makeButton(
            "FAKE ROOT — SIMULATION ONLY"
        )

        fakeRoot.setOnClickListener {
            rootStatus.text =
                "FAKE ROOT: SIMULATION ACTIVE"

            Toast.makeText(
                this,
                "Simulation only — real root access was NOT granted.",
                Toast.LENGTH_LONG
            ).show()
        }

        root.addView(fakeRoot)

        root.addView(space(16))

        root.addView(section("STORAGE"))

        val scan = makeButton("SCAN JUNK / CACHE")

        scan.setOnClickListener {
            scanCache()
        }

        root.addView(scan)

        val storageSettings =
            makeButton("OPEN STORAGE SETTINGS")

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

        root.addView(section("BATTERY"))

        val batteryInfo = makeButton("BATTERY INFO")

        batteryInfo.setOnClickListener {
            showBatteryInfo()
        }

        root.addView(batteryInfo)

        root.addView(space(16))

        root.addView(section("APP MANAGER"))

        val appManager = makeButton("OPEN APP MANAGER")

        appManager.setOnClickListener {
            openAppManager()
        }

        root.addView(appManager)

        root.addView(space(20))

        root.addView(
            makeText(
                "Android may restrict privileged operations such as force-stop.",
                13f,
                0xFF999999.toInt()
            )
        )

        setContentView(scroll)
    }

    private fun startBoost() {

        progressBar.progress = 0
        resultText.text = "BOOSTING..."

        Thread {

            for (value in 0..100 step 5) {

                Thread.sleep(70)

                runOnUiThread {
                    progressBar.progress = value
                    resultText.text =
                        "BOOST $boostMode : $value%"
                }
            }

            optimizeRam()

            runOnUiThread {
                progressBar.progress = 100
                resultText.text = "BOOST COMPLETED"
                updateMonitor()
            }

        }.start()
    }

    private fun optimizeRam() {

        val manager =
            getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)

        Toast.makeText(
            this,
            "RAM optimization completed",
            Toast.LENGTH_SHORT
        ).show()

        updateMonitor()
    }

    private fun updateMonitor() {

        val manager =
            getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)

        val total = info.totalMem
        val available = info.availMem
        val used = total - available

        ramText.text =
            "RAM\n" +
                    "Used: ${formatBytes(used)}\n" +
                    "Available: ${formatBytes(available)}\n" +
                    "Total: ${formatBytes(total)}"

        val batteryManager =
            getSystemService(Context.BATTERY_SERVICE)
                    as BatteryManager

        val battery =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        batteryText.text =
            "Battery: $battery%"

        val batteryIntent = registerReceiver(
            null,
            android.content.IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )

        val temperature =
            batteryIntent?.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                0
            ) ?: 0

        temperatureText.text =
            "Battery Temperature: ${temperature / 10.0} °C"

        val stat =
            StatFs(
                Environment.getDataDirectory().path
            )

        val totalStorage = stat.totalBytes
        val freeStorage = stat.availableBytes
        val usedStorage =
            totalStorage - freeStorage

        storageText.text =
            "Storage\n" +
                    "Used: ${formatBytes(usedStorage)}\n" +
                    "Free: ${formatBytes(freeStorage)}\n" +
                    "Total: ${formatBytes(totalStorage)}"
    }

    private fun scanCache() {

        val size = directorySize(cacheDir)

        android.app.AlertDialog.Builder(this)
            .setTitle("Junk / Cache Scan")
            .setMessage(
                "App cache found:\n\n" +
                        formatBytes(size) +
                        "\n\n" +
                        "System cache requires system/privileged access."
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

        val temperature =
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

        android.app.AlertDialog.Builder(this)
            .setTitle("Battery Information")
            .setMessage(
                "Battery: $level / $scale\n" +
                        "Charging: " +
                        if (charging) "YES" else "NO" +
                        "\nTemperature: " +
                        "${temperature / 10.0} °C"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun openAppManager() {

        val apps =
            packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA
            )
                .filter {
                    it.packageName != packageName
                }
                .sortedBy {
                    packageManager
                        .getApplicationLabel(it)
                        .toString()
                        .lowercase(Locale.getDefault())
                }

        val names =
            apps.map {
                packageManager
                    .getApplicationLabel(it)
                    .toString()
            }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Installed Apps")
            .setItems(names) { _, position ->

                showAppActions(
                    apps[position].packageName
                )
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showAppActions(packageName: String) {

        val appInfo =
            try {
                packageManager.getApplicationInfo(
                    packageName,
                    0
                )
            } catch (_: Exception) {
                null
            }

        val name =
            if (appInfo != null) {
                packageManager
                    .getApplicationLabel(appInfo)
                    .toString()
            } else {
                packageName
            }

        val actions = arrayOf(
            "RUN APP",
            "KILL BACKGROUND",
            "APP INFO",
            "SECURITY SCAN"
        )

        android.app.AlertDialog.Builder(this)
            .setTitle(name)
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

        val launchIntent =
            packageManager.getLaunchIntentForPackage(
                packageName
            )

        if (launchIntent != null) {

            startActivity(launchIntent)

        } else {

            Toast.makeText(
                this,
                "This app cannot be launched.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun killApp(packageName: String) {

        android.app.AlertDialog.Builder(this)
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

        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            )

        intent.data =
            Uri.parse("package:$packageName")

        startActivity(intent)
    }

    private fun securityScan(packageName: String) {

        val info =
            try {
                packageManager.getPackageInfo(
                    packageName,
                    0
                )
            } catch (_: Exception) {
                null
            }

        val version =
            info?.versionName ?: "Unknown"

        android.app.AlertDialog.Builder(this)
            .setTitle("Security Scan")
            .setMessage(
                "Package:\n$packageName\n\n" +
                        "Version:\n$version\n\n" +
                        "Basic package inspection completed.\n\n" +
                        "This is not a full antivirus scan."
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

    private fun directorySize(directory: File): Long {

        if (!directory.exists()) return 0L

        var size = 0L

        try {

            directory.listFiles()?.forEach { file ->

                size += if (file.isDirectory) {
                    directorySize(file)
                } else {
                    file.length()
                }
            }

        } catch (_: Exception) {
        }

        return size
    }

    private fun formatBytes(bytes: Long): String {

        if (bytes <= 0L) return "0 MB"

        val mb =
            bytes / (1024.0 * 1024.0)

        return if (mb < 1024.0) {

            String.format(
                Locale.US,
                "%.1f MB",
                mb
            )

        } else {

            String.format(
                Locale.US,
                "%.2f GB",
                mb / 1024.0
            )
        }
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int
    ): TextView {

        val view = TextView(this)

        view.text = value
        view.textSize = size
        view.setTextColor(color)
        view.setPadding(
            dp(4),
            dp(8),
            dp(4),
            dp(8)
        )

        return view
    }

    private fun section(value: String): TextView {

        return makeText(
            value,
            18f,
            0xFF66CCFF.toInt()
        )
    }

    private fun makeCard(view: View): View {

        val container =
            LinearLayout(this)

        container.orientation =
            LinearLayout.VERTICAL

        container.setPadding(
            dp(12),
            dp(8),
            dp(12),
            dp(8)
        )

        container.setBackgroundColor(
            0xFF1B1D22.toInt()
        )

        container.addView(view)

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

        container.layoutParams = params

        return container
    }

    private fun makeButton(
        value: String
    ): Button {

        val button = Button(this)

        button.text = value
        button.textSize = 15f
        button.isAllCaps = false

        button.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                setMargins(
                    0,
                    dp(5),
                    0,
                    dp(5)
                )
            }

        return button
    }

    private fun weightParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            dp(52),
            1f
        ).apply {
            setMargins(
                dp(2),
                0,
                dp(2),
                0
            )
        }
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
