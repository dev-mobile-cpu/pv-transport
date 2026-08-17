package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.CheckVersionResponse
import com.pv.transport.data.log.DriverLogResponse
import com.pv.transport.network.ErrorHandler
import com.pv.transport.repository.CheckVersionRepository
import com.pv.transport.viewmodels.ApproveDriverLogViewModel.CorporateUsersState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckVersionViewModel @Inject constructor(
    private val repository: CheckVersionRepository
): ViewModel() {
    sealed class CheckVersionState {
        object Idle : CheckVersionState()
        object Loading : CheckVersionState()
        data class Success(val message: CheckVersionResponse) : CheckVersionState()
        data class Error(val message: String) : CheckVersionState()
    }

    private val _version = MutableStateFlow<CheckVersionState>(CheckVersionState.Loading)
    val version: StateFlow<CheckVersionState> = _version

    fun getCheckVersion() {
        viewModelScope.launch {
            try {
                _version.value = CheckVersionState.Loading
                val result = repository.checkVersion()
                if (result.isSuccessful) {
                    result.body()?.let {
                        _version.value = CheckVersionState.Success(it)
                    } ?: run {
                        _version.value = CheckVersionState.Error("Empty response")
                    }
                } else {
                    _version.value = CheckVersionState.Error(ErrorHandler.fromResponse(result))
                }
            } catch (e: Exception) {
                _version.value = CheckVersionState.Error(ErrorHandler.getMessage(e))
            }
        }

    }

}