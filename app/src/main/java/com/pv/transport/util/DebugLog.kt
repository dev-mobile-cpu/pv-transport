package com.pv.transport.util

import android.util.Log
import com.pv.transport.BuildConfig

/**
 * Developer-only tracing. Release builds stay silent, so values that would be unwanted in a
 * user's logcat (tokens, payloads, file paths) can be logged freely through here.
 */
object DebugLog {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.w(tag, message, throwable)
    }
}
