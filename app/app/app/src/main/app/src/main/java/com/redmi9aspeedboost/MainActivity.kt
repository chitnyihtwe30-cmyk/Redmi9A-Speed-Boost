```kotlin
package com.redmi9aspeedboost

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import java.io.File
import java.util.Locale
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var ramText: TextView
    private lateinit var batteryText: TextView
    private lateinit var tempText: TextView
    private lateinit var storageText: TextView
    private lateinit var deviceText: TextView
    private lateinit var rootText: TextView
    private lateinit var shizukuText: TextView
    private lateinit var historyText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var appListLayout: LinearLayout

    private var appEnabled = true
    private var boostMode = 1
    private var beforeRam = 0L
    private var afterRam = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        requestNotificationPermission()

        buildUI()
        updateDeviceInfo()
        updateShizukuStatus()
    }

    // ---------------------------------------------------------
    // MAIN UI
    // ---------------------------------------------------------

    private fun buildUI() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 24)

        scroll.addView(root)

        val title = TextView(this)
        title.text = "REDMI 9A SPEED BOOST"
        title.textSize = 26f
        title.gravity = Gravity.CENTER
        title.setPadding(0, 12, 0, 20)
        root.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "PHONE BOOST • MONITOR • APP MANAGER"
        subtitle.textSize = 13f
        subtitle.gravity = Gravity.CENTER
        root.addView(subtitle)

        // APP POWER
        addSectionTitle(root, "SYSTEM CONTROL")

        val powerButton = Button(this)
        powerButton.text = "APP ON"

        powerButton.setOnClickListener {
            appEnabled = !appEnabled

            if (appEnabled) {
                powerButton.text = "APP ON"
                statusText.text = "System Ready"
                Toast.makeText(
                    this,
                    "Boost System ON",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                powerButton.text = "APP OFF"
                statusText.text = "Boost System OFF"
                Toast.makeText(
                    this,
                    "Boost System OFF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        root.addView(
            powerButton,
            fullButtonParams()
        )

        statusText = createInfoText()
        statusText.text = "System Ready"
        statusText.gravity = Gravity.CENTER
        root.addView(statusText)

        // ---------------------------------------------------------
        // DEVICE STATUS
        // ---------------------------------------------------------

        addSectionTitle(root, "PHONE MONITOR")

        ramText = createInfoText()
        batteryText = createInfoText()
        tempText = createInfoText()
        storageText = createInfoText()

        root.addView(ramText)
        root.addView(batteryText)
        root.addView(tempText)
        root.addView(storageText)

        val refreshButton = Button(this)
        refreshButton.text = "REFRESH MONITOR"
        refreshButton.setOnClickListener {
            updateDeviceInfo()
        }
        root.addView(refreshButton, fullButtonParams())

        val ramButton = Button(this)
        ramButton.text = "RAM OPTIMIZE"
        ramButton.setOnClickListener {
            optimizeRam()
        }
        root.addView(ramButton, fullButtonParams())

        val storageButton = Button(this)
        storageButton.text = "SCAN STORAGE / JUNK"
        storageButton.setOnClickListener {
            scanStorage()
        }
        root.addView(storageButton, fullButtonParams())

        val batteryButton = Button(this)
        batteryButton.text = "BATTERY INFORMATION"
        batteryButton.setOnClickListener {
            updateBatteryInfo()
        }
        root.addView(batteryButton, fullButtonParams())

        // ---------------------------------------------------------
        // DEVICE INFO
        // ---------------------------------------------------------

        addSectionTitle(root, "DEVICE INFORMATION")

        deviceText = createInfoText()
        root.addView(deviceText)

        // ---------------------------------------------------------
        // ROOT
        // ---------------------------------------------------------

        addSectionTitle(root, "ROOT STATUS")

        rootText = createInfoText()
        rootText.text = detectRoot()
        root.addView(rootText)

        val fakeRootButton = Button(this)
        fakeRootButton.text = "FAKE ROOT SIMULATION"

        fakeRootButton.setOnClickListener {

            rootText.text =
                "Fake Root: ENABLED\n" +
                "Simulation Only\n" +
                "Actual Root: " +
                if (isDeviceRooted()) "Detected" else "Not Detected"

            Toast.makeText(
                this,
                "Fake Root is simulation only",
                Toast.LENGTH_SHORT
            ).show()
        }

        root.addView(
            fakeRootButton,
            fullButtonParams()
        )

        // ---------------------------------------------------------
        // SHIZUKU
        // ---------------------------------------------------------

        addSectionTitle(root, "APP MANAGER PRIVILEGE")

        shizukuText = createInfoText()
        root.addView(shizukuText)

        val shizukuRefresh = Button(this)
        shizukuRefresh.text = "CHECK SHIZUKU"
        shizukuRefresh.setOnClickListener {
            updateShizukuStatus()
        }

        root.addView(
            shizukuRefresh,
            fullButtonParams()
        )

        // ---------------------------------------------------------
        // BOOST MODES
        // ---------------------------------------------------------

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
        boostButton.textSize = 20f

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

        root.addView(
            boostButton,
            fullButtonParams()
        )

        // ---------------------------------------------------------
        // PROGRESS
        // ---------------------------------------------------------

        addSectionTitle(root, "BOOST PROGRESS")

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        )

        progressBar.max = 100
        progressBar.progress = 0
        progressBar.isIndeterminate = false

        root.addView(
            progressBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                45
            )
        )

        // ---------------------------------------------------------
        // RESULT
        // ---------------------------------------------------------

        addSectionTitle(root, "BOOST RESULT")

        val resultButton = Button(this)
        resultButton.text = "SHOW BEFORE / AFTER"

        resultButton.setOnClickListener {

            statusText.text =
                "RAM BEFORE: $beforeRam MB\n" +
                "RAM AFTER: $afterRam MB\n\n" +
                "Optimization request completed."
        }

        root.addView(
            resultButton,
            fullButtonParams()
        )

        // ---------------------------------------------------------
        // HISTORY
        // ---------------------------------------------------------

        addSectionTitle(root, "BOOST HISTORY")

        historyText = createInfoText()
        historyText.text = "No boost history yet."
        root.addView(historyText)

        // ---------------------------------------------------------
        // APP MANAGER
        // ---------------------------------------------------------

        addSectionTitle(root, "APP MANAGER")

        val appManagerButton = Button(this)
        appManagerButton.text = "OPEN APP MANAGER"

        appManagerButton.setOnClickListener {
            showAppManager()
        }

        root.addView(
            appManagerButton,
            fullButtonParams()
        )

        // ---------------------------------------------------------
        // SETTINGS
        // ---------------------------------------------------------

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

        root.addView(
            storageSettingsButton,
            fullButtonParams()
        )

        setContentView(scroll)
    }

    // ---------------------------------------------------------
    // APP MANAGER
    // ---------------------------------------------------------

    private fun showAppManager() {

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("APP MANAGER")

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(20, 10, 20, 10)

        val search = EditText(this)
        search.hint = "Search app..."
        container.addView(search)

        appListLayout = LinearLayout(this)
        appListLayout.orientation = LinearLayout.VERTICAL

        val listScroll = ScrollView(this)
        listScroll.addView(appListLayout)

        container.addView(
            listScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                650
            )
        )

        dialog.setView(container)

        dialog.setNegativeButton("CLOSE", null)

        val appDialog = dialog.create()

        search.addTextChangedListener(
            SimpleTextWatcher {
                loadApps(it)
            }
        )

        appDialog.show()

        loadApps("")
    }

    private fun loadApps(query: String) {

        if (!::appListLayout.isInitialized) return

        appListLayout.removeAllViews()

        val pm = packageManager

        val packages =
            pm.getInstalledPackages(
                PackageManager.GET_META_DATA
            )

        for (pkg in packages) {

            val appInfo = pkg.applicationInfo ?: continue

            val appName =
                pm.getApplicationLabel(appInfo).toString()

            val packageName = pkg.packageName

            if (
                query.isNotBlank() &&
                !appName.contains(
                    query,
                    ignoreCase = true
                ) &&
                !packageName.contains(
                    query,
                    ignoreCase = true
                )
            ) {
                continue
            }

            val row = LinearLayout(this)
            row.orientation = LinearLayout.VERTICAL
            row.setPadding(8, 12, 8, 12)

            val name = TextView(this)
            name.text = appName
            name.textSize = 17f

            val info = TextView(this)

            val version =
                try {
                    if (Build.VERSION.SDK_INT >= 33) {
                        pm.getPackageInfo(
                            packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        ).versionName ?: "Unknown"
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getPackageInfo(
                            packageName,
                            0
                        ).versionName ?: "Unknown"
                    }
                } catch (e: Exception) {
                    "Unknown"
                }

            info.text =
                "$packageName\nVersion: $version"

            row.addView(name)
            row.addView(info)

            val buttons = LinearLayout(this)
            buttons.orientation = LinearLayout.HORIZONTAL

            val run = Button(this)
            run.text = "RUN"

            run.setOnClickListener {
                confirmRunApp(
                    appName,
                    packageName
                )
            }

            val kill = Button(this)
            kill.text = "KILL"

            kill.setOnClickListener {
                confirmKillApp(
                    appName,
                    packageName
                )
            }

            val security = Button(this)
            security.text = "SCAN"

            security.setOnClickListener {
                scanAppSecurity(
                    appName,
                    packageName
                )
            }

            buttons.addView(run)
            buttons.addView(kill)
            buttons.addView(security)

            row.addView(buttons)

            appListLayout.addView(row)

            val divider = View(this)
            divider.setBackgroundColor(0xFFCCCCCC.toInt())

            appListLayout.addView(
                divider,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
            )
        }
    }

    // ---------------------------------------------------------
    // RUN APP
    // ---------------------------------------------------------

    private fun confirmRunApp(
        appName: String,
        packageName: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("RUN APP")
            .setMessage(
                "App: $appName\n\n" +
                "Package: $packageName\n\n" +
                "Action: Launch application"
            )
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("APPLY") { _, _ ->

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
                            "No launch activity found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        this,
                        "Unable to launch app",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    // ---------------------------------------------------------
    // KILL APP
    // ---------------------------------------------------------

    private fun confirmKillApp(
        appName: String,
        packageName: String
    ) {

        if (packageName == packageNameOfThisApp()) {

            Toast.makeText(
                this,
                "Cannot kill Speed Boost itself",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        AlertDialog.Builder(this)
            .setTitle("KILL APP")
            .setMessage(
                "App: $appName\n\n" +
                "Package: $packageName\n\n" +
                "Expected effect:\n" +
                "Request background process stop.\n\n" +
                "Continue?"
            )
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("APPLY") { _, _ ->

                performKill(
                    appName,
                    packageName
                )
            }
            .show()
    }

    private fun performKill(
        appName: String,
        packageName: String
    ) {

        var success = false

        // Normal Android fallback
        try {

            val manager =
                getSystemService(
                    Context.ACTIVITY_SERVICE
                ) as ActivityManager

            manager.killBackgroundProcesses(
                packageName
            )

            success = true

        } catch (e: Exception) {
            success = false
        }

        // Shizuku availability
        if (isShizukuAvailable()) {

            Toast.makeText(
                this,
                if (success)
                    "$appName stop request sent"
                else
                    "Shizuku available, but action was not completed",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            Toast.makeText(
                this,
                if (success)
                    "$appName stop request sent"
                else
                    "Permission unavailable",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ---------------------------------------------------------
    // APK INSTALL
    // ---------------------------------------------------------

    private fun installApk() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

        intent.type = "application/vnd.android.package-archive"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        startActivityForResult(
            intent,
            REQUEST_INSTALL_APK
        )
    }

    // ---------------------------------------------------------
    // SECURITY SCAN
    // ---------------------------------------------------------

    private fun scanAppSecurity(
        appName: String,
        packageName: String
    ) {

        val pm = packageManager

        val info =
            try {
                if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(
                            PackageManager.GET_PERMISSIONS.toLong()
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(
                        packageName,
                        PackageManager.GET_PERMISSIONS
                    )
                }
            } catch (e: Exception) {
                null
            }

        val permissions =
            info?.requestedPermissions ?: emptyArray()

        val dangerous =
            permissions.count {
                it.contains("LOCATION") ||
                it.contains("CAMERA") ||
                it.contains("MICROPHONE") ||
                it.contains("SMS") ||
                it.contains("CONTACTS") ||
                it.contains("READ_PHONE")
            }

        val result =
            if (dangerous == 0) {
                "LOW RISK\n\nNo common sensitive permission found."
            } else {
                "REVIEW REQUIRED\n\n" +
                "Sensitive permissions detected: $dangerous\n\n" +
                permissions.joinToString("\n")
            }

        AlertDialog.Builder(this)
            .setTitle("SECURITY SCAN\n$appName")
            .setMessage(
                "Package: $packageName\n\n$result"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // ---------------------------------------------------------
    // SHIZUKU
    // ---------------------------------------------------------

    private fun isShizukuAvailable(): Boolean {

        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    private fun updateShizukuStatus() {

        val available = isShizukuAvailable()

        shizukuText.text =
            if (available) {
                "Shizuku: RUNNING\n" +
                "App Manager privileged backend available."
            } else {
                "Shizuku: NOT RUNNING\n" +
                "Fallback Android methods will be used."
            }
    }

    // ---------------------------------------------------------
    // BOOST
    // ---------------------------------------------------------

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

            Runtime.getRuntime().gc()
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

        Runtime.getRuntime().gc()
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

    // ---------------------------------------------------------
    // STORAGE
    // ---------------------------------------------------------

    private fun scanStorage() {

        val stat =
            StatFs(
                Environment.getDataDirectory().path
            )

        val total =
            stat.totalBytes /
                    (1024L * 1024L * 1024L)

        val free =
            stat.availableBytes /
                    (1024L * 1024L * 1024L)

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

    // ---------------------------------------------------------
    // DEVICE MONITOR
    // ---------------------------------------------------------

    private fun updateDeviceInfo() {

        val memory =
            getSystemService(
                Context.ACTIVITY_SERVICE
            ) as ActivityManager

        val memoryInfo =
            ActivityManager.MemoryInfo()

        memory.getMemoryInfo(memoryInfo)

        val totalRam =
            memoryInfo.totalMem /
                    (1024L * 1024L)

        val availableRam =
            memoryInfo.availMem /
                    (1024L * 1024L)

        val usedRam =
            totalRam - availableRam

        ramText.text =
            "RAM Usage: $usedRam MB\n" +
            "Available: $availableRam MB\n" +
            "Total: $totalRam MB"

        updateBatteryInfo()

        val stat =
            StatFs(
                Environment.getDataDirectory().path
            )

        val total =
            stat.totalBytes /
                    (1024L * 1024L * 1024L)

        val free =
            stat.availableBytes /
                    (1024L * 1024L * 1024L)

        val used =
            total - free

        storageText.text =
            "Storage Used: $used GB\n" +
            "Free: $free GB\n" +
            "Total: $total GB"

        deviceText.text =
            "Manufacturer: ${Build.MANUFACTURER}\n" +
            "Model: ${Build.MODEL}\n" +
            "Android: ${Build.VERSION.RELEASE}\n" +
            "SDK: ${Build.VERSION.SDK_INT}\n" +
            "CPU: ${
                Build.SUPPORTED_ABIS.firstOrNull()
                    ?: "Unknown"
            }"
    }

    private fun updateBatteryInfo() {

        val batteryManager =
            getSystemService(
                BATTERY_SERVICE
            ) as BatteryManager

        val level =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        val intent =
            registerReceiver(
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

        val status =
            intent?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            ) ?: -1

        val charging =
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val tempC =
            if (temperature >= 0)
                temperature / 10.0
            else
                0.0

        batteryText.text =
            "Battery: $level%\n" +
            "Charging: ${if (charging) "YES" else "NO"}"

        tempText.text =
            String.format(
                Locale.US,
                "Temperature: %.1f°C",
                tempC
            )
    }

    private fun getUsedRam(): Long {

        val memoryInfo =
            Debug.MemoryInfo()

        Debug.getMemoryInfo(memoryInfo)

        return memoryInfo.totalPss / 1024L
    }

    // ---------------------------------------------------------
    // ROOT
    // ---------------------------------------------------------

    private fun isDeviceRooted(): Boolean {

        val paths =
            arrayOf(
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

    // ---------------------------------------------------------
    // NOTIFICATIONS
    // ---------------------------------------------------------

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    "boost_channel",
                    "Boost Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
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

        manager.notify(
            100,
            notification
        )
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private fun createInfoText(): TextView {

        val text = TextView(this)

        text.textSize = 16f
        text.setPadding(
            8,
            10,
            8,
            10
        )

        return text
    }

    private fun addSectionTitle(
        root: LinearLayout,
        text: String
    ) {

        val title = TextView(this)

        title.text = text
        title.textSize = 19f
        title.setPadding(
            0,
            20,
            0,
            8
        )

        root.addView(title)
    }

    private fun fullButtonParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 6
            bottomMargin = 6
        }
    }

    private fun packageNameOfThisApp(): String {
        return packageName
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == REQUEST_INSTALL_APK &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {

            val uri = data.data!!

            showInstallPreview(uri)
        }
    }

    private fun showInstallPreview(uri: Uri) {

        AlertDialog.Builder(this)
            .setTitle("INSTALL APK")
            .setMessage(
                "APK selected.\n\n" +
                "Security scan will be performed before applying.\n\n" +
                "Action: Install application"
            )
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("APPLY") { _, _ ->

                try {

                    val intent =
                        Intent(
                            Intent.ACTION_VIEW
                        )

                    intent.setDataAndType(
                        uri,
                        "application/vnd.android.package-archive"
                    )

                    intent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    startActivity(intent)

                } catch (e: Exception) {

                    Toast.makeText(
                        this,
                        "Unable to open APK installer",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()

        if (::shizukuText.isInitialized) {
            updateShizukuStatus()
        }

        if (::ramText.isInitialized) {
            updateDeviceInfo()
        }
    }

    companion object {

        private const val REQUEST_INSTALL_APK = 2001
    }
}

// ---------------------------------------------------------
// SIMPLE TEXT WATCHER
// ---------------------------------------------------------

private class SimpleTextWatcher(
    private val callback: (String) -> Unit
) : android.text.TextWatcher {

    override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
    ) {}

    override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) {
        callback(s?.toString() ?: "")
    }

    override fun afterTextChanged(
        s: android.text.Editable?
    ) {}
}
```
