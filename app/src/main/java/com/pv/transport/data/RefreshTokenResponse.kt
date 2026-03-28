package com.pv.transport.data

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    override val message: String = "",
    override val data: TokenData? = null,
    override val isSuccess: Boolean
) : BaseResponse<TokenData>()

data class TokenData(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("accessTokenExpiredAt")
    val accessTokenExpiredAt: String,
    @SerializedName("refreshTokenExpiredAt")
    val refreshTokenExpiredAt: String
)
