package com.pv.transport.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.log.Data
import com.pv.transport.data.log.DriverLogData
import com.pv.transport.data.log.DriverLogResponse
import com.pv.transport.data.log.LogSheetResponse
import com.pv.transport.local.data.OfflineCheckInEntity
import com.pv.transport.local.data.OfflineCheckOutEntity
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.network.ErrorHandler
import com.pv.transport.network.NetworkUtils
import com.pv.transport.network.NoInternetException
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("NewApi")
@HiltViewModel
class DriverLogViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    sealed class LogSheetState{
        object Idle : LogSheetState()
        object Loading : LogSheetState()
        data class Success(val message: LogSheetResponse) : LogSheetState()
        data class Error(val message: String) : LogSheetState()
    }

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
        data class Success(
            val logs: List<Data>,
            val currentPage: Int,
            val lastPage: Int,
            val isLoadingMore: Boolean = false,
            val isOffline: Boolean = false
        ) : DriverLogListState()
        data class Error(val message: String) : DriverLogListState()
    }

    private val _state = MutableStateFlow<DriverLogState>(DriverLogState.Idle)
    val state: StateFlow<DriverLogState> = _state

    private val _driverLogList = MutableStateFlow<DriverLogListState>(DriverLogListState.Loading)

    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            connectivityObserver.getCurrentStatus()
        )


    private val pendingCheckIns = repository.observePendingCheckIns()
    private val pendingCheckOuts = repository.observePendingCheckOuts()

    // Unified logs list
    val driverLogList: StateFlow<DriverLogListState> = combine(
        _driverLogList,
        pendingCheckIns,
        pendingCheckOuts,
        repository.observeCachedDriverLogs()
    ) { currentState, pendingIn, pendingOut, cache ->
        if (currentState is DriverLogListState.Loading || currentState is DriverLogListState.Error) {
            if (cache != null) {
                println("Hey cache logs---- ${cache.logs}")
                val merged = mergeLogs(cache.logs, pendingIn, pendingOut)
                DriverLogListState.Success(merged, 1, 1, isOffline = true)
            } else {
                currentState
            }
        } else if (currentState is DriverLogListState.Success) {
            val merged = mergeLogs(allLogs, pendingIn, pendingOut)
            currentState.copy(logs = merged)
        } else {
            currentState
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DriverLogListState.Loading)

    private var currentPage = 1
    private var allLogs = mutableListOf<Data>()
    // In-memory cache of mappings so offline entries can show readable labels
    private val tripTypeMap = mutableMapOf<String, String>()
    private val reasonMap = mutableMapOf<String, String>()

    // Daily Check-In states
    val dailyStartKm = MutableStateFlow("")
    val dailyRemark = MutableStateFlow("")
    val dailyPurpose = MutableStateFlow("")
    val dailySite = MutableStateFlow("")
    val dailyStartUri = MutableStateFlow<Uri?>(null)
    val dailySelectedReason = MutableStateFlow("")
    val dailySelectedIndex = MutableStateFlow(0)

    // Trip Check-In states
    val tripSelectedTrip = MutableStateFlow("")
    val tripTypeIndex = MutableStateFlow(0)
    val tripFrom = MutableStateFlow("")
    val tripTo = MutableStateFlow("")
    val tripStartKm = MutableStateFlow("")
    val tripPurpose = MutableStateFlow("")
    val tripStartUri = MutableStateFlow<Uri?>(null)
    val tripSelectedReason = MutableStateFlow("")
    val tripSelectedIndex = MutableStateFlow(0)

    // Check-Out states
    val checkOutEndKm = MutableStateFlow(TextFieldValue(""))
    val checkOutEndUri = MutableStateFlow<Uri?>(null)
    val checkOutRemark = MutableStateFlow(TextFieldValue(""))
    val checkOutPurpose = MutableStateFlow(TextFieldValue(""))

    var currentCheckOutId: String? = null
        private set

    fun setCurrentCheckOutId(id: String) {
        currentCheckOutId = id
    }

    private fun mergeLogs(serverLogs: List<Data>, pendingIn: List<OfflineCheckInEntity>, pendingOut: List<OfflineCheckOutEntity>): List<Data> {
        val pendingInByUuid = pendingIn.associateBy { it.uuid }
        val pendingOutByUuid = pendingOut.associateBy { it.uuid }
        val pendingOutByLocalCheckIn = pendingOut.associateBy { it.localCheckInUuid }

        val offlineLogs = pendingIn.map { entity ->
            val checkout = pendingOut.find { it.localCheckInUuid == entity.uuid }
            val isSyncing = entity.isSyncing || (checkout?.isSyncing ?: false)
            
            Data(
                id = entity.uuid,
                startTime = entity.startTime,
                endTime = checkout?.endTime,
                reason = reasonMap[entity.reason] ?: entity.reason,
                remark = entity.remark,
                startKm = entity.startKm,
                endKm = checkout?.endKm,
                driverLogId = "",
                type = entity.type,
                tripTypeId = entity.tripTypeId,
                from = entity.fromLocation,
                to = entity.toLocation,
                purpose = entity.purpose,
                createdAt = "",
                updatedAt = "",
                details = null,
                isCheckout = if (checkout == null) "true" else "false",
                status = if (isSyncing) "SYNCING" else "OFFLINE",
                driverLog = DriverLogData(
                    id = "", carPlateNo = "", type = entity.type, tripTypeId = entity.tripTypeId ?: "",
                    from = entity.fromLocation ?: "", to = entity.toLocation ?: "", purpose = entity.purpose ?: "",
                    date = entity.date, startTime = entity.startTime, endTime = checkout?.endTime,
                    customerId = "", customerType = "", status = if (isSyncing) "SYNCING" else "OFFLINE", 
                    isDisabled = "false", cooperateDriverId = "", createdAt = "", updatedAt = "",
                    startKm = entity.startKm, endKm = checkout?.endKm, confirmedId = null, tripType = tripTypeMap[entity.tripTypeId]
                ),
                documents = emptyList(),
                actualUser = null,
                corporateUser = null,
                // Include offline image paths
                startImagePath = entity.startPhotoPath,
                endImagePath = checkout?.endPhotoPath
            )
        }
        // Remove offline logs that already exist on server (server may return client_uuid fields)
        val serverClientUuids = serverLogs.flatMap { listOfNotNull(it.clientUuid, it.checkoutClientUuid) }.toSet()
        val filteredOfflineLogs = offlineLogs.filter { it.id !in serverClientUuids }

        val updatedServerLogs = serverLogs.map { log ->
            // If server returned clientUuid (mapping to local uuid), merge offline start image/time
            val pendingInMatch = log.clientUuid?.let { pendingInByUuid[it] }
            val pendingOutMatch = pendingOut.find { it.serverRecordId == log.id } ?: log.checkoutClientUuid?.let { pendingOutByUuid[it] }

            var updated = log

            if (pendingInMatch != null) {
                // prefer server data but include offline image if server hasn't attached it yet
                val startImage = if (log.documents.isEmpty()) pendingInMatch.startPhotoPath else log.startImagePath
                updated = updated.copy(startImagePath = startImage)
            }

            if (pendingOutMatch != null) {
                updated = updated.copy(
                    endTime = pendingOutMatch.endTime,
                    endKm = pendingOutMatch.endKm,
                    isCheckout = "false",
                    status = if (pendingOutMatch.isSyncing) "SYNCING" else "OFFLINE",
                    endImagePath = pendingOutMatch.endPhotoPath
                )
            }

            updated
        }

        return filteredOfflineLogs + updatedServerLogs
    }

    init {
        // Auto-trigger sync when network is back
        viewModelScope.launch {
            networkStatus
                .collectLatest { status ->
                    if (status == ConnectivityObserver.Status.Available) {
                        repository.scheduleSyncWorker()
                    }
                }
        }

        // Best-effort load of trip types and reasons into memory
        viewModelScope.launch {
            try {
                val resp = repository.getTripTypes()
                if (resp.isSuccessful) {
                    resp.body()?.data?.forEach { t ->
                        tripTypeMap[t.id] = t.value
                    }
                }
            } catch (_: Exception) { }
        }

        viewModelScope.launch {
            try {
                val resp = repository.getReason()
                if (resp.isSuccessful) {
                    resp.body()?.data?.forEach { r ->
                        reasonMap[r.id] = r.value
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun clearDailyCheckIn() {
        dailyStartKm.value = ""
        dailySite.value = ""
        dailyPurpose.value = ""
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
        checkOutEndKm.value = TextFieldValue("")
        checkOutEndUri.value = null
        checkOutRemark.value = TextFieldValue("")
        checkOutPurpose.value = TextFieldValue("")
        currentCheckOutId = null
    }

    fun resetState() {
        _state.value = DriverLogState.Idle
    }

    fun checkInDriverLog(
        date: String,
        type: String,
        reasonId: String,
        site: String,
        purpose: String,
        remark: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri,
        context: Context) {
        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                if (!NetworkUtils.isInternetAvailable(context)) {
                    println("Offline Reason: $reasonId")
                    repository.checkInDriverLogOffline(date, type, reasonId,site,purpose, remark, startTime, startKm, startPhoto)
                    _state.value = DriverLogState.SavedOffline
                    clearDailyCheckIn()
                    return@launch
                }
                println("Hey site and purpose---- $site $purpose")
                val result = repository.checkInDriverLog(date, type, reasonId,site,purpose, remark, startTime, startKm, startPhoto)
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

    fun checkInTripDriverLog(date: String, type: String, tripTypeId: String, from: String, to: String, purpose: String, reasonId: String, startTime: String, startKm: String, startPhoto: Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                if (!NetworkUtils.isInternetAvailable(context)) {
                    repository.checkInTripDriverLogOffline(date, type, tripTypeId, from, to, purpose, reasonId, startTime, startKm, startPhoto)
                    _state.value = DriverLogState.SavedOffline
                    clearTripCheckIn()
                    return@launch
                }
                val result = repository.checkInTripDriverLog(date, type, tripTypeId, from, to, purpose, reasonId, startTime, startKm, startPhoto)
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

    fun checkOutDriverLog(recordId: String, remark: String, endTime: String, endKm: String, endPhoto: Uri, context: android.content.Context, localCheckInUuid: String? = null) {
        viewModelScope.launch {
            try {
                _state.value = DriverLogState.Loading
                if (!NetworkUtils.isInternetAvailable(context)) {
                    // Detect if recordId is a UUID (contains hyphens and is 36 chars) or server ID (short)
                    val isUuid = recordId.contains("-") && recordId.length == 36
                    val serverRecordId = if (isUuid) null else recordId
                    val uuid = if (isUuid) recordId else localCheckInUuid
                    repository.checkOutDriverLogOffline(serverRecordId, uuid, remark, endTime, endKm, endPhoto)
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
                    _driverLogList.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }


    private val _logSheetState = MutableStateFlow<LogSheetState>(LogSheetState.Idle)
    val logSheetState: StateFlow<LogSheetState> = _logSheetState

    fun saveLogSheet(date: String, uploadPhoto: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _logSheetState.value = LogSheetState.Loading
                if (!NetworkUtils.isInternetAvailable(context)) {
                    _logSheetState.value = LogSheetState.Error("No internet connection. Please try again later.")
                    return@launch
                }
                val result = repository.saveLogSheet(date, uploadPhoto)
                if (result.isSuccessful) {
                    _logSheetState.value = LogSheetState.Success(result.body()!!)
                } else {
                    _logSheetState.value = LogSheetState.Error("Failed: ${result.code()}")
                }
            } catch (e: Exception) {
                _logSheetState.value = LogSheetState.Error(ErrorHandler.getMessage(e))
            }
        }
    }


}
