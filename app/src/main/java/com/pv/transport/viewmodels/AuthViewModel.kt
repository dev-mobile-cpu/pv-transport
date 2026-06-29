package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.ErrorResponse
import com.pv.transport.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.pv.transport.data.log.LoginResponse
import com.pv.transport.network.ErrorHandler

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: LoginResponse) : AuthState()
    data class InvalidCredentials(val errorMessage: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val authPrefs: AuthPrefs
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(username: String, password: String) {
        _state.value = AuthState.Loading

        viewModelScope.launch {
            try {

                val response = repo.login(username, password)

                if (response.isSuccessful) {

                    val result = response.body()

                    if (result != null) {
                        authPrefs.saveToken(AuthPrefs.KEYS.ACCESS_TOKEN, result.token)
                        authPrefs.saveLogin(true)
                        authPrefs.saveDriver(result.driver)
                        authPrefs.saveUserName(username)
                        authPrefs.savePassword(password)

                        _state.value = AuthState.Success(result)
                    } else {
                        _state.value = AuthState.Error("Empty response")
                    }

                } else {

                    val errorJson = response.errorBody()?.string()

                    val msg = try {
                        val obj = Gson().fromJson(errorJson, ErrorResponse::class.java)
                        obj.error ?: "Invalid username or password"
                    } catch (e: Exception) {
                        "Invalid username or password"
                    }

                    _state.value = AuthState.Error(msg)
                }

            }catch (e: Exception) {
                _state.value = AuthState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

}
