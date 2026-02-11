package com.github.kanataidarov.ytskipper.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

@SuppressLint("AccessibilityPolicy")
class ScreenClickerService : AccessibilityService() {

    companion object {
        private const val TAG = "ScreenClickerService"
        const val PREFS_NAME = "ytskipper_prefs"
        const val KEY_TARGET_TEXT = "target_text"
        const val DEFAULT_TARGET_TEXT = "Google"
    }

    private lateinit var prefs: SharedPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) return

        val targetText = prefs.getString(KEY_TARGET_TEXT, DEFAULT_TARGET_TEXT) ?: return
        if (targetText.isBlank()) return

        val rootNode = rootInActiveWindow ?: return
        findAndClickTarget(rootNode, targetText)
    }

    private fun findAndClickTarget(node: AccessibilityNodeInfo, targetText: String): Boolean {
        val nodeText = node.text?.toString()
        if (nodeText != null && nodeText.trim() == targetText) {
            Log.d(TAG, "Found target text: \"$targetText\"")
            if (performClickOnNodeOrParent(node)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickTarget(child, targetText)) {
                return true
            }
        }
        return false
    }

    private fun performClickOnNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            val result = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Clicked on node directly, result=$result")
            return result
        }

        var current = node.parent
        while (current != null) {
            if (current.isClickable) {
                val result = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Clicked on ancestor node, result=$result")
                return result
            }
            current = current.parent
        }
        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility service destroyed")
    }
}
