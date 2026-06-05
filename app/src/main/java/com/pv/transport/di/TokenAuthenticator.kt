package com.pv.transport.di

import com.pv.transport.api.AuthenticationService
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.SessionEvents
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authPrefs: AuthPrefs,
    private val authService: AuthenticationService
) : Authenticator {

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

        synchronized(this) {

            if (responseCount(response) >= 2) {
                handleForcedLogout()
                return null
            }

            val currentToken = authPrefs.getAccessToken()

            if (
                response.request.header("Authorization")
                != "Bearer $currentToken"
            ) {

                return response.request.newBuilder()
                    .header(
                        "Authorization",
                        "Bearer $currentToken"
                    )
                    .build()
            }

            val refreshToken =
                authPrefs.getRefreshToken()

            if (refreshToken.isNullOrBlank()) {
                handleForcedLogout()
                return null
            }

            return try {

                val refreshResponse =
                    authService.refreshToken("Bearer $refreshToken").execute()

                if (
                    refreshResponse.isSuccessful &&
                    refreshResponse.body() != null
                ) {

                    val newTokens = refreshResponse.body()!!
                    authPrefs.saveToken(AuthPrefs.KEYS.ACCESS_TOKEN, newTokens.token)

                    response.request.newBuilder()
                        .header(
                            "Authorization",
                            "Bearer ${newTokens.token}"
                        )
                        .build()

                } else {
                    handleForcedLogout()
                    null
                }

            } catch (e: Exception) {
                handleForcedLogout()
                null
            }
        }
    }

    private fun handleForcedLogout() {
        authPrefs.clear()
        SessionEvents.triggerLogout()
    }

    private fun responseCount(
        response: Response
    ): Int {

        var count = 1
        var priorResponse = response.priorResponse

        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }

        return count
    }
}