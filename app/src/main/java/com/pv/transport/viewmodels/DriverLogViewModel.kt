package com.pv.transport.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.log.Data
import com.pv.transport.local.data.OfflineCheckInEntity
import com.pv.transport.local.data.OfflineCheckOutEntity
import com.pv.transport.network.NetworkUtils
import com.pv.transport.repository.AuthRepository
import com.pv.transport.data.log.DriverLogResponse
import com.pv.transport.network.ErrorHandler
import com.pv.transport.network.NoInternetException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.text.input.TextFieldValue

@SuppressLint("NewApi")
@HiltViewModel
class DriverLogViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    sealed class DriverLogState {
        object Idle : DriverLogState()
        object Loading : DriverLogState()
        object SavedOffline : DriverLogState()
        data class Success(val message: DriverLogResponse) : DriverLogState()
        data class Error(val message: String) : DriverLogState()
    }

    sealed class DriverLogListState {
        object Idle : DriverLogListState()
        object Loading : DriverLogListState()
        data class Success(val logs: List<Data>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false, val isOffline: Boolean = false) : DriverLogListState()
        data class Error(val message: String) : DriverLogListState()
    }

    private val _state = MutableStateFlow<DriverLogState>(DriverLogState.Idle)
    val state: StateFlow<DriverLogState> = _state

    private val _driverLogList = MutableStateFlow<DriverLogListState>(DriverLogListState.Loading)
    val driverLogList: StateFlow<DriverLogListState> = _driverLogList

    val pendingCheckIns: StateFlow<List<OfflineCheckInEntity>> =
        repository.observePendingCheckIns()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingCheckOuts: StateFlow<List<OfflineCheckOutEntity>> =
        repository.observePendingCheckOuts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var currentPage = 1
    private var allLogs = mutableListOf<Data>()

    // --- Persistent Form States (Option 1) ---
    
    // Daily Check-In
    val dailyStartKm = MutableStateFlow("")
    val dailyRemark = MutableStateFlow("")
    val dailyStartUri = MutableStateFlow<Uri?>(null)
    val dailySelectedReason = MutableStateFlow("")
    val dailySelectedIndex = MutableStateFlow(0)

    // Trip Check-In
    val tripSelectedTrip = MutableStateFlow("")
    val tripTypeIndex = MutableStateFlow(0)
    val tripFrom = MutableStateFlow("")
    val tripTo = MutableStateFlow("")
    val tripStartKm = MutableStateFlow("")
    val tripPurpose = MutableStateFlow("")
    val tripStartUri = MutableStateFlow<Uri?>(null)
    val tripSelectedReason = MutableStateFlow("")
    val tripSelectedIndex = MutableStateFlow(0)

    // Check-Out
    val checkOutEndKm = MutableStateFlow("")
    val checkOutEndUri = MutableStateFlow<Uri?>(null)
    val checkOutRemark = MutableStateFlow(TextFieldValue(""))
    val checkOutPurpose = MutableStateFlow(TextFieldValue(""))

    fun clearDailyCheckIn() {
        dailyStartKm.value = ""
        dailyRemark.value = ""
        dailyStartUri.value = null
        dailySelectedReason.value = ""
        dailySelectedIndex.value = 0
    }

    fun clearTripCheckIn() {
        tripSelectedTrip.value = ""
        tripTypeIndex.value = 0
        tripFrom.value = ""
        tripTo.value = ""
        tripStartKm.value = ""
        tripPurpose.value = ""
        tripStartUri.value = null
        tripSelectedReason.value = ""
        tripSelectedIndex.value = 0
    }

    fun clearCheckOut() {
        checkOutEndKm.value = ""
        checkOutEndUri.value = null
        checkOutRemark.value = TextFieldValue("")
        checkOutPurpose.value = TextFieldValue("")
    }

    fun resetState() {
        _state.value = DriverLogState.Idle
    }

    init {
        // Observe cache and state to show cached data when offline or loading
        viewModelScope.launch {
            combine(
                repository.observeCachedDriverLogs(),
                _driverLogList
            ) { cache, currentState ->
                if (cache != null && (currentState is DriverLogListState.Loading || currentState is DriverLogListState.Error)) {
                    allLogs.clear()
                    allLogs.addAll(cache.logs)
                    _driverLogList.value = DriverLogListState.Success(
                        logs = allLogs.toList(),
                        currentPage = 1,
                        lastPage = 1,
                        isOffline = true
                    )
                }
            }.collect {}
        }
    }

    fun checkInDriverLog(
        date: String,
        type: String,
        reason: String,
        remark: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri,
        context: android.content.Context
    ) {
        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                if (!NetworkUtils.isInternetAvailable(context)) {
                    repository.checkInDriverLogOffline(date, type, reason, remark, startTime, startKm, startPhoto)
                    _state.value = DriverLogState.SavedOffline
                    clearDailyCheckIn()
                    return@launch
                }
                val result = repository.checkInDriverLog(date, type, reason, remark, startTime, startKm, startPhoto)
                if (result.isSuccessful) {
                    _state.value = DriverLogState.Success(result.body()!!)
                    clearDailyCheckIn()
                } else {
                    _state.value = DriverLogState.Error("Failed: ${result.code()}")
                }
            } catch (e: Exception) {
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
        startPhoto: Uri,
        context: android.content.Context
    ) {
        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                if (!NetworkUtils.isInternetAvailable(context)) {
                    repository.checkInTripDriverLogOffline(date, type, tripTypeId, from, to, purpose, reason, startTime, startKm, startPhoto)
                    _state.value = DriverLogState.SavedOffline
                    clearTripCheckIn()
                    return@launch
                }
                val result = repository.checkInTripDriverLog(date, type, tripTypeId, from, to, purpose, reason, startTime, startKm, startPhoto)
                if (result.isSuccessful) {
                    _state.value = DriverLogState.Success(result.body()!!)
                    clearTripCheckIn()
                } else {
                    _state.value = DriverLogState.Error("Failed: ${result.code()}")
                }
            } catch (e: Exception) {
                _state.value = DriverLogState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun checkOutDriverLog(
        recordId: String,
        remark: String,
        endTime: String,
        endKm: String,
        endPhoto: Uri,
        context: android.content.Context,
        localCheckInUuid: String? = null
    ) {
        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                if (!NetworkUtils.isInternetAvailable(context)) {
                    // If recordId is empty, it means the check-in was also offline
                    val serverRecordId = if (recordId.isNotEmpty()) recordId else null
                    repository.checkOutDriverLogOffline(serverRecordId, localCheckInUuid, remark, endTime, endKm, endPhoto)
                    _state.value = DriverLogState.SavedOffline
                    clearCheckOut()
                    return@launch
                }
                val result = repository.checkOutDriverLog(recordId, remark, endTime, endKm, endPhoto)
                if (result.isSuccessful) {
                    _state.value = DriverLogState.Success(result.body()!!)
                    clearCheckOut()
                } else {
                    _state.value = DriverLogState.Error("Failed: ${result.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = DriverLogState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getDriverLogs(start: String, end: String) {
        viewModelScope.launch {
            try {
                _driverLogList.value = DriverLogListState.Loading
                val result = repository.getDriverLogs(start, end)
                if (result.isSuccessful) {
                    val body = result.body()!!
                    currentPage = 1
                    allLogs.clear()
                    allLogs.addAll(body.data)
                    _driverLogList.value = DriverLogListState.Success(allLogs.toList(), currentPage, body.meta.lastPage.toInt())
                } else {
                    _driverLogList.value = DriverLogListState.Error("Failed: ${result.code()}")
                }
            } catch (e: NoInternetException) {
                _driverLogList.value = DriverLogListState.Error(e.message.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                _driverLogList.value = DriverLogListState.Error(ErrorHandler.getMessage(e))
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
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _driverLogList.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }
}
