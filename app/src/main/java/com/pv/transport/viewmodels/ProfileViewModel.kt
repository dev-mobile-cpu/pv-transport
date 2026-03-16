package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.auth.AuthPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authPrefs: AuthPrefs
) : ViewModel() {

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _driverId = MutableStateFlow<String?>(null)
    val driverId: StateFlow<String?> = _driverId

    private val _phone = MutableStateFlow<String?>(null)
    val phone: StateFlow<String?> = _phone

    private val _licensePlate = MutableStateFlow<String?>(null)
    val licensePlate: StateFlow<String?> = _licensePlate

    private val _createdAt = MutableStateFlow<String?>(null)
    val createdAt: StateFlow<String?> = _createdAt

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        viewModelScope.launch {
            _username.value = authPrefs.getUserName()
            _driverId.value = authPrefs.getDriverId()
            _phone.value = authPrefs.getPhone()
            _licensePlate.value = authPrefs.getLicensePlate()
            _createdAt.value = authPrefs.getCreatedAt()
        }
    }

    fun refresh() {
        loadFromPrefs()
    }

    fun logout() {
        viewModelScope.launch {
            authPrefs.clear()
            authPrefs.saveLogin(false)


            // you may want to notify UI or navigate; keep logic minimal here
        }
    }
}