package com.redmi9aspeedboost

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.Build
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import rikka.shizuku.Shizuku

class MainActivity : Activity() {
    private val bg = Color.rgb(9, 14, 22)
    private val card = Color.rgb(20, 27, 39)
    private val card2 = Color.rgb(28, 37, 51)
    private val accent = Color.rgb(75, 210, 255)
    private val primaryText = Color.rgb(240, 246, 252)
    private val muted = Color.rgb(150, 165, 180)
    private lateinit var appPowerButton: Button
    private lateinit var status: TextView
    private lateinit var shizukuStatus: TextView
    private lateinit var ramValue: TextView
    private lateinit var batteryValue: TextView
    private lateinit var storageValue: TextView
    private var appEnabled = true
    private var selectedMode = 0
    private var shizukuReady = false
    private val shizukuRequestCode = 1001

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == shizukuRequestCode) {
            shizukuReady = grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED
            updateShizukuUI()
            status.text = if (shizukuReady) "✓ SHIZUKU CONNECTED" else "⚠ SHIZUKU PERMISSION DENIED"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        shizukuReady = isShizukuReady()
        createUI()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (::shizukuStatus.isInitialized) {
            shizukuReady = isShizukuReady()
            updateShizukuUI()
        }
    }

    private fun rounded(color: Int, radius: Float, stroke: Int? = null): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
        if (stroke != null) setStroke(2, stroke)
    }

    private fun marginView(v: View, h: Int, vertical: Int) {
        v.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(h, vertical, h, vertical) }
    }

    private fun section(root: LinearLayout, title: String) {
        root.addView(TextView(this).apply {
            text = title
            textSize = 12f
            setTextColor(muted)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(5, 18, 5, 7)
        })
    }

    private fun makeCard(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(18, 16, 18, 16)
        background = rounded(card, 22f, Color.rgb(35, 44, 59))
        marginView(this, 6, 6)
    }

    private fun makeButton(label: String, color: Int = card2, size: Float = 15f): Button = Button(this).apply {
        text = label
        textSize = size
        setTextColor(primaryText)
        isAllCaps = false
        background = rounded(color, 16f)
        stateListAnimator = null
        setPadding(12, 8, 12, 8)
        marginView(this, 5, 5)
    }

    private fun createUI() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); clipToPadding = false }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 30)
            setBackgroundColor(bg)
        }
        scroll.addView(root)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 24, 18, 24)
            background = rounded(Color.rgb(13, 29, 43), 26f, Color.rgb(47, 117, 150))
            marginView(this, 0, 10)
        }
        hero.addView(TextView(this).apply { text = "⚡"; textSize = 32f; gravity = Gravity.CENTER })
        hero.addView(TextView(this).apply { text = "REDMI 9A"; textSize = 28f; setTextColor(primaryText); gravity = Gravity.CENTER; setTypeface(typeface, android.graphics.Typeface.BOLD) })
        hero.addView(TextView(this).apply { text = "SPEED BOOST"; textSize = 20f; setTextColor(accent); gravity = Gravity.CENTER; setTypeface(typeface, android.graphics.Typeface.BOLD) })
        hero.addView(TextView(this).apply { text = "NON-ROOT + SHIZUKU"; textSize = 10f; setTextColor(muted); gravity = Gravity.CENTER; setPadding(0, 6, 0, 0) })
        root.addView(hero)

        appPowerButton = makeButton("🟢  BOOST APP  •  ON", Color.rgb(20, 88, 66), 17f)
        appPowerButton.setOnClickListener {
            appEnabled = !appEnabled
            updatePowerUI()
            status.text = if (appEnabled) "● SYSTEM READY" else "○ BOOST APP IS OFF"
        }
        root.addView(appPowerButton)

        status = TextView(this).apply { text = "● SYSTEM READY"; textSize = 12f; setTextColor(accent); gravity = Gravity.CENTER; setPadding(0, 8, 0, 8) }
        root.addView(status)

        section(root, "SHIZUKU")
        val shizukuCard = makeCard()
        shizukuStatus = TextView(this).apply {
            textSize = 14f
            setTextColor(accent)
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 8)
        }
        shizukuCard.addView(shizukuStatus)
        val shizukuButton = makeButton("🔐  REQUEST SHIZUKU PERMISSION", Color.rgb(50, 64, 91), 14f)
        shizukuButton.setOnClickListener { requestShizukuPermission() }
        shizukuCard.addView(shizukuButton)
        root.addView(shizukuCard)
        updateShizukuUI()

        section(root, "PHONE STATUS")
        val monitor = makeCard()
        val ramLine = infoLine("RAM", ramInfo())
        val batteryLine = infoLine("BATTERY", "${batteryLevel()}%")
        val storageLine = infoLine("STORAGE", storageInfo())
        ramValue = ramLine.getChildAt(1) as TextView
        batteryValue = batteryLine.getChildAt(1) as TextView
        storageValue = storageLine.getChildAt(1) as TextView
        monitor.addView(ramLine)
        monitor.addView(batteryLine)
        monitor.addView(storageLine)
        val refresh = makeButton("↻  REFRESH PHONE STATUS")
        refresh.setOnClickListener { refreshStatus("● PHONE STATUS REFRESHED ✓") }
        monitor.addView(refresh)
        root.addView(monitor)

        section(root, "BOOST CONTROL")
        val modes = makeCard()
        val modeText = TextView(this).apply { text = "Selected: MODE 1 • SAFE"; textSize = 14f; setTextColor(accent); gravity = Gravity.CENTER; setPadding(0, 4, 0, 8) }
        modes.addView(modeText)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("1 • SAFE", "2 • PERFORMANCE", "3 • MAX SAFE").forEachIndexed { i, label ->
            val b = makeButton(label, if (i == 0) Color.rgb(24, 72, 93) else card2, 12f)
            b.layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(4, 4, 4, 4) }
            b.setOnClickListener {
                selectedMode = i
                modeText.text = "Selected: MODE ${i + 1} • ${label.substringAfter("• ").trim()}"
                status.text = "● MODE ${i + 1} SELECTED"
            }
            row.addView(b)
        }
        modes.addView(row)
        val boost = makeButton("⚡  BOOST NOW", Color.rgb(18, 83, 112), 17f)
        boost.setOnClickListener { performBoost() }
        modes.addView(boost)
        root.addView(modes)

        section(root, "SMART TOOLS")
        val tools = makeCard()
        val ram = makeButton("🧹  RAM OPTIMIZE")
        ram.setOnClickListener { optimizeMemory() }
        tools.addView(ram)
        val scan = makeButton("🔍  SCAN STORAGE / JUNK")
        scan.setOnClickListener { scanStorage() }
        tools.addView(scan)
        root.addView(tools)

        section(root, "DEVICE & SECURITY")
        val details = makeCard()
        details.addView(infoLine("DEVICE", "${Build.MANUFACTURER} ${Build.MODEL}"))
        details.addView(infoLine("ANDROID", "${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}"))
        details.addView(infoLine("APP", "Redmi 9A Speed Boost v1.2"))
        details.addView(infoLine("SECURITY", if (Build.VERSION.SECURITY_PATCH.isNotEmpty()) "PATCH ${Build.VERSION.SECURITY_PATCH}" else "STANDARD"))
        root.addView(details)

        root.addView(TextView(this).apply {
            text = "REAL NON-ROOT + SHIZUKU OPTIMIZATION\nShizuku enables privileged shell operations when the user grants permission. Without Shizuku, the app falls back to Android's normal APIs."
            textSize = 10f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(10, 22, 10, 10)
        })
        setContentView(scroll)
    }

    private fun isShizukuReady(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    private fun requestShizukuPermission() {
        try {
            if (!Shizuku.pingBinder()) {
                shizukuReady = false
                updateShizukuUI()
                status.text = "⚠ START SHIZUKU FIRST"
                return
            }
            if (Shizuku.isPreV11()) {
                status.text = "⚠ SHIZUKU VERSION TOO OLD"
                return
            }
            if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(shizukuRequestCode)
            } else {
                shizukuReady = true
                updateShizukuUI()
                status.text = "✓ SHIZUKU ALREADY AUTHORIZED"
            }
        } catch (_: Exception) {
            status.text = "⚠ SHIZUKU REQUEST FAILED"
        }
    }

    private fun updateShizukuUI() {
        if (!::shizukuStatus.isInitialized) return
        shizukuStatus.text = if (shizukuReady) "● CONNECTED • PRIVILEGED MODE READY" else "○ NOT CONNECTED • NON-ROOT FALLBACK"
        shizukuStatus.setTextColor(if (shizukuReady) Color.rgb(80, 220, 150) else muted)
    }

    private fun performBoost() {
        if (!appEnabled) { status.text = "○ BOOST APP IS OFF"; return }
        status.text = "⚙ BOOSTING…"
        releaseAppMemory()
        var shizukuCount = 0
        if (selectedMode >= 1 && shizukuReady) shizukuCount = stopBackgroundAppsWithShizuku()
        if (selectedMode >= 1 && !shizukuReady) stopBackgroundApps()
        refreshStatus(if (shizukuReady && selectedMode >= 1) "✓ BOOST COMPLETED • MODE ${selectedMode + 1} • SHIZUKU • ${shizukuCount} PROCESSES" else "✓ BOOST COMPLETED • MODE ${selectedMode + 1} • NON-ROOT")
    }

    private fun optimizeMemory() {
        if (!appEnabled) { status.text = "○ BOOST APP IS OFF"; return }
        releaseAppMemory()
        refreshStatus("✓ RAM OPTIMIZE COMPLETED • APP MEMORY RELEASED")
    }

    private fun releaseAppMemory() {
        try {
            window.decorView.rootView.clearAnimation()
            Runtime.getRuntime().gc()
            System.gc()
            System.runFinalization()
        } catch (_: Exception) { }
    }

    @Suppress("DEPRECATION")
    private fun stopBackgroundApps() {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        try {
            val running = am.runningAppProcesses ?: return
            for (process in running) {
                val name = process.processName ?: continue
                if (name != packageName && process.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    am.killBackgroundProcesses(name)
                }
            }
        } catch (_: SecurityException) {
            status.text = "⚠ BACKGROUND CLEANUP LIMITED BY ANDROID"
        } catch (_: Exception) { }
    }

    private fun stopBackgroundAppsWithShizuku(): Int {
        return try {
            if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) return 0
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val running = am.runningAppProcesses ?: return 0
            var count = 0
            for (process in running) {
                val name = process.processName ?: continue
                if (name == packageName || name.startsWith("com.android.") || name.startsWith("com.google.android.gms")) continue
                if (process.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    val p = Shizuku.newProcess(arrayOf("sh", "-c", "cmd activity force-stop $name"), null, null)
                    p.waitFor()
                    p.destroy()
                    count++
                }
            }
            count
        } catch (_: Exception) {
            status.text = "⚠ SHIZUKU CLEANUP FAILED"
            0
        }
    }

    private fun scanStorage() {
        val dataDir = Environment.getDataDirectory()
        val cacheBytes = directorySize(cacheDir)
        val dataFs = StatFs(dataDir.path)
        val free = dataFs.availableBytes / (1024L * 1024L)
        status.text = "✓ SCAN DONE • APP CACHE ${cacheBytes / (1024 * 1024)} MB • ${free} MB FREE"
    }

    private fun directorySize(file: java.io.File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        var total = 0L
        file.listFiles()?.forEach { total += directorySize(it) }
        return total
    }

    private fun refreshStatus(message: String) {
        ramValue.text = ramInfo()
        batteryValue.text = "${batteryLevel()}%"
        storageValue.text = storageInfo()
        status.text = message
    }

    private fun infoLine(label: String, value: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 7, 0, 7)
        addView(TextView(this@MainActivity).apply { text = label; textSize = 12f; setTextColor(muted); layoutParams = LinearLayout.LayoutParams(0, -2, 1f) })
        addView(TextView(this@MainActivity).apply { text = value; textSize = 13f; setTextColor(primaryText); gravity = Gravity.END; layoutParams = LinearLayout.LayoutParams(0, -2, 1.6f) })
    }

    private fun updatePowerUI() {
        appPowerButton.text = if (appEnabled) "🟢  BOOST APP  •  ON" else "🔴  BOOST APP  •  OFF"
        appPowerButton.background = rounded(if (appEnabled) Color.rgb(20, 88, 66) else Color.rgb(78, 35, 43), 16f)
    }

    private fun ramInfo(): String {
        val mi = ActivityManager.MemoryInfo()
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(mi)
        val used = (mi.totalMem - mi.availMem) / (1024 * 1024)
        return "${used} MB used / ${mi.totalMem / (1024 * 1024)} MB"
    }

    private fun storageInfo(): String {
        val s = StatFs(Environment.getDataDirectory().path)
        val total = s.totalBytes / (1024L * 1024L * 1024L)
        val free = s.availableBytes / (1024L * 1024L * 1024L)
        return "$free GB free / $total GB"
    }

    private fun batteryLevel(): Int {
        val i = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = i?.getIntExtra("level", 0) ?: 0
        val scale = i?.getIntExtra("scale", 100) ?: 100
        return if (scale > 0) (level * 100 / scale) else 0
    }
}
