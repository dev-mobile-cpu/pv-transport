package com.pv.transport.auth

import android.content.Context
import android.util.Log
import com.pv.transport.api.AuthenticationService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val authPrefs: AuthPrefs,
    private val apiService: AuthenticationService // Retrofit service for refresh
) {

    private val lock = Any()

    fun getCurrentToken(): String? {
        return authPrefs.load(AuthPrefs.KEYS.ACCESS_TOKEN)


    }

    /**
     * Synchronous token refresh
     * Returns new token if successful, null if failed
     */
    fun refreshTokenSync(): String? {
        synchronized(lock) {
            val oldToken = getCurrentToken() ?: return null
            Log.e("TokenManager", "Attempting to refresh token: $oldToken")

            return try {
                val response = apiService.refreshToken(oldToken).execute()
                if (response.isSuccessful) {
                    val newToken = response.body()?.token
                    Log.e("TokenManager", "Refresh successful, new token: $newToken")
                    if (!newToken.isNullOrEmpty()) {
                        authPrefs.saveToken(AuthPrefs.KEYS.ACCESS_TOKEN, newToken)
                        newToken
                    } else null
                } else {
                    Log.w("TokenManager", "Refresh failed: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("TokenManager", "Refresh exception", e)
                null
            }
        }
    }
}