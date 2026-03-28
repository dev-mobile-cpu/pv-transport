package com.pv.transport.di

import android.util.Log
import com.pv.transport.auth.AuthPrefs
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
        Log.d("token","Bearer ${bearerToken}")
        //builder.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3OTkwMzI5MGNlZDdiODA2YTQ2MDBjOCIsImlzU2VydmljZUFkbWluIjp0cnVlLCJsb2dpbklkIjoiU0VSMDEzNTc2IiwiYWdlbnRDb2RlIjoiQUczNDg5MDkiLCJpYXQiOjE3NTkzNzY3MTB9.hk5MOY0GGaP6WyooNlvtjuUm-6Xm9WwoWuhXt7UxwzU")

        if (bearerToken != "")
            builder.addHeader("Authorization", "Bearer $bearerToken")


        val request = builder.build()
        val response = chain.proceed(request)
//        if (response.code == 401) {
//            // Token expired, refresh it
//            response.close()
//            //var secondResponse : Response? = null
//            synchronized(this) {
//                val newToken: String? = tokenManager.refreshTokenSync()
//                if (newToken != null) {
//                    // Retry the request with the new token
//                    showLogD("AUTH_INSPECTOR", newToken)
//                    val newRequest: Request = originalRequest.newBuilder()
//                        .header("Authorization", "Bearer $newToken")
//                        .build()
//                    //showLogI("AUTH_INSPECTOR", "2st ${secondResponse!!.code}")
//                    val secondResponse = chain.proceed(newRequest)
//                    showLogI("AUTH_INSPECTOR", "3rd ${secondResponse.code}")
//                    return secondResponse
//                }
//            }
//        }
//        showLogI("AUTH_INSPECTOR", "1st ${response.code}")
        return response
    }

}
