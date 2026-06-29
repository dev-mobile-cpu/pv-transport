package com.pv.transport.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class NetworkConnectionInterceptor(
    private val context: Context
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetworkUtils.isInternetAvailable(context)) {
            throw NoInternetException("No Internet Connection")
        }
        return chain.proceed(chain.request())
    }
}