package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.log.TripTypeResponse
import com.pv.transport.network.ErrorHandler
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripTypeViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val tripType: TripTypeResponse) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun getTripType() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                // AuthRepository.getTripTypes() now handles online/offline caching transparently
                val response = repository.getTripTypes()
                if (response.isSuccessful) {
                    _state.value = UiState.Success(response.body() ?: TripTypeResponse(emptyList()))
                } else {
                    _state.value = UiState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(ErrorHandler.getMessage(e))
            }
        }
    }
}
