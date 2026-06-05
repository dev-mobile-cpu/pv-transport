package com.pv.transport.repository

import android.content.Context
import com.pv.transport.api.AuthApi
import com.pv.transport.data.CheckVersionResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

class CheckVersionRepository @Inject constructor(
    private val api: AuthApi,
    @ApplicationContext private val  context: Context
){
    suspend fun checkVersion(): Response<CheckVersionResponse> {
         return api.getCheckVersion()
    }
}