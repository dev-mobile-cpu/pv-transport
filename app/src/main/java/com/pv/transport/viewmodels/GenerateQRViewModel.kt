package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.log.GenerateQR
import com.pv.transport.data.log.GenerateQRUiState
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenerateQRViewModel @Inject constructor(private val repo: AuthRepository): ViewModel(){

    private val _uiState = MutableStateFlow<GenerateQRUiState>(GenerateQRUiState.Idle)
    val uiState: StateFlow<GenerateQRUiState> = _uiState

    fun generateQR(request: GenerateQR) {

        viewModelScope.launch {

            _uiState.value = GenerateQRUiState.Loading

            try {

                val response = repo.getGenerateQR(request)

                if (response.isSuccessful) {

                    response.body()?.let {

                        _uiState.value =

                            GenerateQRUiState.Success(it)

                    } ?: run {
                        _uiState.value =
                            GenerateQRUiState.Error("Empty response")
                    }

                } else {

                    _uiState.value =
                        GenerateQRUiState.Error("API Error ${response.code()}")

                }

            } catch (e: Exception) {

                _uiState.value =
                    GenerateQRUiState.Error(e.message ?: "Unknown error")

            }
        }
    }

    fun resetState() {
        _uiState.value = GenerateQRUiState.Idle
    }

}