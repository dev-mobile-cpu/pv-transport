package com.pv.transport.network

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.jvm.Throws

class NetworkExceptionInterceptor : Interceptor {

    @Throws()
    override fun intercept(chain : Interceptor.Chain) : Response {

//        val response = chain.proceed(chain.request())
//
//        when (response.isSuccessful) {
//            true  -> return response
//            false -> {
//                throw NetworkException(response.body, response.code)
//            }
//        }

        val response = chain.proceed(chain.request())

        if (response.isSuccessful) {
            return response
        }

        val errorBodyString = response.body.string()

        throw NetworkException(
            errorBody = errorBodyString,
            errorCode = response.code
        )

    }
}
