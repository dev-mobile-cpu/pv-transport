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
import com.pv.transport.repository.FuelRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope

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
    private val fuelRepo: FuelRepository,
    private val authPrefs: AuthPrefs
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(
        username: String,
        password: String
    ) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = repo.login(username, password)
                if(response.isSuccessful){
                    val result = response.body()
                    if(result != null){
                        authPrefs.saveToken(
                            AuthPrefs.KEYS.ACCESS_TOKEN,
                            result.token
                        )

                        supervisorScope {
                            val tripTypes = async { repo.getTripTypes() }
                            val reasons = async { repo.getReason() }
                            val fuelTypes = async { fuelRepo.getFuelTypes() }
                            val fuelCompanies = async { fuelRepo.getFuelCompanies() }
                            val costTypes = async { repo.getCostTypes() }
                            val corporateUsers = async { repo.getCorporateUsers() }

                            val results = awaitAll(
                                tripTypes,
                                reasons,
                                fuelTypes,
                                fuelCompanies,
                                costTypes,
                                corporateUsers
                            )

                            // Check all API result
                            val failed =
                                results.any {
                                    !it.isSuccessful
                                }

                            if(failed){
                                throw Exception(
                                    "Download master data failed"
                                )
                            }

                        }

                        // Only after download success
                        authPrefs.saveLogin(true)
                        authPrefs.saveDriver(result.driver)
                        authPrefs.saveUserName(username)
                        authPrefs.savePassword(password)

                        _state.value = AuthState.Success(result)

                    } else {
                        _state.value = AuthState.Error("Empty response")
                    }

                }else{
                    _state.value = AuthState.Error("Invalid username or password")
                }

            }catch(e:Exception){
                _state.value = AuthState.Error(ErrorHandler.getMessage(e))
            }

        }
    }

}
