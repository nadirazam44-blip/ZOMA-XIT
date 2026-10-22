package com.zomaxit.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText

class MainActivity : Activity() {
private val bg = Color.rgb(5, 9, 17)
private val panel = Color.rgb(13, 23, 37)
private val cyan = Color.rgb(52, 220, 255)
private val muted = Color.rgb(155, 174, 194)
private val prefs by lazy { getSharedPreferences("zoma_access", Context.MODE_PRIVATE) }
private val demoHourKey = "ZOMA-1H-2026"
private val demoPermanentKey = "ZOMA-PERM-2026"

private lateinit var rootLayout: LinearLayout  
private var activeTab = "HOME"  
private var circleSize = 120  
private var circleVisible = false  

override fun onCreate(savedInstanceState: Bundle?) {  
    super.onCreate(savedInstanceState)  
    window.statusBarColor = bg  
    window.navigationBarColor = bg  
    if (hasAccess()) showHome() else showActivation()  
}  

private fun hasAccess(): Boolean {  
    return when (prefs.getString("type", "") ?: "") {  
        "permanent" -> true  
        "hour" -> {  
            val valid = System.currentTimeMillis() < prefs.getLong("until", 0L)  
            if (!valid) prefs.edit().clear().apply()  
            valid  
        }  
        else -> false  
    }  
}  

private fun rounded(color: Int, strokeColor: Int = color, radius: Float = 18f): GradientDrawable {  
    return GradientDrawable().apply {  
        setColor(color)  
        cornerRadius = radius  
        setStroke(1, strokeColor)  
    }  
}  

private fun page(): LinearLayout = LinearLayout(this).apply {  
    orientation = LinearLayout.VERTICAL  
    setPadding(dp(20), dp(18), dp(20), dp(20))  
    setBackgroundColor(bg)  
}  

private fun label(text: String, size: Float = 14f, color: Int = Color.WHITE, bold: Boolean = false): TextView {  
    return TextView(this).apply {  
        this.text = text  
        textSize = size  
        setTextColor(color)  
        gravity = Gravity.CENTER  
        if (bold) typeface = Typeface.create("sans-serif-black", Typeface.BOLD)  
        setPadding(dp(4), dp(6), dp(4), dp(6))  
    }  
}  

private fun actionButton(text: String, primary: Boolean = false, action: () -> Unit): Button {  
    return Button(this).apply {  
        this.text = text  
        textSize = 14f  
        isAllCaps = false  
        setTextColor(if (primary) bg else Color.WHITE)  
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)  
        background = rounded(if (primary) cyan else panel, if (primary) cyan else Color.rgb(31, 65, 87))  
        setOnClickListener { action() }  
        minHeight = dp(48)  
    }  
}  

private fun addGap(parent: LinearLayout, height: Int = 10) {  
    parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))  
}  

private fun showActivation() {  
    val page = page()  
    page.gravity = Gravity.CENTER  
    page.addView(label("ZOMA", 18f, Color.WHITE, true))  
    page.addView(label("XIT", 42f, cyan, true))  
    page.addView(label("ACCESS PORTAL  •  ANDROID", 11f, muted, true))  
    addGap(page, 28)  
    val card = LinearLayout(this).apply {  
        orientation = LinearLayout.VERTICAL  
        setPadding(dp(18), dp(20), dp(18), dp(20))  
        background = rounded(panel, Color.rgb(31, 65, 87), 22f)  
    }  
    card.addView(label("ACTIVATION REQUIRED", 17f, cyan, true))  
    card.addView(label("Enter your access key to continue", 13f, muted))  
    val input = EditText(this).apply {  
        hint = "Enter activation code"  
        setSingleLine(true)  
        setTextColor(Color.WHITE)  
        setHintTextColor(muted)  
        setPadding(dp(14), dp(12), dp(14), dp(12))  
        background = rounded(bg, Color.rgb(31, 65, 87), 12f)  
    }  
    addGap(card, 12)  
    card.addView(input, LinearLayout.LayoutParams(-1, dp(52)))  
    addGap(card, 14)  
    card.addView(actionButton("UNLOCK ACCESS", true) {  
        when (input.text.toString().trim()) {  
            demoHourKey -> {  
                prefs.edit().putString("type", "hour")  
                    .putLong("until", System.currentTimeMillis() + 3600000L).apply()  
                showHome()  
            }  
            demoPermanentKey -> {  
                prefs.edit().putString("type", "permanent").apply()  
                showHome()  
            }  
            else -> Toast.makeText(this, "Invalid demo key", Toast.LENGTH_LONG).show()  
        }  
    }, LinearLayout.LayoutParams(-1, dp(52)))  
    addGap(page, 16)  
    page.addView(card)  
    page.addView(label("Demo keys are stored locally and are not secure.\nWebsite activation requires a real verification API.", 11f, muted))  
    setContentView(page)  
}  

private fun showHome() {  
    rootLayout = page()  
    rootLayout.addView(label("ZOMA XIT", 32f, cyan, true))  
    rootLayout.addView(label("GAMING TOOLS  •  CONTROL CENTER", 10f, muted, true))  
    addGap(rootLayout, 16)  

    val tabs = LinearLayout(this).apply {  
        orientation = LinearLayout.HORIZONTAL  
        background = rounded(panel, Color.rgb(31, 65, 87), 16f)  
        setPadding(dp(4), dp(4), dp(4), dp(4))  
    }  
    listOf("HOME", "FEATURES", "MANAGE").forEach { tab ->  
        val b = TextView(this).apply {  
            text = tab  
            textSize = 11f  
            gravity = Gravity.CENTER  
            typeface = Typeface.DEFAULT_BOLD  
            setTextColor(if (activeTab == tab) bg else muted)  
            background = if (activeTab == tab) rounded(cyan, cyan, 12f) else rounded(Color.TRANSPARENT, Color.TRANSPARENT, 12f)  
            setPadding(dp(8), dp(13), dp(8), dp(13))  
            setOnClickListener {  
                activeTab = tab  
                showHome()  
            }  
        }  
        tabs.addView(b, LinearLayout.LayoutParams(0, dp(44), 1f))  
    }  
    rootLayout.addView(tabs)  
    addGap(rootLayout, 16)  

    when (activeTab) {  
        "HOME" -> showHomeTab(rootLayout)  
        "FEATURES" -> showFeaturesTab(rootLayout)  
        else -> showManageTab(rootLayout)  
    }  

    val spacer = View(this)  
    rootLayout.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))  
    rootLayout.addView(label("ZOMA XIT  •  DEMO BUILD", 10f, muted))  
    setContentView(rootLayout)  
}  

private fun section(parent: LinearLayout, heading: String, subtitle: String = "") {  
    val card = LinearLayout(this).apply {  
        orientation = LinearLayout.VERTICAL  
        setPadding(dp(16), dp(12), dp(16), dp(12))  
        background = rounded(panel, Color.rgb(31, 65, 87), 18f)  
    }  
    card.addView(label(heading, 16f, cyan, true))  
    if (subtitle.isNotEmpty()) card.addView(label(subtitle, 12f, muted))  
    parent.addView(card, LinearLayout.LayoutParams(-1, -2))  
    addGap(parent, 12)  
}  

private fun showHomeTab(parent: LinearLayout) {  
    section(parent, "WELCOME BACK", "Your ZOMA control center is ready.")  
    parent.addView(actionButton("INTER  •  OPEN HAPP PROXY", true) {  
        if (!launchPackage("com.happproxy")) {  
            try {  
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.happproxy")))  
            } catch (_: Exception) {  
                Toast.makeText(this, "Happ Proxy is not installed.", Toast.LENGTH_LONG).show()  
            }  
        }  
    }, LinearLayout.LayoutParams(-1, dp(54)))  
    addGap(parent, 12)  
    parent.addView(actionButton("OPEN FREE FIRE", false) {  
        if (!launchPackage("com.dts.freefireth") && !launchPackage("com.dts.freefiremax")) {  
            Toast.makeText(this, "Free Fire is not installed.", Toast.LENGTH_LONG).show()  
        }  
    }, LinearLayout.LayoutParams(-1, dp(54)))  
    addGap(parent, 12)  
    section(parent, "QUICK STATUS", "Activation is stored locally on this device.")  
}  

private fun showFeaturesTab(parent: LinearLayout) {  
    section(parent, "ZOMA GF", "Generic on-screen circle preview")  
    parent.addView(actionButton(if (circleVisible) "HIDE CIRCLE PREVIEW" else "SHOW CIRCLE PREVIEW", true) {  
        circleVisible = !circleVisible  
        showHome()  
    }, LinearLayout.LayoutParams(-1, dp(52)))  
    addGap(parent, 8)  
    parent.addView(label("Circle size: $circleSize dp", 13f, Color.WHITE))  
    val seek = SeekBar(this).apply {  
        max = 180  
        progress = circleSize - 60  
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {  
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {  
                circleSize = progress + 60  
                if (circleVisible) showHome()  
            }  
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}  
            override fun onStopTrackingTouch(seekBar: SeekBar?) { showHome() }  
        })  
    }  
    parent.addView(seek, LinearLayout.LayoutParams(-1, dp(42)))  
    if (circleVisible) {  
        val preview = TextView(this).apply {  
            text = "○"  
            textSize = (circleSize / 3).toFloat()  
            gravity = Gravity.CENTER  
            setTextColor(cyan)  
            background = rounded(Color.TRANSPARENT, cyan, 1000f)  
        }  
        val size = dp(circleSize)  
        parent.addView(preview, LinearLayout.LayoutParams(size, size).apply { gravity = Gravity.CENTER })  
        parent.addView(label("Preview only — not displayed over other apps.", 11f, muted))  
    }  
    addGap(parent, 12)  
    section(parent, "SAFE CONTROLS", "This build includes UI controls only; it does not automate aiming or modify gameplay.")  
}  

private fun showManageTab(parent: LinearLayout) {  
    section(parent, "MANAGE ACCESS", "Demo activation keys are local-only.")  
    parent.addView(actionButton("LOCK APP / CHANGE KEY", false) {  
        prefs.edit().clear().apply()  
        activeTab = "HOME"  
        showActivation()  
    }, LinearLayout.LayoutParams(-1, dp(52)))  
    addGap(parent, 12)  
    parent.addView(actionButton("OPEN HAPP PROXY", true) {  
        if (!launchPackage("com.happproxy")) {  
            Toast.makeText(this, "Happ Proxy is not installed.", Toast.LENGTH_LONG).show()  
        }  
    }, LinearLayout.LayoutParams(-1, dp(52)))  
    addGap(parent, 12)  
    section(parent, "CONFIGURATION", "Importing a proxy config must be completed inside Happ using a valid config supplied by your provider.")  
}  

private fun launchPackage(pkg: String): Boolean {  
    val intent = packageManager.getLaunchIntentForPackage(pkg) ?: return false  
    return try {  
        startActivity(intent)  
        true  
    } catch (_: Exception) {  
        false  
    }  
}  

private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

}
