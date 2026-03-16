package com.pv.transport.data

sealed class GenerateQRUiState{
    object Idle : GenerateQRUiState()
    object Loading : GenerateQRUiState()
    data class Success(val qrResponse: GenerateQRResponse) : GenerateQRUiState()
    data class Error(val message: String) : GenerateQRUiState()
}
