package com.pv.transport.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.SessionEvents
import com.pv.transport.data.log.Data
import com.pv.transport.data.log.DriverLogData
import com.pv.transport.data.log.DriverLogResponse
import com.pv.transport.data.log.withCheckout
import com.pv.transport.data.log.LogSheetResponse
import com.pv.transport.data.log.stableKey
import com.pv.transport.local.data.OfflineCheckInEntity
import com.pv.transport.local.data.OfflineCheckOutEntity
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.network.ErrorHandler
import com.pv.transport.network.NetworkUtils
import com.pv.transport.network.NoInternetException
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val recentlySyncedCheckIns = repository.observeRecentlySyncedCheckIns()
    private val recentlySyncedCheckOuts = repository.observeRecentlySyncedCheckOuts()
    private val logsMutex = Mutex()

    private val visibleCheckOuts = combine(pendingCheckOuts, recentlySyncedCheckOuts) { pending, recent ->
        mergeLocalCheckOuts(pending, recent)
    }

    // Unified logs list
    val driverLogList: StateFlow<DriverLogListState> = combine(
        _driverLogList,
        pendingCheckIns,
        visibleCheckOuts,
        recentlySyncedCheckIns,
        repository.observeCachedDriverLogs()
    ) { currentState, pendingIn, pendingOut, recentlySynced, cache ->
        val localCheckIns = mergeLocalCheckIns(pendingIn, recentlySynced)
        val cachedLogs = inSelectedRange(cache?.logs ?: emptyList())
        val visibleLocal = pendingInSelectedRange(localCheckIns)
        if (currentState is DriverLogListState.Loading || currentState is DriverLogListState.Error) {
            if (cachedLogs.isNotEmpty() || visibleLocal.isNotEmpty()) {
                val merged = mergeLogs(cachedLogs, visibleLocal, pendingOut)
                DriverLogListState.Success(merged, 1, 1, isOffline = true)
            } else {
                currentState
            }
        } else if (currentState is DriverLogListState.Success) {
            val serverOrMemory = mergeByStableKey(currentState.logs + cachedLogs)
            currentState.copy(logs = mergeLogs(serverOrMemory, visibleLocal, pendingOut))
        } else {
            currentState
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DriverLogListState.Loading)

    private fun mergeLocalCheckIns(
        pending: List<OfflineCheckInEntity>,
        recentlySynced: List<OfflineCheckInEntity>
    ): List<OfflineCheckInEntity> {
        val pendingUuids = pending.map { it.uuid }.toSet()
        return pending + recentlySynced.filter { it.uuid !in pendingUuids }
    }

    private fun mergeLocalCheckOuts(
        pending: List<OfflineCheckOutEntity>,
        recentlySynced: List<OfflineCheckOutEntity>
    ): List<OfflineCheckOutEntity> {
        val pendingUuids = pending.map { it.uuid }.toSet()
        return pending + recentlySynced.filter { it.uuid !in pendingUuids }
    }

    /** Offline rows are only meaningful for the date range the list is currently showing. */
    private fun pendingInSelectedRange(pending: List<OfflineCheckInEntity>): List<OfflineCheckInEntity> {
        val start = lastLogQueryStart ?: return pending
        val end = lastLogQueryEnd ?: return pending
        return pending.filter { it.date.take(10) in start..end }
    }

    private fun inSelectedRange(logs: List<Data>): List<Data> {
        val start = lastLogQueryStart ?: return logs
        val end = lastLogQueryEnd ?: return logs
        return logs.filter { log ->
            val date = log.driverLog?.date?.take(10) ?: return@filter true
            date in start..end
        }
    }

    private var currentPage = 1
    private var allLogs = mutableListOf<Data>()
    private var lastLogQueryStart: String? = null
    private var lastLogQueryEnd: String? = null
    // Set after a local save so the next list visit refetches instead of reusing the cached page
    private var forceNextLogRefresh = false
    private var logsJob: Job? = null
    // In-memory cache of mappings so offline entries can show readable labels
    private val tripTypeMap = mutableMapOf<String, String>()
    private val reasonMap = mutableMapOf<String, String>()

    // Survives leaving Add Log and coming back (Daily vs Trip).
    val checkInLogType = MutableStateFlow("Daily")

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
    val checkOutSite = MutableStateFlow(TextFieldValue(""))
    val checkOutPurpose = MutableStateFlow(TextFieldValue(""))

    var currentCheckOutId: String? = null
        private set

    fun setCurrentCheckOutId(id: String) {
        currentCheckOutId = id
    }

    private val logSortComparator = compareByDescending<Data> {
        it.driverLog?.date?.take(10).orEmpty()
    }.thenByDescending {
        it.startTime
    }.thenBy {
        it.stableKey
    }

    private fun mergeLogs(serverLogs: List<Data>, pendingIn: List<OfflineCheckInEntity>, pendingOut: List<OfflineCheckOutEntity>): List<Data> {
        val pendingInByUuid = pendingIn.associateBy { it.uuid }
        val pendingOutByUuid = pendingOut.associateBy { it.uuid }
        val pendingOutByLocalCheckIn = pendingOut.associateBy { it.localCheckInUuid }

        val offlineLogs = pendingIn.map { entity ->
            val checkout = pendingOutByLocalCheckIn[entity.uuid]
            val isSyncing = entity.isSyncing || (checkout?.isSyncing ?: false)
            val status = when {
                isSyncing -> "SYNCING"
                entity.isSynced -> "pending"
                else -> "OFFLINE"
            }

            Data(
                id = entity.serverRecordId?.takeIf { it.isNotBlank() } ?: entity.uuid,
                clientUuid = entity.uuid,
                startTime = entity.startTime,
                endTime = checkout?.endTime,
                reason = reasonMap[entity.reason] ?: entity.reason,
                remark = checkout?.remark ?: entity.remark,
                startKm = entity.startKm,
                endKm = checkout?.endKm,
                driverLogId = entity.serverRecordId.orEmpty(),
                type = entity.type,
                tripTypeId = entity.tripTypeId,
                from = entity.fromLocation,
                to = entity.toLocation,
                purpose = checkout?.purpose?.takeUnless { it.isBlank() } ?: entity.purpose,
                site = checkout?.site ?: entity.site,
                createdAt = "",
                updatedAt = "",
                details = null,
                isCheckout = if (checkout == null) "true" else "false",
                status = status,
                driverLog = DriverLogData(
                    id = entity.serverRecordId.orEmpty(), carPlateNo = "", type = entity.type, tripTypeId = entity.tripTypeId ?: "",
                    from = entity.fromLocation ?: "", to = entity.toLocation ?: "",
                    purpose = (checkout?.purpose?.takeUnless { it.isBlank() } ?: entity.purpose) ?: "",
                    date = entity.date, startTime = entity.startTime, endTime = checkout?.endTime,
                    customerId = "", customerType = "", status = status,
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
        // Drop local rows once the same record is already present from the server/cache.
        val serverIds = serverLogs.map { it.id }.toSet()
        val serverClientUuids = serverLogs.flatMap { listOfNotNull(it.clientUuid, it.checkoutClientUuid) }.toSet()
        val filteredOfflineLogs = offlineLogs.filter { local ->
            val uuid = local.clientUuid
            uuid !in serverClientUuids &&
                uuid !in serverIds &&
                local.id !in serverIds &&
                local.id !in serverClientUuids
        }

        val uuidByServerId = pendingIn.mapNotNull { entity ->
            entity.serverRecordId?.takeIf { it.isNotBlank() }?.let { it to entity.uuid }
        }.toMap()

        val updatedServerLogs = serverLogs.map { log ->
            // If server returned clientUuid (mapping to local uuid), merge offline start image/time
            val mappedUuid = log.clientUuid?.takeIf { it.isNotBlank() } ?: uuidByServerId[log.id]
            val pendingInMatch = mappedUuid?.let { pendingInByUuid[it] }
            val pendingOutMatch = pendingOut.find { it.serverRecordId == log.id }
                ?: log.checkoutClientUuid?.let { pendingOutByUuid[it] }
                ?: mappedUuid?.let { pendingOutByLocalCheckIn[it] }
                ?: log.clientUuid?.let { pendingOutByLocalCheckIn[it] }

            var updated = if (!mappedUuid.isNullOrBlank() && log.clientUuid.isNullOrBlank()) {
                log.copy(clientUuid = mappedUuid)
            } else {
                log
            }

            if (pendingInMatch != null) {
                // Keep the local file even after documents exist so the list thumb does not
                // wait on a new S3 download for a photo the device already has.
                val startImage = pendingInMatch.startPhotoPath.takeIf { it.isNotBlank() }
                    ?: log.startImagePath
                updated = updated.copy(
                    startImagePath = startImage,
                    site = updated.site?.takeUnless { it.isBlank() }
                        ?: pendingInMatch.site.takeUnless { it.isBlank() },
                    purpose = updated.purpose?.takeUnless { it.isBlank() }
                        ?: pendingInMatch.purpose?.takeUnless { it.isBlank() },
                    remark = updated.remark?.takeUnless { it.isBlank() }
                        ?: pendingInMatch.remark.takeUnless { it.isBlank() }
                )
            }

            if (pendingOutMatch != null) {
                val serverHasCheckout = !updated.endTime.isNullOrBlank() || !updated.endKm.isNullOrBlank()
                val checkoutPurpose = pendingOutMatch.purpose.takeUnless { it.isBlank() }
                val checkoutRemark = pendingOutMatch.remark.takeUnless { it.isBlank() }
                val checkoutSite = pendingOutMatch.site.takeUnless { it.isBlank() }
                // Checkout is the later edit — keep those fields even after sync if the
                // server still echoes the original check-in purpose/site/remark.
                if (!pendingOutMatch.isSynced || !serverHasCheckout) {
                    val overlayStatus = when {
                        pendingOutMatch.isSyncing -> "SYNCING"
                        pendingOutMatch.isSynced -> updated.status.takeUnless { it.equals("OFFLINE", true) } ?: "pending"
                        else -> "OFFLINE"
                    }
                    updated = updated.copy(
                        endTime = pendingOutMatch.endTime,
                        endKm = pendingOutMatch.endKm,
                        isCheckout = "false",
                        status = overlayStatus,
                        checkoutClientUuid = pendingOutMatch.uuid,
                        endImagePath = pendingOutMatch.endPhotoPath,
                        site = checkoutSite ?: updated.site,
                        purpose = checkoutPurpose ?: updated.purpose,
                        remark = checkoutRemark ?: updated.remark,
                        driverLog = updated.driverLog?.withCheckout(
                            pendingOutMatch.endTime,
                            pendingOutMatch.endKm,
                            checkoutPurpose
                        )
                    )
                } else {
                    updated = updated.copy(
                        endImagePath = updated.endImagePath
                            ?: pendingOutMatch.endPhotoPath.takeUnless { it.isBlank() },
                        site = checkoutSite ?: updated.site,
                        purpose = checkoutPurpose ?: updated.purpose,
                        remark = checkoutRemark ?: updated.remark,
                        driverLog = updated.driverLog?.withCheckout(
                            updated.endTime,
                            updated.endKm,
                            checkoutPurpose
                        )
                    )
                }
            }

            updated
        }

        return (filteredOfflineLogs + updatedServerLogs)
            .distinctBy { it.stableKey }
            .sortedWith(logSortComparator)
    }

    init {
        viewModelScope.launch {
            SessionEvents.sessionDataClearedEvent.collectLatest {
                logsJob?.cancel()
                currentPage = 1
                allLogs.clear()
                forceNextLogRefresh = true
                _driverLogList.value = DriverLogListState.Loading
            }
        }

        // Auto-trigger sync when network is back
        viewModelScope.launch {
            networkStatus
                .collectLatest { status ->
                    if (status == ConnectivityObserver.Status.Available) {
                        repository.scheduleSyncWorker()
                        refreshDriverLogsSilent()
                    }
                }
        }

        // After pending local rows shrink (synced + removed), refresh list for API status
        viewModelScope.launch {
            var previousPendingCount = Int.MAX_VALUE
            combine(pendingCheckIns, pendingCheckOuts) { checkIns, checkOuts ->
                checkIns.size + checkOuts.size
            }.collectLatest { pendingCount ->
                if (pendingCount < previousPendingCount &&
                    previousPendingCount != Int.MAX_VALUE &&
                    networkStatus.value == ConnectivityObserver.Status.Available
                ) {
                    refreshDriverLogsSilent()
                }
                previousPendingCount = pendingCount
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

    private suspend fun refreshDriverLogsSilent() {
        val start = lastLogQueryStart ?: return
        val end = lastLogQueryEnd ?: return
        try {
            val result = repository.getDriverLogs(start, end)
            if (result.isSuccessful) {
                val body = result.body() ?: return
                applyPageOne(body.data, body.meta.lastPage.toInt())
            }
        } catch (_: Exception) {
            // Keep current list; pending local cards still visible via merge
        }
    }

    private fun hasCheckoutData(log: Data): Boolean =
        !log.endTime.isNullOrBlank() || !log.endKm.isNullOrBlank()

    private fun preferRicherLog(primary: Data, secondary: Data): Data {
        var result = primary
        if (!hasCheckoutData(primary) && hasCheckoutData(secondary)) {
            result = result.copy(
                endTime = secondary.endTime,
                endKm = secondary.endKm,
                isCheckout = "false",
                checkoutClientUuid = result.checkoutClientUuid ?: secondary.checkoutClientUuid,
                endImagePath = result.endImagePath ?: secondary.endImagePath,
                driverLog = result.driverLog?.withCheckout(secondary.endTime, secondary.endKm)
            )
        } else if (result.endImagePath.isNullOrBlank() && !secondary.endImagePath.isNullOrBlank()) {
            result = result.copy(endImagePath = secondary.endImagePath)
        }
        if (hasCheckoutData(secondary)) {
            val purpose = secondary.purpose?.takeUnless { it.isBlank() }
            if (purpose != null) {
                result = result.copy(
                    purpose = purpose,
                    remark = secondary.remark?.takeUnless { it.isBlank() } ?: result.remark,
                    site = secondary.site?.takeUnless { it.isBlank() } ?: result.site,
                    driverLog = result.driverLog?.withCheckout(result.endTime, result.endKm, purpose)
                )
            }
        }
        return result
    }

    private fun mergeByStableKey(logs: List<Data>): List<Data> {
        return logs.groupBy { it.stableKey }.values.map { group ->
            group.reduce { acc, log -> preferRicherLog(acc, log) }
        }
    }

    private fun upsertPageOne(incoming: List<Data>): List<Data> {
        val incomingKeys = incoming.map { it.stableKey }.toSet()
        val incomingIds = incoming.map { it.id }.toSet()
        val incomingClientUuids = incoming.mapNotNull { it.clientUuid?.takeIf { uuid -> uuid.isNotBlank() } }.toSet()
        val oldByKey = allLogs.associateBy { it.stableKey }
        val oldById = allLogs.associateBy { it.id }
        val mergedIncoming = incoming.map { new ->
            val old = oldByKey[new.stableKey]
                ?: oldById[new.id]
                ?: new.clientUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
                    allLogs.find { it.clientUuid == uuid }
                }
            if (old != null) preferRicherLog(new, old) else new
        }
        val kept = allLogs.filter { old ->
            val key = old.stableKey
            key !in incomingKeys &&
                old.id !in incomingIds &&
                old.id !in incomingClientUuids &&
                old.clientUuid?.takeIf { it.isNotBlank() } !in incomingClientUuids
        }
        return mergedIncoming + kept
    }

    private suspend fun applyPageOne(incoming: List<Data>, lastPage: Int, replaceAll: Boolean = false) {
        logsMutex.withLock {
            currentPage = 1
            val next = if (replaceAll) incoming else upsertPageOne(incoming)
            allLogs.clear()
            allLogs.addAll(next)
            _driverLogList.value = DriverLogListState.Success(
                logs = allLogs.toList(),
                currentPage = currentPage,
                lastPage = lastPage
            )
        }
    }

    fun clearDailyCheckIn() {
        checkInLogType.value = "Daily"
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
        checkInLogType.value = "Daily"
    }

    fun clearCheckOut() {
        checkOutEndKm.value = TextFieldValue("")
        checkOutEndUri.value = null
        checkOutRemark.value = TextFieldValue("")
        checkOutSite.value = TextFieldValue("")
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
                // Local-first: always persist, then auto-sync when network is good
                _state.value = DriverLogState.Loading
                repository.checkInDriverLogOffline(
                    date, type, reasonId, site, purpose, remark, startTime, startKm, startPhoto
                )
                forceNextLogRefresh = true
                _state.value = DriverLogState.SavedOffline
                clearDailyCheckIn()
            } catch (e: Exception) {
                _state.value = DriverLogState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun checkInTripDriverLog(date: String, type: String, tripTypeId: String, from: String, to: String, purpose: String, reasonId: String, startTime: String, startKm: String, startPhoto: Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                // Local-first: always persist, then auto-sync when network is good
                _state.value = DriverLogState.Loading
                repository.checkInTripDriverLogOffline(
                    date, type, tripTypeId, from, to, purpose, reasonId, startTime, startKm, startPhoto
                )
                forceNextLogRefresh = true
                _state.value = DriverLogState.SavedOffline
                clearTripCheckIn()
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
        localCheckInUuid: String? = null,
        site: String = "",
        purpose: String = ""
    ) {
        viewModelScope.launch {
            try {
                // Local-first: always persist checkout, then auto-sync when network is good
                _state.value = DriverLogState.Loading
                val isUuid = recordId.contains("-") && recordId.length == 36
                val serverRecordId = if (isUuid) null else recordId.takeIf { it.isNotBlank() }
                val checkInUuid = when {
                    isUuid -> recordId
                    !localCheckInUuid.isNullOrBlank() -> localCheckInUuid
                    else -> null
                }
                repository.checkOutDriverLogOffline(
                    serverRecordId, checkInUuid, remark, endTime, endKm, endPhoto,
                    site = site, purpose = purpose
                )
                forceNextLogRefresh = true
                _state.value = DriverLogState.SavedOffline
                clearCheckOut()
            } catch (e: Exception) {
                _state.value = DriverLogState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getDriverLogs(start: String, end: String, force: Boolean = false) {
        val sameRange = lastLogQueryStart == start && lastLogQueryEnd == end
        if (!force &&
            !forceNextLogRefresh &&
            sameRange &&
            _driverLogList.value is DriverLogListState.Success
        ) {
            return
        }
        forceNextLogRefresh = false
        // Keep the current page visible while refreshing the same range, but never show
        // another range's rows while a new range is loading.
        val keepList = sameRange && _driverLogList.value is DriverLogListState.Success
        logsJob?.cancel()
        logsJob = viewModelScope.launch {
            try {
                lastLogQueryStart = start
                lastLogQueryEnd = end
                // Visiting/refreshing the list re-attempts any pending offline records
                runCatching { repository.scheduleSyncIfPending() }
                if (!keepList) {
                    _driverLogList.value = DriverLogListState.Loading
                }
                val result = repository.getDriverLogs(start, end)
                if (result.isSuccessful) {
                    val body = result.body()!!
                    applyPageOne(body.data, body.meta.lastPage.toInt(), replaceAll = !keepList)
                } else if (!keepList) {
                    _driverLogList.value = DriverLogListState.Error(ErrorHandler.fromResponse(result))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: NoInternetException) {
                if (!keepList) {
                    _driverLogList.value = DriverLogListState.Error(e.message.toString())
                }
            } catch (e: Exception) {
                if (!keepList) {
                    _driverLogList.value = DriverLogListState.Error(ErrorHandler.getMessage(e))
                }
            }
        }
    }

    fun loadMoreLogs(start: String, end: String) {
        val currentState = _driverLogList.value
        if (currentState is DriverLogListState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _driverLogList.value = currentState.copy(isLoadingMore = true)
                    val nextPage = currentPage + 1
                    val result = repository.getDriverLogs(start, end, nextPage)
                    if (result.isSuccessful) {
                        val body = result.body()!!
                        logsMutex.withLock {
                            currentPage = nextPage
                            val existingKeys = allLogs.map { it.stableKey }.toSet()
                            val existingIds = allLogs.map { it.id }.toSet()
                            allLogs.addAll(
                                body.data.filter { log ->
                                    log.stableKey !in existingKeys && log.id !in existingIds
                                }
                            )
                            _driverLogList.value = DriverLogListState.Success(
                                allLogs.toList(),
                                currentPage,
                                body.meta.lastPage.toInt()
                            )
                        }
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
                    _logSheetState.value = LogSheetState.Error(ErrorHandler.fromResponse(result))
                }
            } catch (e: Exception) {
                _logSheetState.value = LogSheetState.Error(ErrorHandler.getMessage(e))
            }
        }
    }


}
