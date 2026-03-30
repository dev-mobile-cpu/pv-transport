package com.pv.transport.di

import okhttp3.Interceptor
import okhttp3.Response
import com.pv.transport.auth.AuthPrefs
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val authPrefs: AuthPrefs) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authPrefs.getAccessToken()

        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(request)
    }
 }


