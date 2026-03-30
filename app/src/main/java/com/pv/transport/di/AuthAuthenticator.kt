package com.pv.transport.di

import com.pv.transport.api.AuthApi
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.auth.AuthManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named

class AuthAuthenticator @Inject constructor(

    private val authPrefs: AuthPrefs,
    @Named("refreshApi") private val apiService: AuthApi,
    private val authManager: AuthManager
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val oldToken = authPrefs.getAccessToken() ?: return null

        val refreshResponse = try {
            apiService.refreshToken(oldToken)
        } catch (e: Exception) {
            return null
        }

        if (refreshResponse.isSuccessful) {

            val newToken = refreshResponse.body()?.token

            if (!newToken.isNullOrEmpty()) {
                authPrefs.saveAccessToken(newToken)
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }
        }
        authPrefs.clear()
        // Emit logout event
        runBlocking { authManager.logout() }
        return null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var res = response.priorResponse

        while (res != null) {
            count++
            res = res.priorResponse
        }

        return count
    }

}
