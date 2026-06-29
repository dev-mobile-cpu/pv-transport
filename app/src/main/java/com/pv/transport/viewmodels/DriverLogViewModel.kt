package com.pv.transport.viewmodels

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.log.Data
import com.pv.transport.repository.AuthRepository
import com.pv.transport.data.log.DriverLogResponse
import com.pv.transport.network.ErrorHandler
import com.pv.transport.network.NoInternetException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
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


    private val _state = MutableStateFlow<DriverLogState>(DriverLogState.Idle)
    val state: StateFlow<DriverLogState> = _state

    private val _driverLogList = MutableStateFlow<DriverLogListState>(DriverLogListState.Loading)
    val driverLogList: StateFlow<DriverLogListState> = _driverLogList


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
                _state.value = DriverLogState.Error(ErrorHandler.getMessage(e))
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
                _state.value = DriverLogState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun checkOutDriverLog(
        recordId: String,
        remark: String,
        endTime: String,
        endKm: String,
        endPhoto: Uri) {

        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                val result = repository.checkOutDriverLog(
                    recordId,remark, endTime, endKm, endPhoto
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
                _state.value = DriverLogState.Error(ErrorHandler.getMessage(e))
            }
        }

    }

    fun getDriverLogs(start: String, end: String) {

        viewModelScope.launch {
            try {
                _driverLogList.value = DriverLogListState.Loading
                currentPage = 1
                allLogs.clear()
                val result = repository.getDriverLogs(start, end)
                println("Hey Driver Log List Data-----$result")
                if (result.isSuccessful) {
                    val body = result.body()!!
                    allLogs.addAll(body.data)
                    _driverLogList.value = DriverLogListState.Success(allLogs.toList(), currentPage, body.meta.lastPage.toInt())
                    println("Driver logs retrieved successfully: ${_driverLogList.value.toString()}")
                } else {
                    _driverLogList.value = DriverLogListState.Error("Failed: ${result.code()}")
                    println("Failed to retrieve driver logs: ${result.code()} - ${result.message()}")
                }
            }catch (e: NoInternetException){
                _driverLogList.value = DriverLogListState.Error(e.message.toString())
            }
            catch (e: Exception) {
                e.printStackTrace()
                _driverLogList.value = DriverLogListState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun loadMoreLogs(start: String, end: String) {
        Log.e("DriverLogViewModel", "Attempting to load more logs. Current Page: $_driverLogList.value")
        val currentState = _driverLogList.value
        Log.e("DriverLogViewModel", "Current State: $currentState, Current Page: $currentPage")
        if (currentState is DriverLogListState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _driverLogList.value = currentState.copy(isLoadingMore = true)
                    currentPage++
                    Log.e("DriverLogViewModel", "Loading more logs for page: $currentPage")
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

}