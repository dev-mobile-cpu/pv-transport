package com.pv.transport.api

import com.pv.transport.data.LoginResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface TransportApi {

    @POST("driver/login")
    suspend fun login(@Query("login_id")loginId: String, @Query("password")password: String): Response<LoginResponse>

}