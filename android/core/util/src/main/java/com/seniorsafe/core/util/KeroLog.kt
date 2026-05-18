package com.seniorsafe.core.util

import android.util.Log

/**
 * Usage:
 *   KeroLog.d("message")              // tag = 호출한 클래스 이름 (자동)
 *   KeroLog.d("MyTag", "message")     // tag = 명시
 *   KeroLog.e("message", throwable)
 *
 * Filter in logcat:
 *   adb logcat -s Kero
 */
object KeroLog {

    private const val TAG = "Kero"
    private val keroClass = KeroLog::class.java.name

    fun d(msg: String) = print(Log.DEBUG, callerTag(), msg)
    fun d(tag: String, msg: String) = print(Log.DEBUG, tag, msg)

    fun i(msg: String) = print(Log.INFO, callerTag(), msg)
    fun i(tag: String, msg: String) = print(Log.INFO, tag, msg)

    fun w(msg: String) = print(Log.WARN, callerTag(), msg)
    fun w(tag: String, msg: String) = print(Log.WARN, tag, msg)

    fun e(msg: String, tr: Throwable? = null) = print(Log.ERROR, callerTag(), msg, tr)
    fun e(tag: String, msg: String, tr: Throwable? = null) = print(Log.ERROR, tag, msg, tr)

    private fun print(level: Int, tag: String, msg: String, tr: Throwable? = null) {
        val text = "[$tag] $msg"
        when (level) {
            Log.DEBUG -> Log.d(TAG, text)
            Log.INFO  -> Log.i(TAG, text)
            Log.WARN  -> Log.w(TAG, text)
            Log.ERROR -> if (tr != null) Log.e(TAG, text, tr) else Log.e(TAG, text)
        }
    }

    private fun callerTag(): String =
        Thread.currentThread().stackTrace
            .dropWhile { it.className == keroClass || it.className.startsWith("java.lang.Thread") }
            .firstOrNull()
            ?.className
            ?.substringAfterLast('.')
            ?: "Unknown"
}
