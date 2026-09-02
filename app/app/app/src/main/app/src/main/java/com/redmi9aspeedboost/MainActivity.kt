package com.redmi9aspeedboost

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.Build
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private val bg = Color.rgb(9, 14, 22)
    private val card = Color.rgb(20, 27, 39)
    private val card2 = Color.rgb(28, 37, 51)
    private val accent = Color.rgb(75, 210, 255)
    private val primaryText = Color.rgb(240, 246, 252)
    private val muted = Color.rgb(150, 165, 180)
    private lateinit var appPowerButton: Button
    private lateinit var status: TextView
    private var appEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        createUI()
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
            this.text = title
            textSize = 12f
            setTextColor(this@MainActivity.muted)
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
        this.text = label
        textSize = size
        setTextColor(this@MainActivity.primaryText)
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
        hero.addView(TextView(this).apply { this.text = "⚡"; textSize = 32f; gravity = Gravity.CENTER })
        hero.addView(TextView(this).apply { this.text = "REDMI 9A"; textSize = 28f; setTextColor(this@MainActivity.primaryText); gravity = Gravity.CENTER; setTypeface(typeface, android.graphics.Typeface.BOLD) })
        hero.addView(TextView(this).apply { this.text = "SPEED BOOST"; textSize = 20f; setTextColor(this@MainActivity.accent); gravity = Gravity.CENTER; setTypeface(typeface, android.graphics.Typeface.BOLD) })
        hero.addView(TextView(this).apply { this.text = "PREMIUM • SMART • NON-ROOT"; textSize = 10f; setTextColor(this@MainActivity.muted); gravity = Gravity.CENTER; setPadding(0, 6, 0, 0) })
        root.addView(hero)

        appPowerButton = makeButton("🟢  BOOST APP  •  ON", Color.rgb(20, 88, 66), 17f)
        appPowerButton.setOnClickListener {
            appEnabled = !appEnabled
            updatePowerUI()
            status.text = if (appEnabled) "● SYSTEM READY" else "○ BOOST APP IS OFF"
        }
        root.addView(appPowerButton)

        status = TextView(this).apply { this.text = "● SYSTEM READY"; textSize = 12f; setTextColor(this@MainActivity.accent); gravity = Gravity.CENTER; setPadding(0, 8, 0, 8) }
        root.addView(status)

        section(root, "PHONE STATUS")
        val monitor = makeCard()
        monitor.addView(infoLine("RAM", ramInfo()))
        monitor.addView(infoLine("BATTERY", "${batteryLevel()}%"))
        monitor.addView(infoLine("STORAGE", storageInfo()))
        val refresh = makeButton("↻  REFRESH PHONE STATUS")
        refresh.setOnClickListener {
            (monitor.getChildAt(0) as LinearLayout).getChildAt(1).let { (it as TextView).text = ramInfo() }
            (monitor.getChildAt(1) as LinearLayout).getChildAt(1).let { (it as TextView).text = "${batteryLevel()}%" }
            (monitor.getChildAt(2) as LinearLayout).getChildAt(1).let { (it as TextView).text = storageInfo() }
            status.text = "● PHONE STATUS REFRESHED ✓"
        }
        monitor.addView(refresh)
        root.addView(monitor)

        section(root, "BOOST CONTROL")
        val modes = makeCard()
        val modeText = TextView(this).apply { this.text = "Selected: MODE 1 • SAFE"; textSize = 14f; setTextColor(this@MainActivity.accent); gravity = Gravity.CENTER; setPadding(0, 4, 0, 8) }
        modes.addView(modeText)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("1 • SAFE", "2 • PERFORMANCE", "3 • MAX SAFE").forEachIndexed { i, label ->
            val b = makeButton(label, if (i == 0) Color.rgb(24, 72, 93) else card2, 12f)
            b.layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(4, 4, 4, 4) }
            b.setOnClickListener { modeText.text = "Selected: MODE ${i + 1} • ${label.substringAfter("• ").trim()}"; status.text = "● MODE ${i + 1} SELECTED" }
            row.addView(b)
        }
        modes.addView(row)
        val boost = makeButton("⚡  BOOST NOW", Color.rgb(18, 83, 112), 17f)
        boost.setOnClickListener {
            if (!appEnabled) { status.text = "○ BOOST APP IS OFF"; return@setOnClickListener }
            System.gc()
            System.runFinalization()
            Thread.sleep(180)
            status.text = "✓ BOOST COMPLETED • SAFE NON-ROOT"
        }
        modes.addView(boost)
        root.addView(modes)

        section(root, "SMART TOOLS")
        val tools = makeCard()
        val ram = makeButton("🧹  RAM OPTIMIZE")
        ram.setOnClickListener { System.gc(); System.runFinalization(); status.text = "✓ RAM OPTIMIZE COMPLETED" }
        tools.addView(ram)
        val scan = makeButton("🔍  SCAN STORAGE / JUNK")
        scan.setOnClickListener { status.text = "✓ STORAGE SCAN COMPLETED • ${storageInfo()}" }
        tools.addView(scan)
        root.addView(tools)

        section(root, "FAKE ROOT")
        val fake = makeCard()
        fake.addView(TextView(this).apply { this.text = "ROOT STATUS  •  SIMULATED"; textSize = 15f; setTextColor(this@MainActivity.accent); setTypeface(typeface, android.graphics.Typeface.BOLD) })
        fake.addView(TextView(this).apply { this.text = "Visual simulation only • NO real root access"; textSize = 12f; setTextColor(this@MainActivity.muted); setPadding(0, 7, 0, 0) })
        val fakeBtn = makeButton("◉  TOGGLE FAKE ROOT")
        fakeBtn.setOnClickListener { status.text = "✓ FAKE ROOT VISUAL STATUS TOGGLED" }
        fake.addView(fakeBtn)
        root.addView(fake)

        section(root, "DEVICE & SECURITY")
        val details = makeCard()
        details.addView(infoLine("DEVICE", "${Build.MANUFACTURER} ${Build.MODEL}"))
        details.addView(infoLine("ANDROID", "${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}"))
        details.addView(infoLine("APP", "Redmi 9A Speed Boost v1.0"))
        details.addView(infoLine("SECURITY", if (Build.VERSION.SECURITY_PATCH.isNotEmpty()) "PATCH ${Build.VERSION.SECURITY_PATCH}" else "STANDARD"))
        root.addView(details)

        root.addView(TextView(this).apply {
            this.text = "SAFE NON-ROOT OPTIMIZATION\nSystem RAM/kernel settings cannot be changed without privileged access."
            textSize = 10f
            setTextColor(this@MainActivity.muted)
            gravity = Gravity.CENTER
            setPadding(10, 22, 10, 10)
        })
        setContentView(scroll)
    }

    private fun infoLine(label: String, value: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 7, 0, 7)
        addView(TextView(this@MainActivity).apply { this.text = label; textSize = 12f; setTextColor(this@MainActivity.muted); layoutParams = LinearLayout.LayoutParams(0, -2, 1f) })
        addView(TextView(this@MainActivity).apply { this.text = value; textSize = 13f; setTextColor(this@MainActivity.primaryText); gravity = Gravity.END; layoutParams = LinearLayout.LayoutParams(0, -2, 1.6f) })
    }

    private fun updatePowerUI() {
        appPowerButton.text = if (appEnabled) "🟢  BOOST APP  •  ON" else "🔴  BOOST APP  •  OFF"
        appPowerButton.background = rounded(if (appEnabled) Color.rgb(20, 88, 66) else Color.rgb(78, 35, 43), 16f)
    }

    private fun ramInfo(): String {
        val mi = android.app.ActivityManager.MemoryInfo()
        (getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager).getMemoryInfo(mi)
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
        val i = registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = i?.getIntExtra("level", 0) ?: 0
        val scale = i?.getIntExtra("scale", 100) ?: 100
        return if (scale > 0) (level * 100 / scale) else 0
    }
}
