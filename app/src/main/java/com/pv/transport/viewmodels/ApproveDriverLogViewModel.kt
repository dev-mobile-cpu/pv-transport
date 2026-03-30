package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.AllDriverLogResponse
import com.pv.transport.data.ApproveDriverLogRequest
import com.pv.transport.data.ApproveDriverLogResponse
import com.pv.transport.repository.AuthRepository
import com.pv.transport.viewmodels.DriverLogViewModel.DriverLogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApproveDriverLogViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val authPrefs: AuthPrefs
): ViewModel() {
    sealed class ApproveDriverLogState {
        object Idle : ApproveDriverLogState()
        object Loading : ApproveDriverLogState()
        data class Success(val response: ApproveDriverLogResponse) : ApproveDriverLogState()
        data class Error(val message: String) : ApproveDriverLogState()
    }

    private val _state = MutableStateFlow<ApproveDriverLogState>(ApproveDriverLogState.Idle)
    val state: StateFlow<ApproveDriverLogState> = _state

    fun approveDriverLog(token: String, password: ApproveDriverLogRequest) {
        viewModelScope.launch {
            _state.value = ApproveDriverLogState.Loading
            var currentToken = token
            var response = repo.approveDriverLog(currentToken, password)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    _state.value = ApproveDriverLogState.Success(responseBody)
                    println("ApproveDriverLogViewModel: Approval successful - ${responseBody.message}")
                } else {
                    _state.value = ApproveDriverLogState.Error("Empty response body")
                }
            } else if (response.code() == 401) {
                val refreshResponse = repo.refreshToken(currentToken)
                if (refreshResponse.isSuccessful) {
                    val newToken = refreshResponse.body()?.token
                    if (!newToken.isNullOrEmpty()) {
                        authPrefs.saveAccessToken(newToken)
                        currentToken = newToken
                        response = repo.approveDriverLog(currentToken, password)
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                _state.value = ApproveDriverLogState.Success(responseBody)
                                println("ApproveDriverLogViewModel: Approval successful after refresh - ${responseBody.message}")
                            } else {
                                _state.value = ApproveDriverLogState.Error("Empty response body after refresh")
                            }
                        } else {
                            _state.value = ApproveDriverLogState.Error("Error after refresh: ${response.code()} ${response.message()}")
                        }
                    } else {
                        _state.value = ApproveDriverLogState.Error("Refresh failed: invalid new token")
                    }
                } else {
                    _state.value = ApproveDriverLogState.Error("Refresh failed: ${refreshResponse.code()} ${refreshResponse.message()}")
                }
            } else {
                _state.value = ApproveDriverLogState.Error("Error: ${response.code()} ${response.message()}")
            }
        }
    }

}