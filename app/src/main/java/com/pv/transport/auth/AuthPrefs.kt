package com.pv.transport.auth

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.pv.transport.data.LoginResponse
import com.pv.transport.data.Driver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPrefs @Inject constructor(
    private val prefs: SharedPreferences,
) {
    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USERNAME_KEY = "user_name"
        private const val DRIVER_ID_KEY = "driver_id"
        private const val PHONE_KEY = "phone" // example for token expiry
        private const val LICENSE_PLATE_KEY = "license_plate"
        private const val CREATED_AT = "created_at"
        private const val LOGIN_IN = "isLoggedIn"
    }


    fun saveAccessToken(accessToken: String) {
        prefs.edit {
            putString(ACCESS_TOKEN_KEY, accessToken)
        }
    }

    fun saveRefreshToken( refreshToken: String){
        prefs.edit {
            putString(REFRESH_TOKEN_KEY, refreshToken)
        }
    }

    fun saveDriver(driver: Driver) {
        prefs.edit {
            putString(USERNAME_KEY, driver.name)
            putString(DRIVER_ID_KEY, driver.id)
            putString(PHONE_KEY, driver.phone)
            putString(LICENSE_PLATE_KEY, driver.phone)
            putString(CREATED_AT, driver.createdAt)
        }
    }

    fun saveLogin(login: Boolean) {
        prefs.edit {
            putBoolean(LOGIN_IN, login)
        }
    }

    fun getAccessToken(): String? =
        prefs.getString(ACCESS_TOKEN_KEY, null)

    fun getRefreshToken(): String? =
        prefs.getString(REFRESH_TOKEN_KEY, null)
    fun getUserName(): String? = prefs.getString(USERNAME_KEY, null)
    fun getDriverId(): String? = prefs.getString(DRIVER_ID_KEY, null)
    fun getPhone(): String? = prefs.getString(PHONE_KEY, null)
    fun getLicensePlate(): String? = prefs.getString(LICENSE_PLATE_KEY, null)
    fun getCreatedAt(): String? = prefs.getString(CREATED_AT, null)

    fun clear() {
        prefs.edit {
            remove(ACCESS_TOKEN_KEY)
            remove(USERNAME_KEY)
            remove(DRIVER_ID_KEY)
            remove(PHONE_KEY)
            remove(LICENSE_PLATE_KEY)
            remove(LOGIN_IN)
            remove(CREATED_AT)
        }
    }
}
