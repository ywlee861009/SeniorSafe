package com.kero.anbu.core.util

import android.util.Log

/**
 * Usage:
 *   KeroLog.d("message")
 *   KeroLog.e("message", throwable)
 *
 * Logcat filter:
 *   adb logcat -s Kero
 */
object KeroLog {

    private const val TAG = "Kero"

    fun d(msg: String) = Log.d(TAG, msg)

    fun i(msg: String) = Log.i(TAG, msg)

    fun w(msg: String) = Log.w(TAG, msg)

    fun e(msg: String, tr: Throwable? = null) {
        if (tr != null) Log.e(TAG, msg, tr) else Log.e(TAG, msg)
    }
}
