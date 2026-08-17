package com.pv.transport.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.log.ApproveDriverLogResponse
import com.pv.transport.data.log.CorporateUsersResponse
import com.pv.transport.data.log.Data
import com.pv.transport.network.ErrorHandler
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ApproveDriverLogViewModel @Inject constructor(
    private val repo: AuthRepository
): ViewModel() {

    sealed class ApprovalState {
        object Idle : ApprovalState()
        object Loading : ApprovalState()
        data class Success(val response: List<Data>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false) : ApprovalState()
        data class Error(val message: String) : ApprovalState()
    }

    sealed class CorporateUsersState {
        object Loading : CorporateUsersState()
        data class Success(val response: List<CorporateUsersResponse>) : CorporateUsersState()
        data class Error(val message: String) : CorporateUsersState()
    }
    sealed class ApproveDriverLogState {
        object Idle : ApproveDriverLogState()
        object Loading : ApproveDriverLogState()
        data class Success(val response: ApproveDriverLogResponse) : ApproveDriverLogState()
        data class Error(val message: String) : ApproveDriverLogState()
    }

    private val _state = MutableStateFlow<ApproveDriverLogState>(ApproveDriverLogState.Idle)
    val state: StateFlow<ApproveDriverLogState> = _state

    private val _approval = MutableStateFlow<ApprovalState>(ApprovalState.Loading)
    val approval: StateFlow<ApprovalState> = _approval

    private val _corporateUsers = MutableStateFlow<CorporateUsersState>(CorporateUsersState.Loading)
    val corporateUsers: StateFlow<CorporateUsersState> = _corporateUsers

    private var currentPage = 1
    private var allApproval = mutableListOf<Data>()

    fun getApprovalStatus(startDate: String, endDate: String,status: String) {
        viewModelScope.launch {
            try {
                _approval.value = ApprovalState.Loading
                currentPage = 1
                allApproval.clear()
                val result = repo.getApprovalStatus(startDate,endDate,status)
                if (result.isSuccessful) {
                    val body = result.body()!!
                    allApproval.addAll(body.data)
                    _approval.value = ApprovalState.Success(allApproval.toList(),currentPage,body.meta.lastPage.toInt())
                } else {
                    _approval.value = ApprovalState.Error(ErrorHandler.fromResponse(result))
                }
            } catch (e: Exception) {
                _approval.value = ApprovalState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun loadMoreLogs(start: String, end: String,status: String) {
        val currentState = _approval.value
        if (currentState is ApprovalState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _approval.value = currentState.copy(isLoadingMore = true)
                    currentPage++
                    val result = repo.getApprovalStatus(start, end,status, currentPage)
                    if (result.isSuccessful) {
                        val body = result.body()!!
                        allApproval.addAll(body.data)
                        _approval.value = ApprovalState.Success(allApproval.toList(), currentPage, body.meta.lastPage.toInt())
                    } else {
                        _approval.value = currentState.copy(isLoadingMore = false)
                    }
                } catch (e: Exception) {
                    _approval.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun getCorporateUsers() {
        viewModelScope.launch {
            try {
                _corporateUsers.value = CorporateUsersState.Loading
                val result = repo.getCorporateUsers()
                if (result.isSuccessful) {
                    val body = result.body()
                    _corporateUsers.value = CorporateUsersState.Success(body!!)

                } else {
                    _corporateUsers.value = CorporateUsersState.Error(ErrorHandler.fromResponse(result))
                }
            } catch (e: Exception) {
                _corporateUsers.value = CorporateUsersState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun approveDriverLog(token: String, password: String, signature: File) {
        viewModelScope.launch {
            try {
                _state.value = ApproveDriverLogState.Loading
                val response = repo.approveDriverLog(token, password, signature)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _state.value = ApproveDriverLogState.Success(responseBody)

                    } else {
                        _state.value = ApproveDriverLogState.Error("Empty response body")
                    }
                } else {
                    _state.value = ApproveDriverLogState.Error(ErrorHandler.fromResponse(response))
                }
            }catch (e: Exception){
                _state.value = ApproveDriverLogState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun resetState() {
        _state.value = ApproveDriverLogState.Idle
    }
}
