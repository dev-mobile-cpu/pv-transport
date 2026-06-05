package com.pv.transport.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import com.pv.transport.R
import com.pv.transport.data.log.Driver
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
        private const val USER = "user"
        private const val USERNAME_KEY = "user_name"
        private const val PASSWORD = "password"
        private const val DRIVER_ID_KEY = "driver_id"
        private const val PHONE_KEY = "phone" // example for token expiry
        private const val DRIVER_TYPE = "driver_type"
        private const val FUEL_TYPE_ID = "fuel_type_id"
        private const val LICENSE_PLATE_KEY = "license_plate"
        private const val CREATED_AT = "created_at"
        private const val LOGIN_IN = "isLoggedIn"
        private const val LANGUAGE_KEY = "language"
    }

    fun load(key : KEYS) : String? {
        return sharedPreferences.getString(context.getString(key.label) , key.defaultValue)
    }

     enum class KEYS(val label : Int , val defaultValue : String) {
         ACCESS_TOKEN(R.string.pref_access_token, ""),
         REFRESH_TOKEN(R.string.pref_refresh_token, ""),

     }


//    fun saveToken(token: String) {
//        sharedPreferences.edit {
//            putString(TOKEN_KEY, token)
//        }
//    }

     fun saveToken(key: KEYS, value: String) {
         sharedPreferences.edit {
             putString(context.getString(key.label), value)
         }
     }

    /**
     * Save driver object as JSON
     */
    fun saveDriver(driver: Driver) {
        sharedPreferences.edit {
            putString(USER, driver.name)
            putString(DRIVER_ID_KEY, driver.id)
            putString(PHONE_KEY, driver.phone)
            putString(DRIVER_TYPE, driver.driverType)
            putString(FUEL_TYPE_ID, driver.fuelTypeId)
            putString(LICENSE_PLATE_KEY, driver.licensePlate)
            putString(CREATED_AT, driver.createdAt)
        }
    }

    fun saveLogin(login: Boolean) {
        sharedPreferences.edit {
            putBoolean(LOGIN_IN, login)
        }
    }
     fun saveUserName(userName: String){
         sharedPreferences.edit {
             putString(USERNAME_KEY,userName)
         }
     }
     fun savePassword(password: String){
         sharedPreferences.edit {
             putString(PASSWORD,password)
         }
     }

    fun saveLanguage(language: String) {
        sharedPreferences.edit {
            putString(LANGUAGE_KEY, language)
        }
    }

     fun getAccessToken(): String? = load(KEYS.ACCESS_TOKEN)
     fun getRefreshToken(): String? = load(KEYS.REFRESH_TOKEN)

     fun isLoggedIn(): Boolean = sharedPreferences.getBoolean(LOGIN_IN, false)
     fun getUser(): String? = sharedPreferences.getString(USER,null)
     fun getUserName(): String = sharedPreferences.getString(USERNAME_KEY, "") ?: ""
     fun getPassword(): String = sharedPreferences.getString(PASSWORD, "") ?: ""

    fun getDriverId(): String? = sharedPreferences.getString(DRIVER_ID_KEY, null)
    fun getPhone(): String? = sharedPreferences.getString(PHONE_KEY, null)
    fun getDriverType(): String? = sharedPreferences.getString(DRIVER_TYPE, null)
    fun getFuelTypeId(): String? = sharedPreferences.getString(FUEL_TYPE_ID, null)
    fun getLicensePlate(): String? = sharedPreferences.getString(LICENSE_PLATE_KEY, null)
    fun getCreatedAt(): String? = sharedPreferences.getString(CREATED_AT, null)
    fun getLanguage(): String? = sharedPreferences.getString(LANGUAGE_KEY, "en") // default to English


    fun clear() {
        sharedPreferences.edit {
            remove(context.getString(KEYS.ACCESS_TOKEN.label))
            remove(USER)
            remove(DRIVER_ID_KEY)
            remove(PHONE_KEY)
            remove(DRIVER_TYPE)
            remove(FUEL_TYPE_ID)
            remove(LICENSE_PLATE_KEY)
            remove(LOGIN_IN)
            remove(CREATED_AT)
        }
    }
}
