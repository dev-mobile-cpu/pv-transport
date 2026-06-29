package com.pv.transport.network

import java.io.IOException
import java.net.SocketTimeoutException

object ErrorHandler {

    fun getMessage(e: Throwable): String {
        return when (e) {

            is NoInternetException ->
                "No internet connection"

            is SocketTimeoutException ->
                "Connection timeout"

            is IOException ->
                "Network error. Please check your connection"

            else ->
                e.message ?: "Something went wrong"
        }
    }
}