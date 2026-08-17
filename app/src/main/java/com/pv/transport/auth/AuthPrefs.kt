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
        private const val ADDRESS_KEY = "address"
        private const val CORPORATE_KEY = "corporate"
        private const val VEHICLE_NAME_KEY = "vehicle_name"
        private const val VEHICLE_TYPE_KEY = "vehicle_type"
        private const val CREATED_AT = "created_at"
        private const val LOGIN_IN = "isLoggedIn"
        private const val LANGUAGE_KEY = "language"
        private const val FORCE_UPDATE = "force_update"
        private const val SKIPPED_VERSION_CODE = "skipped_version_code"
        private const val INITIAL_DATA_UPDATE = "initial_data_update"
        private const val INITIAL_DATA_SYNCED_AT = "initial_data_synced_at"
        private const val INITIAL_DATA_ROW_COUNT = "initial_data_row_count"
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
            putString(LICENSE_PLATE_KEY, driver.resolvedLicensePlate)
            putString(ADDRESS_KEY, driver.address)
            putString(CORPORATE_KEY, driver.corporate)
            putString(VEHICLE_NAME_KEY, driver.vehicleName)
            putString(VEHICLE_TYPE_KEY, driver.vehicleType)
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
    fun saveForceUpdate(forceUpdate: Boolean) {
        sharedPreferences.edit {
            putBoolean(FORCE_UPDATE, forceUpdate)
        }
    }

    /** "Later" only skips this specific version; a newer release shows the sheet again. */
    fun saveSkippedVersionCode(versionCode: Int) {
        sharedPreferences.edit {
            putInt(SKIPPED_VERSION_CODE, versionCode)
        }
    }

    fun getSkippedVersionCode(): Int = sharedPreferences.getInt(SKIPPED_VERSION_CODE, -1)

    /**
     * One successful master data download: the server cursor to send as `since` next time, how
     * many rows it left cached, and when it happened on this device.
     */
    fun saveInitialDataSync(update: Long?, rowCount: Int) {
        sharedPreferences.edit {
            update?.let { putLong(INITIAL_DATA_UPDATE, it) }
            putInt(INITIAL_DATA_ROW_COUNT, rowCount)
            putLong(INITIAL_DATA_SYNCED_AT, System.currentTimeMillis())
        }
    }

    /** Null until the master data has been downloaded at least once. */
    fun getInitialDataUpdate(): Long? =
        sharedPreferences.getLong(INITIAL_DATA_UPDATE, 0L).takeIf { it > 0L }

    /** Device clock, not the server cursor — 0 when no download has ever succeeded. */
    fun getInitialDataSyncedAt(): Long = sharedPreferences.getLong(INITIAL_DATA_SYNCED_AT, 0L)

    /** -1 when unknown, so an install from before this was recorded keeps the old behaviour. */
    fun getInitialDataRowCount(): Int = sharedPreferences.getInt(INITIAL_DATA_ROW_COUNT, -1)

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
    fun getAddress(): String? = sharedPreferences.getString(ADDRESS_KEY, null)
    fun getCorporate(): String? = sharedPreferences.getString(CORPORATE_KEY, null)
    fun getVehicleName(): String? = sharedPreferences.getString(VEHICLE_NAME_KEY, null)
    fun getVehicleType(): String? = sharedPreferences.getString(VEHICLE_TYPE_KEY, null)
    fun getCreatedAt(): String? = sharedPreferences.getString(CREATED_AT, null)
    fun getLanguage(): String? = sharedPreferences.getString(LANGUAGE_KEY, "en") // default to English

    fun getForceUpdate(): Boolean = sharedPreferences.getBoolean(FORCE_UPDATE, false)

    fun clear() {
        sharedPreferences.edit {
            remove(context.getString(KEYS.ACCESS_TOKEN.label))
            remove(USER)
            remove(DRIVER_ID_KEY)
            remove(PHONE_KEY)
            remove(DRIVER_TYPE)
            remove(FUEL_TYPE_ID)
            remove(LICENSE_PLATE_KEY)
            remove(ADDRESS_KEY)
            remove(CORPORATE_KEY)
            remove(VEHICLE_NAME_KEY)
            remove(VEHICLE_TYPE_KEY)
            remove(LOGIN_IN)
            remove(CREATED_AT)
            remove(INITIAL_DATA_UPDATE)
            remove(INITIAL_DATA_SYNCED_AT)
            remove(INITIAL_DATA_ROW_COUNT)
        }
    }
}
