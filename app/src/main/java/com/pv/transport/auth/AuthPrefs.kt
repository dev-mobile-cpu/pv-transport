package com.pv.transport.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.pv.transport.R
import com.pv.transport.data.Driver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPrefs @Inject constructor(
 private val context : Context)
 {

     private val PREFERENCES_NAME = "IMessagePreference"

     private val sharedPreferences : SharedPreferences =
         context.applicationContext.getSharedPreferences(PREFERENCES_NAME , Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "auth_token"
        private const val USERNAME_KEY = "user_name"
        private const val DRIVER_ID_KEY = "driver_id"
        private const val PHONE_KEY = "phone" // example for token expiry
        private const val LICENSE_PLATE_KEY = "license_plate"
        private const val CREATED_AT = "created_at"
        private const val LOGIN_IN = "isLoggedIn"
    }

    fun load(key : KEYS) : String? {
        return sharedPreferences.getString(context.getString(key.label) , key.defaultValue)
    }

     enum class KEYS(val label : Int , val defaultValue : String) {
         ACCESS_TOKEN(R.string.pref_access_token, ""),
         REFRESH_TOKEN(R.string.pref_refresh_token, ""),

     }


    fun saveToken(token: String) {
        sharedPreferences.edit {
            putString(TOKEN_KEY, token)
        }
    }

    /**
     * Save driver object as JSON
     */
    fun saveDriver(driver: Driver) {
        sharedPreferences.edit {
            putString(USERNAME_KEY, driver.name)
            putString(DRIVER_ID_KEY, driver.id)
            putString(PHONE_KEY, driver.phone)
            putString(LICENSE_PLATE_KEY, driver.phone)
            putString(CREATED_AT, driver.createdAt)
        }
    }

    fun saveLogin(login: Boolean) {
        sharedPreferences.edit {
            putBoolean(LOGIN_IN, login)
        }
    }

    fun getToken(): String? = sharedPreferences.getString(TOKEN_KEY, null)
    fun getUserName(): String? = sharedPreferences.getString(USERNAME_KEY, null)
    fun getDriverId(): String? = sharedPreferences.getString(DRIVER_ID_KEY, null)
    fun getPhone(): String? = sharedPreferences.getString(PHONE_KEY, null)
    fun getLicensePlate(): String? = sharedPreferences.getString(LICENSE_PLATE_KEY, null)
    fun getCreatedAt(): String? = sharedPreferences.getString(CREATED_AT, null)


    fun clear() {
        sharedPreferences.edit {
            remove(TOKEN_KEY)
            remove(USERNAME_KEY)
            remove(DRIVER_ID_KEY)
            remove(PHONE_KEY)
            remove(LICENSE_PLATE_KEY)
            remove(LOGIN_IN)
            remove(CREATED_AT)

        }
    }
}
