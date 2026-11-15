package com.example.noreels

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.core.content.ContextCompat

class ReelsBlockerService : AccessibilityService() {

    private var lastReelsRedirectTime = 0L
    private var lastXBlockTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val prefs = getSharedPreferences("NoReelsPrefs", Context.MODE_PRIVATE)
        val toggleReelsAlways = prefs.getBoolean("toggle_reels_always", true)
        val toggleBothCalendar = prefs.getBoolean("toggle_both_calendar", true)

        if (!toggleReelsAlways && !toggleBothCalendar) return

        val now = System.currentTimeMillis()

        if (pkg == "com.twitter.android") {
            if (toggleBothCalendar) {
                handleTwitterLaunch(now)
            }
            return
        }

        if (pkg == "com.instagram.android") {
            val shouldCheckReels = toggleReelsAlways || (toggleBothCalendar && getActiveCalendarEventTitle(now) != null)
            if (!shouldCheckReels) return

            val rootNode = rootInActiveWindow ?: return
            if (isReelsTabSelected(rootNode)) {
                if (now - lastReelsRedirectTime > 700) {
                    lastReelsRedirectTime = now
                    incrementCounter("reels_count")
                    if (!clickHomeNode(rootNode)) {
                        tapHomeCoordinate()
                    }
                }
            }
        }
    }

    private fun handleTwitterLaunch(now: Long) {
        if (now - lastXBlockTime < 1500) return

        val activeEventTitle = getActiveCalendarEventTitle(now)
        if (activeEventTitle != null) {
            lastXBlockTime = now
            incrementCounter("x_block_count")
            performGlobalAction(GLOBAL_ACTION_HOME)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    applicationContext,
                    "X blocked! Scheduled on Calendar: $activeEventTitle",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getActiveCalendarEventTitle(nowMillis: Long): String? {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, nowMillis)
        ContentUris.appendId(builder, nowMillis)

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.SELF_ATTENDEE_STATUS
        )

        val selection = "${CalendarContract.Instances.BEGIN} <= ? AND " +
                        "${CalendarContract.Instances.END} >= ? AND " +
                        "${CalendarContract.Instances.ALL_DAY} = 0 AND " +
                        "${CalendarContract.Instances.SELF_ATTENDEE_STATUS} != ${CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED}"
        val selectionArgs = arrayOf(nowMillis.toString(), nowMillis.toString())

        try {
            contentResolver.query(builder.build(), projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getString(0) ?: "Busy"
                }
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun incrementCounter(key: String) {
        val prefs = getSharedPreferences("NoReelsPrefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    private fun isReelsTabSelected(root: AccessibilityNodeInfo): Boolean {
        val metrics = resources.displayMetrics
        val bottomBarThreshold = (metrics.heightPixels * 0.88).toInt()

        val reelsNodes = root.findAccessibilityNodeInfosByText("Reels")
        for (node in reelsNodes) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (rect.top >= bottomBarThreshold) {
                val desc = node.contentDescription?.toString() ?: ""
                if (desc.contains("Reels", ignoreCase = true)) {
                    if (node.isSelected || node.parent?.isSelected == true) {
                        return true
                    }
                }
            }
        }

        val tabIds = listOf(
            "com.instagram.android:id/clips_tab",
            "com.instagram.android:id/reel_tab"
        )
        for (id in tabIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            for (node in nodes) {
                if (node.isSelected || node.parent?.isSelected == true) {
                    return true
                }
            }
        }

        return false
    }

    private fun clickHomeNode(root: AccessibilityNodeInfo): Boolean {
        val metrics = resources.displayMetrics
        val bottomBarThreshold = (metrics.heightPixels * 0.88).toInt()

        val feedNodes = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/feed_tab")
        for (node in feedNodes) {
            if (node.isClickable) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (node.parent?.isClickable == true) return node.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        val homeNodes = root.findAccessibilityNodeInfosByText("Home")
        for (node in homeNodes) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (rect.top >= bottomBarThreshold && node.contentDescription?.toString()?.equals("Home", ignoreCase = true) == true) {
                if (node.isClickable) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (node.parent?.isClickable == true) return node.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
        return false
    }

    private fun tapHomeCoordinate() {
        val metrics = resources.displayMetrics
        val x = metrics.widthPixels * 0.10f
        val y = metrics.heightPixels * 0.975f

        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()

        dispatchGesture(gesture, null, null)
    }

    override fun onInterrupt() {}
}
