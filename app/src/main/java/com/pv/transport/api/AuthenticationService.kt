package com.pv.transport.api

import com.pv.transport.data.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthenticationService {

    @POST("/api/auth/refresh-token")
    @FormUrlEncoded
    fun refreshTokenApiCall(
        @Field("refreshToken") refreshToken : String
    ): Call<RefreshTokenResponse>
}