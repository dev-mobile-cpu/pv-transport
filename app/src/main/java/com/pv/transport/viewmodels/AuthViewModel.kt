package com.pv.transport.viewmodels

import android.os.Message
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.pv.transport.data.log.LoginResponse
import com.pv.transport.network.NetworkException
import java.net.ConnectException
import java.net.UnknownHostException

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
                    handleErrorResponse(response.code(), response.errorBody()?.string())
                }
            } catch (e: Exception) {
                e.printStackTrace() // Logcat တွင် ဆက်လက်စစ်ဆေးနိုင်ရန်

                // Interceptor က ပစ်လိုက်တဲ့ NetworkException ဖြစ်နေရင် ၎င်းထဲက errorBody ကို Parse လုပ်ပါမယ်
                if (e is NetworkException) {
                    // NetworkException ထဲတွင် သတ်မှတ်ထားသော errorCode (သို့) errorBody အား ဆွဲထုတ်ခြင်း
                    val errorCode = e.errorCode
                    val errorBodyString = e.errorBody?.string()

                    if (errorCode == 401 || errorCode == 422) {
                        parseAndSetInvalidCredentials(errorBodyString)
                    } else {
                        _state.value = AuthState.Error("Login failed: $errorCode")
                    }
                } else if (e is UnknownHostException || e is ConnectException) {
                    _state.value = AuthState.Error("No internet connection. Please check your network.")
                } else {
                    _state.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")
                }
            }


        }
    }


    private fun parseAndSetInvalidCredentials(errorJsonString: String?) {
        val displayMessage = try {
            if (!errorJsonString.isNullOrEmpty()) {
                val jsonObject = JsonParser.parseString(errorJsonString).asJsonObject
                val serverMsg = jsonObject.get("error").asString  // "credentials are wrong."

                if (serverMsg.trim().equals("credentials are wrong.", ignoreCase = true)) {
                    "Username or password is wrong"
                } else {
                    serverMsg
                }
            } else {
                "Username or password is wrong"
            }
        } catch (ex: Exception) {
            "Username or password is wrong"
        }
        _state.value = AuthState.InvalidCredentials(displayMessage)
    }

    private fun handleErrorResponse(statusCode: Int, errorBodyString: String?) {
        if (statusCode == 401 || statusCode == 422) {
            parseAndSetInvalidCredentials(errorBodyString)
        } else {
            _state.value = AuthState.Error("Login failed: $statusCode")
        }
    }
    fun clearError() {
        _state.value = AuthState.Idle
    }
}
