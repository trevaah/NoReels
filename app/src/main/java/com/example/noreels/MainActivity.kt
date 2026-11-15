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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var reelsCounterText: TextView
    private lateinit var xCounterText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
        }

        val statsHeader = TextView(this).apply {
            text = "Activation Counters"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }

        reelsCounterText = TextView(this).apply {
            textSize = 18f
            setPadding(0, 0, 0, 8)
        }

        xCounterText = TextView(this).apply {
            textSize = 18f
            setPadding(0, 0, 0, 32)
        }

        val resetBtn = Button(this).apply {
            text = "Reset Counters"
            setOnClickListener {
                getSharedPreferences("NoReelsPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("reels_count", 0)
                    .putInt("x_block_count", 0)
                    .apply()
                updateCounterDisplay()
            }
        }

        val instructions = TextView(this).apply {
            text = "\nSetup Steps:\n" +
                   "1. Grant Calendar Permission so the app can check if you have a scheduled event right now.\n" +
                   "2. Enable 'Instagram Reels & X Blocker' in Accessibility Settings."
            textSize = 15f
            setPadding(0, 32, 0, 32)
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

        layout.addView(statsHeader)
        layout.addView(reelsCounterText)
        layout.addView(xCounterText)
        layout.addView(resetBtn)
        layout.addView(instructions)
        layout.addView(calendarPermBtn)
        layout.addView(accessibilityBtn)

        setContentView(layout)
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
