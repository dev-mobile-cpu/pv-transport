package com.pv.transport.api

import com.pv.transport.data.log.RefreshResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthenticationService {

//    @POST("/api/auth/refresh-token")
//    @FormUrlEncoded
//    fun refreshTokenApiCall(
//        @Field("refreshToken") refreshToken : String
//    ): Call<RefreshTokenResponse>

    @POST("driver/refresh")
    @FormUrlEncoded
    fun refreshToken(@Field("token")token: String): Call<RefreshResponse>
}