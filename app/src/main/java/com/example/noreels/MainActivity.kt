package com.example.noreels

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var reelsCounterText: TextView
    private lateinit var xCounterText: TextView
    private lateinit var toggleAlwaysReels: Switch
    private lateinit var toggleCalendarBoth: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("NoReelsPrefs", Context.MODE_PRIVATE)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
        }

        val modesHeader = TextView(this).apply {
            text = "Blocking Modes"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }

        toggleAlwaysReels = Switch(this).apply {
            text = "1. Block Reels Only (Always)"
            textSize = 16f
            setPadding(0, 16, 0, 16)
            isChecked = prefs.getBoolean("toggle_reels_always", true)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("toggle_reels_always", isChecked).apply()
            }
        }

        toggleCalendarBoth = Switch(this).apply {
            text = "2. Block Both Reels & X (During Calendar Events)"
            textSize = 16f
            setPadding(0, 16, 0, 32)
            isChecked = prefs.getBoolean("toggle_both_calendar", true)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("toggle_both_calendar", isChecked).apply()
            }
        }

        val statsHeader = TextView(this).apply {
            text = "Activation Counters"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 16, 0, 16)
        }

        reelsCounterText = TextView(this).apply {
            textSize = 18f
            setPadding(0, 0, 0, 8)
        }

        xCounterText = TextView(this).apply {
            textSize = 18f
            setPadding(0, 0, 0, 24)
        }

        val resetBtn = Button(this).apply {
            text = "Reset Counters"
            setOnClickListener {
                prefs.edit()
                    .putInt("reels_count", 0)
                    .putInt("x_block_count", 0)
                    .apply()
                updateCounterDisplay()
            }
        }

        val instructions = TextView(this).apply {
            text = "\nSetup Steps:\n" +
                   "1. Grant Calendar Permission so Toggle 2 can check your schedule.\n" +
                   "2. Enable 'Instagram Reels & X Blocker' in Accessibility Settings."
            textSize = 15f
            setPadding(0, 32, 0, 24)
        }

        val calendarPermBtn = Button(this).apply {
            text = "1. Grant Calendar Permission"
            setOnClickListener {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.READ_CALENDAR),
                        101
                    )
                } else {
                    Toast.makeText(this@MainActivity, "Calendar permission already granted!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val accessibilityBtn = Button(this).apply {
            text = "2. Open Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(modesHeader)
        layout.addView(toggleAlwaysReels)
        layout.addView(toggleCalendarBoth)
        layout.addView(statsHeader)
        layout.addView(reelsCounterText)
        layout.addView(xCounterText)
        layout.addView(resetBtn)
        layout.addView(instructions)
        layout.addView(calendarPermBtn)
        layout.addView(accessibilityBtn)

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    override fun onResume() {
        super.onResume()
        updateCounterDisplay()
    }

    private fun updateCounterDisplay() {
        val prefs = getSharedPreferences("NoReelsPrefs", Context.MODE_PRIVATE)
        val reelsCount = prefs.getInt("reels_count", 0)
        val xCount = prefs.getInt("x_block_count", 0)
        reelsCounterText.text = "Reels Redirected: $reelsCount times"
        xCounterText.text = "X (Twitter) Blocked: $xCount times"
    }
}
