package com.seniorsafe.core.util

import android.util.Log

/**
 * Usage:
 *   KeroLog.d("message")                        // [CallerClass] message
 *   KeroLog.d("ActivityMonitorService", "msg")  // [ActivityMonitorService] msg
 *   KeroLog.e("message", throwable)
 *
 * Logcat filter:
 *   adb logcat -s Kero
 */
object KeroLog {

    private const val TAG = "Kero"
    private val keroClass = KeroLog::class.java.name

    fun d(msg: String) = Log.d(TAG, "[${callerTag()}] $msg")
    fun d(tag: String, msg: String) = Log.d(TAG, "[$tag] $msg")

    fun i(msg: String) = Log.i(TAG, "[${callerTag()}] $msg")
    fun i(tag: String, msg: String) = Log.i(TAG, "[$tag] $msg")

    fun w(msg: String) = Log.w(TAG, "[${callerTag()}] $msg")
    fun w(tag: String, msg: String) = Log.w(TAG, "[$tag] $msg")

    fun e(msg: String, tr: Throwable? = null) {
        val text = "[${callerTag()}] $msg"
        if (tr != null) Log.e(TAG, text, tr) else Log.e(TAG, text)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        val text = "[$tag] $msg"
        if (tr != null) Log.e(TAG, text, tr) else Log.e(TAG, text)
    }

    private fun callerTag(): String =
        Thread.currentThread().stackTrace
            .dropWhile { it.className == keroClass || it.className.startsWith("java.lang.Thread") }
            .firstOrNull()
            ?.className
            ?.substringAfterLast('.')
            ?: TAG
}
