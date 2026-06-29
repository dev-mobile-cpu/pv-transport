package com.pv.transport.network

import okhttp3.ResponseBody
import java.io.IOException

data class NetworkException(
    val errorBody: String? = null,
    var errorCode: Int
) : IOException()