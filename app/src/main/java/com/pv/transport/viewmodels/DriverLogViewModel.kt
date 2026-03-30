package com.pv.transport.viewmodels

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.AllDriverLogResponse
import com.pv.transport.data.ApprovalResponse
import com.pv.transport.data.CorporateUsersResponse
import com.pv.transport.data.Data
import com.pv.transport.repository.AuthRepository
import com.pv.transport.data.DriverLogResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@SuppressLint("NewApi")
@HiltViewModel
class DriverLogViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {


    sealed class DriverLogState {
        object Idle : DriverLogState()
        object Loading : DriverLogState()
        data class Success(val message: DriverLogResponse) : DriverLogState()
        data class Error(val message: String) : DriverLogState()
    }

    sealed class DriverLogListState {
        object Idle : DriverLogListState()
        object Loading : DriverLogListState()
        data class Success(val logs: List<Data>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false) : DriverLogListState()
        data class Error(val message: String) : DriverLogListState()
    }

    sealed class ApprovalState {
        object Idle : ApprovalState()
        object Loading : ApprovalState()
        data class Success(val response: AllDriverLogResponse) : ApprovalState()
        data class Error(val message: String) : ApprovalState()
    }

    sealed class CorporateUsersState {
        object Loading : CorporateUsersState()
        data class Success(val response: List<CorporateUsersResponse>) : CorporateUsersState()
        data class Error(val message: String) : CorporateUsersState()
    }

    private val _state = MutableStateFlow<DriverLogState>(DriverLogState.Idle)
    val state: StateFlow<DriverLogState> = _state

    private val _driverLogList = MutableStateFlow<DriverLogListState>(DriverLogListState.Loading)
    val driverLogList: StateFlow<DriverLogListState> = _driverLogList


    private val _approval = MutableStateFlow<ApprovalState>(ApprovalState.Loading)
    val approval: StateFlow<ApprovalState> = _approval

    private val _corporateUsers = MutableStateFlow<CorporateUsersState>(CorporateUsersState.Loading)
    val corporateUsers: StateFlow<CorporateUsersState> = _corporateUsers

    private var currentPage = 1
    private var allLogs = mutableListOf<Data>()

    fun checkInDriverLog(
        date: String,
        type: String,
        reason: String,
        remark: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri) {

        viewModelScope.launch {

            try {
                _state.value = DriverLogState.Loading
                val result = repository.checkInDriverLog(
                    date,type, reason,
                    remark, startTime,
                    startKm,  startPhoto
                )

                println("$date $reason $remark $startTime  $startKm")


                println("Hey Diver Log Data-----$result")
                if (result.isSuccessful) {
                    val body = result.body()
                    _state.value = DriverLogState.Success(body!!)
                    println("Driver log saved successfully: $body")
                } else {
                    _state.value = DriverLogState.Error("Failed: ${result.code()}")
                    println("Failed to save driver log: ${result.code()} - ${result.message()}")
                }

            }catch (e: Exception){
                e.printStackTrace()
                _state.value = DriverLogState.Error(e.localizedMessage ?: "Unknown error")
            }


        }

    }

    fun checkInTripDriverLog(
        date: String,
        type: String,
        tripTypeId: String,
        from: String,
        to: String,
        purpose: String,
        reason: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri) {

        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                val result = repository.checkInTripDriverLog(
                    date,type, tripTypeId, from, to, purpose, reason, startTime, startKm, startPhoto
                )

                println("$date $tripTypeId $from $to $purpose $reason $startTime  $startKm")

                println("Hey Diver Log Data-----$result")
                if (result.isSuccessful) {
                    val body = result.body()
                    _state.value = DriverLogState.Success(body!!)
                    println("Trip driver log saved successfully: $body")
                } else {
                    _state.value = DriverLogState.Error("Failed: ${result.code()}")
                    println("Failed to save trip driver log: ${result.code()} - ${result.message()}")
                }

            }catch (e: Exception){
                e.printStackTrace()
                _state.value = DriverLogState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun checkOutDriverLog(
        recordId: String,
        endTime: String,
        endKm: String,
        endPhoto: Uri) {

        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                val result = repository.checkOutDriverLog(
                    recordId, endTime, endKm, endPhoto
                )

                println("$recordId $endTime  $endKm")

                println("Hey Diver Log Data-----$result")
                if (result.isSuccessful) {
                    val body = result.body()
                    _state.value = DriverLogState.Success(body!!)
                    println("Driver log checked out successfully: $body")
                } else {
                    _state.value = DriverLogState.Error("Failed: ${result.code()}")
                    println("Failed to check out driver log: ${result.code()} - ${result.message()}")
                }

            }catch (e: Exception){
                e.printStackTrace()
                _state.value = DriverLogState.Error(e.localizedMessage ?: "Unknown error")
            }
        }

    }

    fun getDriverLogs(start: String, end: String) {

        viewModelScope.launch {
            try {
                _driverLogList.value = DriverLogListState.Loading
                currentPage = 1
                allLogs.clear()
                val result = repository.getDriverLogs(start, end, null)
                println("Hey Driver Log List Data-----$result")
                if (result.isSuccessful) {
                    val body = result.body()!!
                    allLogs.addAll(body.data)
                    _driverLogList.value = DriverLogListState.Success(allLogs.toList(), currentPage, body.meta.lastPage.toInt())
                    println("Driver logs retrieved successfully: $body")
                } else {
                    _driverLogList.value = DriverLogListState.Error("Failed: ${result.code()}")
                    println("Failed to retrieve driver logs: ${result.code()} - ${result.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _driverLogList.value = DriverLogListState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadMoreLogs(start: String, end: String) {
        val currentState = _driverLogList.value
        if (currentState is DriverLogListState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _driverLogList.value = currentState.copy(isLoadingMore = true)
                    currentPage++
                    val result = repository.getDriverLogs(start, end, currentPage)
                    if (result.isSuccessful) {
                        val body = result.body()!!
                        allLogs.addAll(body.data)
                        _driverLogList.value = DriverLogListState.Success(allLogs.toList(), currentPage, body.meta.lastPage.toInt())
                    } else {
                        _driverLogList.value = currentState.copy(isLoadingMore = false)
                        // Optionally handle error
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _driverLogList.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun getApprovalStatus(startDate: String, endDate: String,status: String) {
        viewModelScope.launch {
            try {

                _approval.value = ApprovalState.Loading
                val result = repository.getApprovalStatus(startDate,endDate,status)
                println("Hey Approval Data-----$result")
                if (result.isSuccessful) {
                    val body = result.body()
                    _approval.value = ApprovalState.Success(body!!)
                    println("Approval retrieved successfully: $body")
                } else {
                    _approval.value = ApprovalState.Error("Failed: ${result.code()}")
                    println("Failed to retrieve approval : ${result.code()} - ${result.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _approval.value = ApprovalState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    fun getCorporateUsers() {
        viewModelScope.launch {
            try {
                _corporateUsers.value = CorporateUsersState.Loading
                val result = repository.getCorporateUsers()
                println("Hey Corporate Users Data-----$result")
                if (result.isSuccessful) {
                    val body = result.body()
                    _corporateUsers.value = CorporateUsersState.Success(body!!)
                    println("Corporate users retrieved successfully: $body")
                } else {
                    _corporateUsers.value = CorporateUsersState.Error("Failed: ${result.code()}")
                    println("Failed to retrieve corporate users: ${result.code()} - ${result.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _corporateUsers.value = CorporateUsersState.Error(e.localizedMessage ?: "Unknown error")
                println("Error retrieving corporate users: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

}
