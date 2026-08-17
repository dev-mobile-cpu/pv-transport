package com.pv.transport.di

import com.pv.transport.auth.AuthPrefs
import com.pv.transport.util.DebugLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.Throws

class RefreshTokenInterceptor(private val pref: AuthPrefs) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val bearerToken: String? = pref.load(AuthPrefs.KEYS.ACCESS_TOKEN)
        val builder = chain.request().newBuilder()

        builder.addHeader("Accept", "application/json")
        DebugLog.d("Bearer Token", "Bearer ${bearerToken}")
        if (bearerToken != null && bearerToken != "")
            builder.addHeader("Authorization", "Bearer $bearerToken")

        val request = builder.build()
        val response = chain.proceed(request)
        DebugLog.d("AUTH_INSPECTOR", "2nd ${response.code}")
        return response
    }
}
