package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.log.GenerateQR
import com.pv.transport.data.log.GenerateQRUiState
import com.pv.transport.network.ErrorHandler
import com.pv.transport.network.WebSocketManager
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenerateQRViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val socketManager: WebSocketManager
): ViewModel(){

    private val _uiState = MutableStateFlow<GenerateQRUiState>(GenerateQRUiState.Idle)
    val uiState: StateFlow<GenerateQRUiState> = _uiState

    val socketState = socketManager.socketState

    init {
        socketManager.connect()
    }


    fun generateQR(request: GenerateQR) {

        viewModelScope.launch {
            _uiState.value = GenerateQRUiState.Loading

            try {

                val response = repo.getGenerateQR(request)

                if (response.isSuccessful) {

                    response.body()?.let {

                        _uiState.value = GenerateQRUiState.Success(it)

                    } ?: run {
                        _uiState.value = GenerateQRUiState.Error("Empty response")
                    }

                } else {

                    _uiState.value = GenerateQRUiState.Error("API Error ${response.code()}")

                }

            } catch (e: Exception) {
                _uiState.value = GenerateQRUiState.Error(ErrorHandler.getMessage(e))

            }
        }
    }

    fun resetState() {
        _uiState.value = GenerateQRUiState.Idle
    }

    override fun onCleared() {
        socketManager.disconnect()
        super.onCleared()
    }

}