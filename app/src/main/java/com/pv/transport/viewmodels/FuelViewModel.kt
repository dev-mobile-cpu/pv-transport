package com.pv.transport.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.SessionEvents
import com.pv.transport.data.fuel.FuelCompaniesResponse
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.data.fuel.FuelTypeResponse
import com.pv.transport.data.fuel.GeneralResponse
import com.pv.transport.data.fuel.Transaction
import com.pv.transport.data.fuel.WalletResponse
import com.pv.transport.local.data.OfflineFuelLogEntity
import com.pv.transport.local.data.SyncedRecordMapping
import com.pv.transport.local.data.toFuelLogData
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.network.ErrorHandler
import com.pv.transport.repository.FuelRepository
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
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@HiltViewModel
class FuelViewModel @Inject constructor(
    private val repo: FuelRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    sealed class FuelTypeState {
        object Idle : FuelTypeState()
        object Loading : FuelTypeState()
        data class Success(val response: FuelTypeResponse) : FuelTypeState()
        data class Error(val message: String) : FuelTypeState()
    }

    sealed class FuelRequestState {
        object Idle : FuelRequestState()
        object Loading : FuelRequestState()
        data class Success(val response: GeneralResponse) : FuelRequestState()
        data class Error(val message: String) : FuelRequestState()
    }

    sealed class AllFuelRequestState {
        object Idle : AllFuelRequestState()
        object Loading : AllFuelRequestState()
        data class Success(val response: List<FuelRequestData>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false) : AllFuelRequestState()
        data class Error(val message: String) : AllFuelRequestState()
    }

    sealed class FuelLogState {
        object Idle : FuelLogState()
        object Loading : FuelLogState()
        object SavedOffline : FuelLogState()
        data class Success(val response: GeneralResponse) : FuelLogState()
        data class Error(val message: String) : FuelLogState()
    }

    sealed class AllFuelLogState {
        object Idle : AllFuelLogState()
        object Loading : AllFuelLogState()
        data class Success(val response: List<FuelLogData>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false,val isOffline: Boolean = false) : AllFuelLogState()
        data class Error(val message: String) : AllFuelLogState()
    }

    sealed class WalletState {
        object Idle : WalletState()
        object Loading : WalletState()
        data class Success(val response: WalletResponse, val currentPage: Int = 1, val lastPage: Int = 1, val isLoadingMore: Boolean = false) : WalletState()
        data class Error(val message: String) : WalletState()
    }

    sealed class FuelCompaniesState {
        object Idle : FuelCompaniesState()
        object Loading : FuelCompaniesState()
        data class Success(val response: FuelCompaniesResponse) : FuelCompaniesState()
        data class Error(val message: String) : FuelCompaniesState()
    }

    private val _state = MutableStateFlow<FuelTypeState>(FuelTypeState.Idle)
    val state: StateFlow<FuelTypeState> = _state

    private val _allRequestState = MutableStateFlow<AllFuelRequestState>(AllFuelRequestState.Idle)
    val allRequestState: StateFlow<AllFuelRequestState> = _allRequestState

    private val _requestState = MutableStateFlow<FuelRequestState>(FuelRequestState.Idle)
    val requestState: StateFlow<FuelRequestState> = _requestState

    private val _fuelLogState = MutableStateFlow<FuelLogState>(FuelLogState.Idle)
    val fuelLogState: StateFlow<FuelLogState> = _fuelLogState

    private val _allFuelLogState = MutableStateFlow<AllFuelLogState>(AllFuelLogState.Idle)
    val allFuelLogState: StateFlow<AllFuelLogState> = _allFuelLogState

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Idle)
    val walletState: StateFlow<WalletState> = _walletState

    private val _fuelCompaniesState = MutableStateFlow<FuelCompaniesState>(FuelCompaniesState.Idle)
    val fuelCompaniesState: StateFlow<FuelCompaniesState> = _fuelCompaniesState

    val pendingFuelLogs: StateFlow<List<OfflineFuelLogEntity>> =
        repo.observePendingFuelLogs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val recentlySyncedFuelLogMappings: StateFlow<List<SyncedRecordMapping>> =
        repo.observeRecentlySyncedFuelLogMappings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),

            connectivityObserver.getCurrentStatus()
        )


    private var currentPage = 1
    private var allFuelRequest = mutableListOf<FuelRequestData>()
    private var allFuelLog = mutableListOf<FuelLogData>()
    private var lastFuelLogStart: String? = null
    private var lastFuelLogEnd: String? = null
    private var lastFuelRequestStart: String? = null
    private var lastFuelRequestEnd: String? = null
    // Set after a local save so the next list visit refetches instead of reusing the cached page
    private var forceNextFuelLogRefresh = false
    private var forceNextFuelRequestRefresh = false
    private var fuelLogJob: Job? = null
    private var fuelRequestJob: Job? = null

    // Add Fuel Request Form States
    var addRequestCategory = MutableStateFlow("fuel_request")
    var addRequestAmount = MutableStateFlow("")
    var addRequestRemark = MutableStateFlow("")
    var addRequestSelectedType = MutableStateFlow("")
    var addRequestSelectedIndex = MutableStateFlow(0)
    var addRequestFiles = MutableStateFlow<List<Uri>>(emptyList())

    // Add Fuel Log Form States
    var addLogAmount = MutableStateFlow("")
    var addLogLiter = MutableStateFlow("")
    var addLogCurrentKm = MutableStateFlow("")
    var addLogFuelShop = MutableStateFlow("")
    var addLogSelectedFuelType = MutableStateFlow("")
    var addLogSelectedFuelTypeId = MutableStateFlow(0)
    var addLogDate = MutableStateFlow(LocalDate.now())
    var addLogUriList = MutableStateFlow<List<Uri>>(emptyList())
    var addLogCurrentUri = MutableStateFlow<Uri?>(null)
    var addLogSelectedPayment = MutableStateFlow("Credit")
    var addLogSelectedFuelCompany = MutableStateFlow("")
    var addLogSelectedCompanyIndex = MutableStateFlow(0)

    val unifiedFuelLogs: StateFlow<AllFuelLogState> =
        combine(
            _allFuelLogState,
            pendingFuelLogs,
            recentlySyncedFuelLogMappings,
            repo.observeCachedFuelLogs()
        ) { currentState, pending, syncedMappings, cache ->

            val uuidByServerId = syncedMappings.associate { it.serverRecordId to it.uuid }
            val clientTimestampByUuid = (pending.map { it.uuid to it.clientTimestamp } +
                    syncedMappings.map { it.uuid to it.clientTimestamp }).toMap()

            val pendingLogs = pending.map { it.toFuelLogData() }
                .filter { isInSelectedFuelLogRange(it.date) }

            val cacheLogs = cache?.logs
                ?.map { it.withStableFuelLogUuid(uuidByServerId) }
                ?.filter { isInSelectedFuelLogRange(it.date) }
                ?: emptyList()

            if (currentState is AllFuelLogState.Loading ||
                currentState is AllFuelLogState.Error
            ) {
                if (cacheLogs.isNotEmpty() || pendingLogs.isNotEmpty()) {
                    val merged = mergeFuelLogs(cacheLogs + pendingLogs, clientTimestampByUuid)

                    AllFuelLogState.Success(
                        response = merged,
                        currentPage = 1,
                        lastPage = 1,
                        isLoadingMore = false
                    )
                } else {
                    currentState
                }

            } else if (currentState is AllFuelLogState.Success) {
                val serverLogs = currentState.response
                    .map { it.withStableFuelLogUuid(uuidByServerId) }
                val merged = mergeFuelLogs(serverLogs + pendingLogs, clientTimestampByUuid)
                currentState.copy(
                    response = merged
                )
            } else {
                currentState
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AllFuelLogState.Loading
        )

    /** Cached and offline rows are only meaningful for the date range the list is showing. */
    private fun isInSelectedFuelLogRange(date: String): Boolean {
        val today = LocalDate.now().toString()
        val start = lastFuelLogStart ?: today
        val end = lastFuelLogEnd ?: today
        return date.take(10) in start..end
    }

    private fun FuelLogData.withStableFuelLogUuid(uuidByServerId: Map<String, String>): FuelLogData {
        val mappedUuid = uuidByServerId[id] ?: return this
        return if (uuid == mappedUuid) this else copy(uuid = mappedUuid)
    }

    private fun mergeFuelLogs(
        logs: List<FuelLogData>,
        clientTimestampByUuid: Map<String, Long>
    ): List<FuelLogData> {
        return logs
            .distinctBy { it.uuid ?: it.id }
            .sortedWith(
                compareByDescending<FuelLogData> { it.date.take(10) }
                    .thenByDescending { log ->
                        log.uuid?.let { clientTimestampByUuid[it] }
                            ?: parseServerTimestamp(log.createdAt)
                    }
            )
    }

    private fun parseServerTimestamp(value: String?): Long {
        if (value.isNullOrBlank()) return 0L
        return runCatching {
            LocalDateTime.parse(value.replace(" ", "T"))
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrDefault(0L)
    }

    init {
        viewModelScope.launch {
            SessionEvents.sessionDataClearedEvent.collectLatest {
                fuelLogJob?.cancel()
                fuelRequestJob?.cancel()
                currentPage = 1
                allFuelLog.clear()
                allFuelRequest.clear()
                forceNextFuelLogRefresh = true
                forceNextFuelRequestRefresh = true
                _allFuelLogState.value = AllFuelLogState.Loading
                _allRequestState.value = AllFuelRequestState.Loading
            }
        }

        // Retry pending uploads as soon as the network is back, then show the server state
        viewModelScope.launch {
            networkStatus.collectLatest { status ->
                if (status == ConnectivityObserver.Status.Available) {
                    runCatching { repo.scheduleSyncIfPending() }
                    refreshFuelLogsSilent()
                }
            }
        }

        // Pending rows disappear once they are uploaded, so refetch to show the synced records
        viewModelScope.launch {
            var previousPendingCount = Int.MAX_VALUE
            pendingFuelLogs.collectLatest { pending ->
                if (pending.size < previousPendingCount &&
                    previousPendingCount != Int.MAX_VALUE &&
                    networkStatus.value == ConnectivityObserver.Status.Available
                ) {
                    refreshFuelLogsSilent()
                }
                previousPendingCount = pending.size
            }
        }
    }

    fun clearAddFuelRequest() {
        addRequestCategory.value = "fuel_request"
        addRequestAmount.value = ""
        addRequestRemark.value = ""
        addRequestSelectedType.value = ""
        addRequestSelectedIndex.value = 0
        addRequestFiles.value = emptyList()
    }

    fun clearAddFuelLog() {
        addLogAmount.value = ""
        addLogLiter.value = ""
        addLogCurrentKm.value = ""
        addLogFuelShop.value = ""
        addLogUriList.value = emptyList()
        addLogCurrentUri.value = null
        addLogSelectedPayment.value = "Credit"
        addLogDate.value = LocalDate.now()

    }

    fun getFuelType() {
        viewModelScope.launch {
            try {
                _state.value = FuelTypeState.Loading
                val result = repo.getFuelTypes()
                if (result.isSuccessful) {
                    _state.value = FuelTypeState.Success(result.body()!!)
                } else {
                    _state.value = FuelTypeState.Error(ErrorHandler.fromResponse(result))
                }
            } catch (e: Exception) {
                _state.value = FuelTypeState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun saveFundRequest(fuelRequest: FuelRequest, files: List<Uri> = emptyList()) {
        viewModelScope.launch {
            try {
                _requestState.value = FuelRequestState.Loading
                val response = repo.saveFundRequest(fuelRequest, files)
                if (response.isSuccessful) {
                    val body = response.body() ?: GeneralResponse(message = "Success", success = true)
                    _requestState.value = FuelRequestState.Success(body)
                    clearAddFuelRequest()
                    forceNextFuelRequestRefresh = true

                } else {
                    _requestState.value = FuelRequestState.Error(ErrorHandler.fromResponse(response))
                }
            } catch (e: Exception) {
                _requestState.value = FuelRequestState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getFuelRequest(startDate: String, endDate: String, force: Boolean = false) {
        val sameRange = lastFuelRequestStart == startDate && lastFuelRequestEnd == endDate
        if (!force &&
            !forceNextFuelRequestRefresh &&
            sameRange &&
            _allRequestState.value is AllFuelRequestState.Success
        ) {
            return
        }
        forceNextFuelRequestRefresh = false
        // Keep the current page visible while refreshing the same range, but never show
        // another range's rows while a new range is loading.
        val keepList = sameRange && _allRequestState.value is AllFuelRequestState.Success
        fuelRequestJob?.cancel()
        fuelRequestJob = viewModelScope.launch {
            try {
                lastFuelRequestStart = startDate
                lastFuelRequestEnd = endDate
                if (!keepList) {
                    _allRequestState.value = AllFuelRequestState.Loading
                }
                currentPage = 1
                val response = repo.getFuelRequest(startDate, endDate)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    allFuelRequest.clear()
                    allFuelRequest.addAll(body.data)
                    _allRequestState.value = AllFuelRequestState.Success(
                        allFuelRequest.toList(),
                        currentPage,
                        body.meta.lastPage.toInt()
                    )
                } else if (!keepList) {
                    _allRequestState.value = AllFuelRequestState.Error(ErrorHandler.fromResponse(response))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_allRequestState.value !is AllFuelRequestState.Success) {
                    _allRequestState.value = AllFuelRequestState.Error(ErrorHandler.getMessage(e))
                }
            }
        }
    }

    fun loadMoreRequest(startDate: String, endDate: String) {
        val currentState = _allRequestState.value
        if (currentState is AllFuelRequestState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _allRequestState.value = currentState.copy(isLoadingMore = true)
                    val nextPage = currentPage + 1
                    val response = repo.getFuelRequest(startDate, endDate, nextPage)
                    if (response.isSuccessful) {
                        val body = response.body()!!
                        currentPage = nextPage
                        allFuelRequest.addAll(body.data)
                        _allRequestState.value = AllFuelRequestState.Success(allFuelRequest.toList(), currentPage, body.meta.lastPage.toInt())
                    } else {
                        _allRequestState.value = currentState.copy(isLoadingMore = false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _allRequestState.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun saveFuelLog(
        carPlateNo: String,
        date: String,
        fuelCompanyId: String,
        fuelShop: String,
        fuelTypeId: String,
        fuelAmount: String,
        fuelLiter: String,
        files: List<Uri>,
        currentKm: String,
        currentKmPhoto: Uri,
        walletBucket: String,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                // Local-first: always persist, then auto-sync when network is good
                _fuelLogState.value = FuelLogState.Loading
                repo.saveFuelLogOffline(carPlateNo, date, fuelCompanyId, fuelShop, fuelTypeId, fuelAmount, fuelLiter, files, currentKm, currentKmPhoto, walletBucket)
                forceNextFuelLogRefresh = true
                _fuelLogState.value = FuelLogState.SavedOffline
                clearAddFuelLog()
            } catch (e: Exception) {
                _fuelLogState.value = FuelLogState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getFuelLog(startDate: String, endDate: String, force: Boolean = false) {
        val sameRange = lastFuelLogStart == startDate && lastFuelLogEnd == endDate
        if (!force &&
            !forceNextFuelLogRefresh &&
            sameRange &&
            _allFuelLogState.value is AllFuelLogState.Success
        ) {
            return
        }
        forceNextFuelLogRefresh = false
        // Keep the current page visible while refreshing the same range, but never show
        // another range's rows while a new range is loading.
        val keepList = sameRange && _allFuelLogState.value is AllFuelLogState.Success
        fuelLogJob?.cancel()
        fuelLogJob = viewModelScope.launch {
            try {
                lastFuelLogStart = startDate
                lastFuelLogEnd = endDate
                // Visiting/refreshing the list re-attempts any pending offline records
                runCatching { repo.scheduleSyncIfPending() }
                if (!keepList) {
                    _allFuelLogState.value = AllFuelLogState.Loading
                }
                currentPage = 1
                val response = repo.getFuelLogs(startDate, endDate)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    allFuelLog.clear()
                    allFuelLog.addAll(body.data)
                    _allFuelLogState.value = AllFuelLogState.Success(
                        allFuelLog.toList(),
                        currentPage,
                        body.meta.lastPage.toInt()
                    )
                } else if (!keepList) {
                    _allFuelLogState.value = AllFuelLogState.Error(ErrorHandler.fromResponse(response))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_allFuelLogState.value !is AllFuelLogState.Success) {
                    _allFuelLogState.value = AllFuelLogState.Error(ErrorHandler.getMessage(e))
                }
            }
        }
    }

    /** Refresh the visible range without touching Loading/Error state, e.g. right after a sync. */
    private suspend fun refreshFuelLogsSilent() {
        val start = lastFuelLogStart ?: return
        val end = lastFuelLogEnd ?: return
        try {
            val response = repo.getFuelLogs(start, end)
            if (response.isSuccessful) {
                val body = response.body()!!
                currentPage = 1
                allFuelLog.clear()
                allFuelLog.addAll(body.data)
                _allFuelLogState.value = AllFuelLogState.Success(
                    allFuelLog.toList(),
                    currentPage,
                    body.meta.lastPage.toInt()
                )
            }
        } catch (_: Exception) {
            // Keep current list; pending local rows still show via the unified flow
        }
    }

    fun loadMoreFuelLog(startDate: String, endDate: String) {
        val currentState = _allFuelLogState.value
        if (currentState is AllFuelLogState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _allFuelLogState.value = currentState.copy(isLoadingMore = true)
                    val nextPage = currentPage + 1
                    val response = repo.getFuelLogs(startDate, endDate, nextPage)
                    if (response.isSuccessful) {
                        val body = response.body()!!
                        currentPage = nextPage
                        allFuelLog.addAll(body.data)
                        _allFuelLogState.value = AllFuelLogState.Success(allFuelLog.toList(), currentPage, body.meta.lastPage.toInt())
                    } else {
                        _allFuelLogState.value = currentState.copy(isLoadingMore = false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _allFuelLogState.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun getWalletBalance(force: Boolean = false) {
        if (!force && _walletState.value is WalletState.Success) {
            return
        }
        viewModelScope.launch {
            try {
                _walletState.value = WalletState.Loading
                val response = repo.getWalletBalance(10)
                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    transactionList.clear()
                    transactionList.addAll(responseBody.data.transactions.data)
                    val lastPage = responseBody.data.transactions.meta.lastPage.toInt()
                    transactionPage = 1
                    endReached = transactionPage >= lastPage
                    _walletState.value = WalletState.Success(responseBody, 1, lastPage, false)
                } else {
                    _walletState.value = WalletState.Error(ErrorHandler.fromResponse(response))
                }
            } catch (e: Exception) {
                _walletState.value = WalletState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    private var isLoading = false
    private var endReached = false
    private var transactionPage = 1
    private val transactionList = mutableListOf<Transaction>()

    fun loadMoreTransactions() {
        val currentState = _walletState.value
        if (currentState is WalletState.Success && !currentState.isLoadingMore && !endReached && !isLoading) {
            viewModelScope.launch {
                try {
                    _walletState.value = currentState.copy(isLoadingMore = true)
                    isLoading = true
                    val nextPage = transactionPage + 1
                    val response = repo.getWalletTransactions(nextPage)
                    if (response.isSuccessful) {
                        val body = response.body()!!
                        val lastPage = body.data.transactions.meta.lastPage.toInt()
                        val newTransactions = body.data.transactions.data
                        if (newTransactions.isEmpty() || nextPage >= lastPage) {
                            endReached = true
                        }
                        if (newTransactions.isNotEmpty()) {
                            transactionList.addAll(newTransactions)
                            transactionPage = nextPage
                        }
                        val updatedResponse = body.copy(
                            data = body.data.copy(
                                transactions = body.data.transactions.copy(data = transactionList)
                            )
                        )
                        _walletState.value = WalletState.Success(updatedResponse, transactionPage, lastPage, false)
                    } else {
                        _walletState.value = currentState.copy(isLoadingMore = false)
                    }
                } catch (e: Exception) {
                    _walletState.value = currentState.copy(isLoadingMore = false)
                } finally {
                    isLoading = false
                }
            }
        }
    }

    fun getFuelCompanies() {
        viewModelScope.launch {
            try {
                _fuelCompaniesState.value = FuelCompaniesState.Loading
                val response = repo.getFuelCompanies()
                if (response.isSuccessful) {
                    _fuelCompaniesState.value = FuelCompaniesState.Success(response.body()!!)
                } else {
                    _fuelCompaniesState.value = FuelCompaniesState.Error(ErrorHandler.fromResponse(response))
                }
            } catch (e: Exception) {
                _fuelCompaniesState.value = FuelCompaniesState.Error(ErrorHandler.getMessage(e))
            }
        }
    }


    fun resetFuelLogState() {
        _fuelLogState.value = FuelLogState.Idle
    }
    fun resetFuelRequestState() {
        _requestState.value = FuelRequestState.Idle
    }
}
