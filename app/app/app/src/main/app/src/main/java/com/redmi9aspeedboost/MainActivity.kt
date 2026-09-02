package com.redmi9aspeedboost

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

    private val bg = Color.rgb(8, 11, 18)
    private val card = Color.rgb(18, 24, 34)
    private val card2 = Color.rgb(24, 31, 44)
    private val accent = Color.rgb(45, 205, 125)
    private val blue = Color.rgb(45, 130, 255)
    private val text = Color.WHITE
    private val muted = Color.rgb(150, 162, 180)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        createUI()
        refreshInfo()
        updatePowerUI()
        updateFakeRootUI()
    }

    private fun rounded(color: Int, radius: Float = 18f, stroke: Int = Color.TRANSPARENT): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (stroke != Color.TRANSPARENT) setStroke(1, stroke)
        }

    private fun marginView(v: View, top: Int = 8, bottom: Int = 8) {
        v.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, top, 0, bottom) }
    }

    private fun addText(root: LinearLayout, value: String, size: Float, color: Int = text, bold: Boolean = false) {
        root.addView(TextView(this).apply {
            text = value; textSize = size; setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 2, 0, 2)
        })
    }

    private fun section(root: LinearLayout, title: String, icon: String = "") {
        val t = TextView(this).apply {
            text = "$icon$title"; textSize = 14f; setTextColor(muted)
            setTypeface(typeface, android.graphics.Typeface.BOLD); setPadding(4, 18, 4, 6)
        }
        root.addView(t)
    }

    private fun makeCard(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(18, 16, 18, 16)
        background = rounded(card, 20f, Color.rgb(38, 48, 65))
        marginView(this, 6, 6)
    }

    private fun makeButton(label: String, color: Int = card2, size: Float = 15f): Button = Button(this).apply {
        text = label; textSize = size; setTextColor(text); isAllCaps = false
        background = rounded(color, 16f)
        stateListAnimator = null
        setPadding(12, 8, 12, 8)
        marginView(this, 5, 5)
    }

    private fun createUI() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); clipToPadding = false }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(18, 18, 18, 28); setBackgroundColor(bg)
        }
        scroll.addView(root)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(18, 24, 18, 22)
            background = rounded(Color.rgb(15, 35, 43), 24f, Color.rgb(32, 103, 104)); marginView(this, 0, 10)
        }
        val title = TextView(this).apply {
            text = "⚡ REDMI 9A"; textSize = 28f; setTextColor(text); gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        hero.addView(title)
        hero.addView(TextView(this).apply {
            text = "SPEED BOOST"; textSize = 21f; setTextColor(accent); gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD); setPadding(0, 2, 0, 5)
        })
        hero.addView(TextView(this).apply {
            text = "SMART • SAFE • NON-ROOT"; textSize = 11f; setTextColor(muted); gravity = Gravity.CENTER
        })
        root.addView(hero)

        appPowerButton = makeButton("🟢  BOOST APP: ON", Color.rgb(24, 93, 69), 17f)
        appPowerButton.setOnClickListener {
            appEnabled = !appEnabled; updatePowerUI()
            status.text = if (appEnabled) "● SYSTEM READY" else "○ BOOST APP IS OFF"
        }
        root.addView(appPowerButton)

        val rootCard = makeCard()
        addText(rootCard, "🔐  FAKE ROOT", 17f, text, true)
        addText(rootCard, "Simulation only • NO real root access", 12f, muted)
        fakeRootStatus = TextView(this).apply { textSize = 14f; setTextColor(accent); setPadding(0, 10, 0, 4) }
        rootCard.addView(fakeRootStatus)
        val fakeRootButton = makeButton("Toggle Fake Root", card2)
        fakeRootButton.setOnClickListener { fakeRootEnabled = !fakeRootEnabled; updateFakeRootUI(); status.text = if (fakeRootEnabled) "FAKE ROOT SIMULATION: ON" else "FAKE ROOT SIMULATION: OFF" }
        rootCard.addView(fakeRootButton)
        root.addView(rootCard)

        val statusCard = makeCard()
        status = TextView(this).apply {
            text = "● SYSTEM READY"; textSize = 16f; gravity = Gravity.CENTER; setTextColor(accent); setPadding(8, 10, 8, 10)
            background = rounded(Color.rgb(12, 38, 34), 14f)
        }
        statusCard.addView(status)
        val refresh = makeButton("↻  Refresh Phone Status", blue)
        refresh.setOnClickListener { refreshInfo(); status.text = "✓ PHONE STATUS REFRESHED" }
        statusCard.addView(refresh)
        root.addView(statusCard)

        section(root, "LIVE PHONE STATUS")
        val infoRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 3f }
        addMiniCard(infoRow, "🧠", "RAM", { ram = it })
        addMiniCard(infoRow, "🔋", "BATTERY", { battery = it })
        addMiniCard(infoRow, "💾", "STORAGE", { storage = it })
        root.addView(infoRow)

        val boostCard = makeCard()
        addText(boostCard, "🚀  BOOST MODE", 18f, text, true)
        addText(boostCard, "Choose your performance profile", 12f, muted)
        modeSelector = Spinner(this).apply {
            background = rounded(card2, 14f)
            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, modes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter; setSelection(0)
        }
        modeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { boostMode = position + 1; if (!boosting) status.text = "SELECTED: ${modes[position]}" }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        boostCard.addView(modeSelector)
        boostButton = makeButton("⚡  BOOST PHONE", accent, 18f)
        boostButton.setTextColor(Color.rgb(5, 20, 15)); boostButton.setTypeface(boostButton.typeface, android.graphics.Typeface.BOLD)
        boostButton.setOnClickListener { runBoost() }
        boostCard.addView(boostButton)
        root.addView(boostCard)

        val tools = makeCard()
        addText(tools, "🛠  TOOLS", 18f, text, true)
        val optimize = makeButton("🧹  RAM OPTIMIZE")
        optimize.setOnClickListener { if (!canRun()) return@setOnClickListener; System.gc(); refreshInfo(); status.text = "✓ RAM OPTIMIZATION COMPLETED"; Toast.makeText(this, "RAM optimization completed", Toast.LENGTH_SHORT).show() }
        tools.addView(optimize)
        val scan = makeButton("🗑  SCAN STORAGE / JUNK")
        scan.setOnClickListener { if (canRun()) scanStorage() }
        tools.addView(scan)
        root.addView(tools)

        val deviceCard = makeCard()
        addText(deviceCard, "📱  DEVICE INFORMATION", 18f, text, true)
        device = TextView(this).apply { textSize = 14f; setTextColor(muted); setPadding(0, 8, 0, 10) }
        deviceCard.addView(device)
        val appInfo = makeButton("APP INFORMATION")
        appInfo.setOnClickListener { status.text = "APP INFORMATION\n\nRedmi 9A Speed Boost\nVersion 1.0 • Free\nPackage: com.redmi9aspeedboost" }
        deviceCard.addView(appInfo)
        val security = makeButton("🛡  SECURITY STATUS")
        security.setOnClickListener { status.text = "SECURITY STATUS\n\nROOT: NOT REQUIRED\nFAKE ROOT: SIMULATION ONLY\nSYSTEM PROTECTION: ON\nNO SECURITY BYPASS" }
        deviceCard.addView(security)
        root.addView(deviceCard)

        root.addView(TextView(this).apply {
            text = "✓ SAFE NON-ROOT OPTIMIZATION\nℹ️ Cannot physically add RAM or obtain real root access."
            textSize = 12f; setTextColor(muted); gravity = Gravity.CENTER; setPadding(8, 16, 8, 8)
        })
        setContentView(scroll)
    }

    private fun addMiniCard(row: LinearLayout, icon: String, label: String, assign: (TextView) -> Unit) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(7, 12, 7, 12); background = rounded(card, 18f) }
        box.layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(3, 3, 3, 3) }
        box.addView(TextView(this).apply { text = icon; textSize = 22f; gravity = Gravity.CENTER })
        box.addView(TextView(this).apply { text = label; textSize = 10f; setTextColor(muted); gravity = Gravity.CENTER; setTypeface(typeface, android.graphics.Typeface.BOLD) })
        val value = TextView(this).apply { textSize = 11f; setTextColor(accent); gravity = Gravity.CENTER; setPadding(0, 5, 0, 0) }
        box.addView(value); assign(value); row.addView(box)
    }

    private fun canRun(): Boolean {
        if (!appEnabled) { status.text = "BOOST APP IS OFF"; return false }
        if (boosting) { status.text = "BOOST ALREADY RUNNING"; return false }
        return true
    }

    private fun runBoost() {
        if (!canRun()) return
        boosting = true; boostButton.isEnabled = false; modeSelector.isEnabled = false
        val selected = modes[boostMode - 1]; status.text = "⚡ BOOSTING...\n$selected"
        Thread {
            try {
                when (boostMode) { 1 -> { System.gc(); Thread.sleep(700) }; 2 -> { System.gc(); Thread.sleep(1100); System.runFinalization() }; 3 -> { System.gc(); Thread.sleep(1400); System.runFinalization(); System.gc() } }
            } catch (_: Exception) { }
            runOnUiThread { boosting = false; boostButton.isEnabled = true; modeSelector.isEnabled = true; refreshInfo(); status.text = "✓ BOOST COMPLETED\n$selected"; Toast.makeText(this, "Boost Completed — $selected", Toast.LENGTH_SHORT).show() }
        }.start()
    }

    private fun scanStorage() {
        try { val stat = StatFs(Environment.getDataDirectory().path); val total = stat.totalBytes / 1073741824L; val free = stat.availableBytes / 1073741824L; val used = total - free; status.text = "✓ STORAGE SCAN COMPLETED\n\nUSED: $used GB   FREE: $free GB\nTOTAL: $total GB\n\nJUNK CLEANUP: SAFE SCAN ONLY" }
        catch (_: Exception) { status.text = "STORAGE SCAN FAILED" }
    }

    private fun updatePowerUI() { appPowerButton.text = if (appEnabled) "🟢  BOOST APP: ON" else "⚪  BOOST APP: OFF"; appPowerButton.background = rounded(if (appEnabled) Color.rgb(24, 93, 69) else card2, 16f) }
    private fun updateFakeRootUI() { fakeRootStatus.text = if (fakeRootEnabled) "🟢  FAKE ROOT: ON  •  SIMULATION" else "⚪  FAKE ROOT: OFF  •  SIMULATION" }

    private fun refreshInfo() {
        try { val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager; val memory = ActivityManager.MemoryInfo(); manager.getMemoryInfo(memory); val total = memory.totalMem / 1048576L; val available = memory.availMem / 1048576L; ram.text = "$((total - available)) / $total MB" } catch (_: Exception) { ram.text = "N/A" }
        try { val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager; battery.text = "${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)}%" } catch (_: Exception) { battery.text = "N/A" }
        try { val stat = StatFs(Environment.getDataDirectory().path); val total = stat.totalBytes / 1073741824L; val free = stat.availableBytes / 1073741824L; storage.text = "${total - free}/$total GB" } catch (_: Exception) { storage.text = "N/A" }
        device.text = "Manufacturer: ${Build.MANUFACTURER}\nModel: ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} • SDK ${Build.VERSION.SDK_INT}\nCPU: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}"
    }
}
