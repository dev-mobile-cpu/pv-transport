package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.repository.MasterDataRepository
import com.pv.transport.repository.SessionCacheCleaner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authPrefs: AuthPrefs,
    private val masterDataRepository: MasterDataRepository,
    private val sessionCacheCleaner: SessionCacheCleaner
) : ViewModel() {

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _driverId = MutableStateFlow<String?>(null)
    val driverId: StateFlow<String?> = _driverId

    private val _phone = MutableStateFlow<String?>(null)
    val phone: StateFlow<String?> = _phone

    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address

    private val _corporate = MutableStateFlow<String?>(null)
    val corporate: StateFlow<String?> = _corporate

    private val _vehicleName = MutableStateFlow<String?>(null)
    val vehicleName: StateFlow<String?> = _vehicleName

    private val _vehicleType = MutableStateFlow<String?>(null)
    val vehicleType: StateFlow<String?> = _vehicleType

    private val _licensePlate = MutableStateFlow<String?>(null)
    val licensePlate: StateFlow<String?> = _licensePlate

    private val _fuelTypeName = MutableStateFlow<String?>(null)
    val fuelTypeName: StateFlow<String?> = _fuelTypeName

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        viewModelScope.launch {
            _username.value = authPrefs.getUser()
            _driverId.value = authPrefs.getDriverId()
            _phone.value = authPrefs.getPhone()
            _address.value = authPrefs.getAddress()
            _corporate.value = authPrefs.getCorporate()
            _vehicleName.value = authPrefs.getVehicleName()
            _vehicleType.value = authPrefs.getVehicleType()
            _licensePlate.value = authPrefs.getLicensePlate()

            val fuelTypeId = authPrefs.getFuelTypeId()
            _fuelTypeName.value = resolveFuelTypeName(fuelTypeId)
        }
    }

    private suspend fun resolveFuelTypeName(fuelTypeId: String?): String? {
        if (fuelTypeId.isNullOrBlank()) return null
        return try {
            masterDataRepository.getFuelTypes()
                .firstOrNull { it.id.toString() == fuelTypeId.trim() }
                ?.name
                ?.takeUnless { it.isBlank() }
        } catch (_: Exception) {
            null
        }
    }

    fun refresh() {
        loadFromPrefs()
    }

    fun logout() {
        viewModelScope.launch {
            sessionCacheCleaner.clearServerCaches()
            authPrefs.clear()
            authPrefs.saveLogin(false)
        }
    }

    fun saveLanguage(language: String) {
        authPrefs.saveLanguage(language)
    }

    fun getLanguage(): String? {
        return authPrefs.getLanguage()
    }
}
