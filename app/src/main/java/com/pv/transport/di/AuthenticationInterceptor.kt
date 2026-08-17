package com.pv.transport.di

import com.pv.transport.auth.AuthPrefs
import com.pv.transport.util.DebugLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthenticationInterceptor(
    private val preference: AuthPrefs
) : Interceptor {


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
     val bearerToken: String = preference.load(AuthPrefs.KEYS.ACCESS_TOKEN) ?: ""

        //val builder = chain.request().newBuilder()
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
        builder.addHeader("Accept", "application/json")
        DebugLog.d("token","Bearer ${bearerToken}")
        //builder.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3OTkwMzI5MGNlZDdiODA2YTQ2MDBjOCIsImlzU2VydmljZUFkbWluIjp0cnVlLCJsb2dpbklkIjoiU0VSMDEzNTc2IiwiYWdlbnRDb2RlIjoiQUczNDg5MDkiLCJpYXQiOjE3NTkzNzY3MTB9.hk5MOY0GGaP6WyooNlvtjuUm-6Xm9WwoWuhXt7UxwzU")

        if (bearerToken != "")
            builder.addHeader("Authorization", "Bearer $bearerToken")


        val request = builder.build()
        val response = chain.proceed(request)
        return response
    }

}
