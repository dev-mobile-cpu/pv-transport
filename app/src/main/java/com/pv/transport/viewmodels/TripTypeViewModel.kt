package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.repository.AuthRepository
import com.pv.transport.data.ReasonResponse
import com.pv.transport.data.TripTypeResponse
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
                val response = repository.getTripTypes()
                if (response.isSuccessful) {
                    val body = response.body() ?: TripTypeResponse(emptyList())
                    _state.value = UiState.Success(body)
                } else {
                    _state.value = UiState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

}