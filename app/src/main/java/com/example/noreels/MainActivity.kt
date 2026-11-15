package com.example.noreels

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
        }

        val instructions = TextView(this).apply {
            text = "1. Tap the button below to open Accessibility Settings.\n\n" +
                   "2. Tap 'Installed apps' -> 'Instagram Reels Redirect' and turn it ON.\n\n" +
                   "Note: On Android 13+, if the toggle is greyed out ('Restricted setting'), " +
                   "open Settings -> Apps -> NoReels -> tap the 3 dots in the top-right corner -> 'Allow restricted settings'."
            textSize = 16f
            setPadding(0, 0, 0, 48)
        }

        val button = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(instructions)
        layout.addView(button)
        setContentView(layout)
    }
}
