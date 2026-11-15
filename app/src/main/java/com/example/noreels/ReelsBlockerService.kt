package com.example.noreels

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ReelsBlockerService : AccessibilityService() {

    private var lastRedirectTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return

        if (isReelsTabSelected(rootNode)) {
            val now = System.currentTimeMillis()
            if (now - lastRedirectTime > 700) {
                lastRedirectTime = now
                if (!clickHomeNode(rootNode)) {
                    tapHomeCoordinate()
                }
            }
        }
    }

    private fun isReelsTabSelected(root: AccessibilityNodeInfo): Boolean {
        val metrics = resources.displayMetrics
        val bottomBarThreshold = (metrics.heightPixels * 0.88).toInt()

        // Method 1: Check by contentDescription / text "Reels" in bottom navigation bar
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

        // Method 2: Check by Instagram's internal tab view ID if available
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

        // Try Instagram's feed_tab ID first
        val feedNodes = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/feed_tab")
        for (node in feedNodes) {
            if (node.isClickable) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (node.parent?.isClickable == true) return node.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        // Try contentDescription "Home" in bottom bar
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
        // Matches the bottom-left Home icon coordinate (10% X, 97.5% Y)
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
