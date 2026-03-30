package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.auth.AuthManager
import com.pv.transport.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.pv.transport.data.LoginResponse

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: LoginResponse) : AuthState()
    object InvalidCredentials : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val authPrefs: AuthPrefs,
    private val authManager: AuthManager // Inject AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authManager.logoutEvent.collect {
                _logoutEvent.emit(Unit)
            }
        }
    }

    fun login(username: String, password: String) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = repo.login(username, password)
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        // save token and driver's license plate separately
                        authPrefs.saveAccessToken(result.token)
                        authPrefs.saveLogin(true)
                        authPrefs.saveDriver(result.driver)

                        println("Hey Token----${result.token}")
                        println("Hey Token----${authPrefs.getAccessToken()}")
                        println("Hey Driver----${authPrefs.getDriverId()}")

                        _state.value = AuthState.Success(result)
                    } else {
                        _state.value = AuthState.Error("Empty response")
                    }
                } else {
                    if (response.code() == 401) {
                        _state.value = AuthState.InvalidCredentials
                    } else {
                        _state.value = AuthState.Error("Login failed: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun clearError() {
        _state.value = AuthState.Idle
    }
}
