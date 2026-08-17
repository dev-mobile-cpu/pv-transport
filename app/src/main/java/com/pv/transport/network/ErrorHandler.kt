package com.pv.transport.network

import com.google.gson.JsonParser
import retrofit2.Response
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

    /**
     * Single place to turn a failed HTTP response into a user-facing message.
     * Prefers the real server message (`message` / `error` / `detail` / first
     * validation error) and falls back to HTTP reason + code.
     */
    fun fromResponse(response: Response<*>): String {
        val body = try {
            response.errorBody()?.string()
        } catch (e: Exception) {
            null
        }

        if (!body.isNullOrBlank()) {
            try {
                val json = JsonParser.parseString(body)
                if (json.isJsonObject) {
                    val obj = json.asJsonObject

                    for (key in listOf("message", "error", "detail")) {
                        val el = obj.get(key)
                        if (el != null && !el.isJsonNull && el.isJsonPrimitive) {
                            val text = el.asString
                            if (text.isNotBlank()) return text
                        }
                    }

                    // Validation shape: {"errors": {"field": ["first message"]}}
                    val errors = obj.get("errors")
                    if (errors != null && errors.isJsonObject) {
                        val first = errors.asJsonObject.entrySet().firstOrNull()?.value
                        if (first != null && first.isJsonArray && first.asJsonArray.size() > 0) {
                            val text = first.asJsonArray[0].asString
                            if (text.isNotBlank()) return text
                        }
                    }
                }
            } catch (_: Exception) {
                // fall through to generic message
            }
        }

        val reason = response.message()
        return if (reason.isNullOrBlank()) {
            "Request failed (${response.code()})"
        } else {
            "$reason (${response.code()})"
        }
    }
}