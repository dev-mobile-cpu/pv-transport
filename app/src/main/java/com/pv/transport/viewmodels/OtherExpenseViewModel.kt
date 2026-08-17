package com.pv.transport.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.SessionEvents
import com.pv.transport.data.AllOtherExpense
import com.pv.transport.data.ExpenseData
import com.pv.transport.data.TypeCostResponse
import com.pv.transport.data.log.AssignedVehicleResponse
import com.pv.transport.data.log.OtherExpenseResponse
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.local.data.toExpenseData
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.network.ErrorHandler
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
class OtherExpenseViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            connectivityObserver.getCurrentStatus()
        )

    sealed class CostState {
        object Idle : CostState()
        object Loading : CostState()
        data class Success(val cost: TypeCostResponse) : CostState()
        data class Error(val message: String) : CostState()
    }

    sealed class OtherExpenseState {
        object Idle : OtherExpenseState()
        object Loading : OtherExpenseState()
        object SavedOffline : OtherExpenseState()
        data class Success(val message: OtherExpenseResponse) : OtherExpenseState()
        data class Error(val message: String) : OtherExpenseState()
    }

    sealed class AllOtherExpenseState {
        object Idle : AllOtherExpenseState()
        object Loading : AllOtherExpenseState()
        data class Success(val response: List<ExpenseData>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false,  val isOffline: Boolean = false) : AllOtherExpenseState()
        data class Error(val message: String) : AllOtherExpenseState()
    }

    sealed class AssignedVehicleState {
        object Idle : AssignedVehicleState()
        object Loading : AssignedVehicleState()
        data class Success(val response: AssignedVehicleResponse) : AssignedVehicleState()
        data class Error(val message: String) : AssignedVehicleState()
    }

    private val _costState = MutableStateFlow<CostState>(CostState.Idle)
    val costState: StateFlow<CostState> = _costState

    private val _otherExpenseState = MutableStateFlow<OtherExpenseState>(OtherExpenseState.Idle)
    val otherExpenseState: StateFlow<OtherExpenseState> = _otherExpenseState

    private val _allOtherExpense = MutableStateFlow<AllOtherExpenseState>(AllOtherExpenseState.Loading)
    val allOtherExpense: StateFlow<AllOtherExpenseState> = _allOtherExpense

    private val _assignedVehicle = MutableStateFlow<AssignedVehicleState>(AssignedVehicleState.Idle)
    val assignedVehicle: StateFlow<AssignedVehicleState> = _assignedVehicle

    val pendingExpenses: StateFlow<List<OfflineOtherExpenseEntity>> =
        repository.observePendingExpenses()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var currentPage = 1
    private var allExpense = mutableListOf<ExpenseData>()
    private var lastExpenseStart: String? = null
    private var lastExpenseEnd: String? = null
    // Set after a local save so the next list visit refetches instead of reusing the cached page
    private var forceNextExpenseRefresh = false
    private var expenseJob: Job? = null

    // --- Persistent Form States (Option 1) ---
    var addExpenseDate = MutableStateFlow(LocalDate.now())
    var addExpenseAmount = MutableStateFlow("")
    var addExpenseType = MutableStateFlow("")
    var addExpenseTypeId = MutableStateFlow(0)
    var addExpenseVehicle = MutableStateFlow("")
    var addExpenseUriList = MutableStateFlow<List<Uri>>(emptyList())

    val pendingOtherExpenseLogs: StateFlow<List<OfflineOtherExpenseEntity>> =
        repository.observePendingExpenses()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val recentlySyncedExpenses: StateFlow<List<OfflineOtherExpenseEntity>> =
        repository.observeRecentlySyncedExpenses()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unifiedOtherExpenseLogs: StateFlow<AllOtherExpenseState> =
        combine(
            _allOtherExpense,
            pendingOtherExpenseLogs,
            recentlySyncedExpenses,
            repository.observeCachedOtherExpenseLogs()
        ) { state, pending, recentlySynced, cache ->

            val uuidByServerId = (pending + recentlySynced).mapNotNull { entity ->
                entity.serverRecordId?.takeIf { it.isNotBlank() }?.let { it to entity.uuid }
            }.toMap()
            val clientTimestampByUuid = (pending.map { it.uuid to it.clientTimestamp } +
                    recentlySynced.map { it.uuid to it.clientTimestamp }).toMap()

            val cacheLogs = cache?.logs
                ?.map { it.withStableExpenseUuid(uuidByServerId) }
                ?.filter { isInSelectedExpenseRange(it.date) }
                ?: emptyList()
            if (state is AllOtherExpenseState.Loading ||
                state is AllOtherExpenseState.Error
            ){
                val offlineLogs = visibleLocalExpenses(pending, recentlySynced, cacheLogs)
                if (cacheLogs.isNotEmpty() || offlineLogs.isNotEmpty()) {
                    val mergedLogs = mergeExpenses(cacheLogs + offlineLogs, clientTimestampByUuid)
                    AllOtherExpenseState.Success(
                        response = mergedLogs,
                        currentPage = 1,
                        lastPage = 1,
                        isLoadingMore = false
                    )
                } else {
                    state
                }
            } else if (state is AllOtherExpenseState.Success) {
                val serverLogs = state.response
                    .map { it.withStableExpenseUuid(uuidByServerId) }
                val offlineLogs = visibleLocalExpenses(pending, recentlySynced, serverLogs)
                val merged = mergeExpenses(serverLogs + offlineLogs, clientTimestampByUuid)
                state.copy(
                    response = merged
                )
            } else {
                state
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AllOtherExpenseState.Loading
        )

    /** Cached and offline rows are only meaningful for the date range the list is showing. */
    private fun isInSelectedExpenseRange(date: String): Boolean {
        val today = LocalDate.now().toString()
        val start = lastExpenseStart ?: today
        val end = lastExpenseEnd ?: today
        return date.take(10) in start..end
    }

    private fun visibleLocalExpenses(
        pending: List<OfflineOtherExpenseEntity>,
        recentlySynced: List<OfflineOtherExpenseEntity>,
        alreadyShown: List<ExpenseData>
    ): List<ExpenseData> {
        val pendingUuids = pending.map { it.uuid }.toSet()
        val extraSynced = recentlySynced.filter { entity ->
            entity.uuid !in pendingUuids && !expenseCoveredByServer(entity, alreadyShown)
        }
        return (pending + extraSynced)
            .map { it.toExpenseData() }
            .filter { isInSelectedExpenseRange(it.date) }
    }

    private fun expenseCoveredByServer(
        entity: OfflineOtherExpenseEntity,
        logs: List<ExpenseData>
    ): Boolean {
        val serverId = entity.serverRecordId?.takeIf { it.isNotBlank() }
        if (serverId != null && logs.any { it.id == serverId }) return true
        return logs.any { it.uuid == entity.uuid || it.id == entity.uuid }
    }

    private fun ExpenseData.withStableExpenseUuid(uuidByServerId: Map<String, String>): ExpenseData {
        val mappedUuid = uuidByServerId[id] ?: return this
        return if (uuid == mappedUuid) this else copy(uuid = mappedUuid)
    }

    private fun mergeExpenses(
        expenses: List<ExpenseData>,
        clientTimestampByUuid: Map<String, Long>
    ): List<ExpenseData> {
        return expenses
            .distinctBy { it.uuid ?: it.id }
            .sortedWith(
                compareByDescending<ExpenseData> { it.date.take(10) }
                    .thenByDescending { expense ->
                        expense.uuid?.let { clientTimestampByUuid[it] }
                            ?: parseServerTimestamp(expense.createdAt)
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
                expenseJob?.cancel()
                currentPage = 1
                allExpense.clear()
                forceNextExpenseRefresh = true
                _allOtherExpense.value = AllOtherExpenseState.Loading
            }
        }

        // Retry pending uploads as soon as the network is back, then show the server state
        viewModelScope.launch {
            networkStatus.collectLatest { status ->
                if (status == ConnectivityObserver.Status.Available) {
                    runCatching { repository.scheduleSyncIfPending() }
                    refreshExpensesSilent()
                }
            }
        }

        // Pending rows disappear once they are uploaded, so refetch to show the synced records
        viewModelScope.launch {
            var previousPendingCount = Int.MAX_VALUE
            pendingOtherExpenseLogs.collectLatest { pending ->
                if (pending.size < previousPendingCount &&
                    previousPendingCount != Int.MAX_VALUE &&
                    networkStatus.value == ConnectivityObserver.Status.Available
                ) {
                    refreshExpensesSilent()
                    delay(2000)
                    if (networkStatus.value == ConnectivityObserver.Status.Available) {
                        refreshExpensesSilent()
                    }
                }
                previousPendingCount = pending.size
            }
        }
    }

    fun clearAddExpense() {
        addExpenseDate.value = LocalDate.now()
        addExpenseAmount.value = ""
        addExpenseUriList.value = emptyList()
        // type and vehicle reset logic usually handled in UI init if empty
    }

    fun getCostTypes() {
        _costState.value = CostState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getCostTypes()
                if (response.isSuccessful) {
                    _costState.value = CostState.Success(response.body() ?: TypeCostResponse(emptyList()))
                } else {
                    _costState.value = CostState.Error(ErrorHandler.fromResponse(response))
                }
            } catch (e: Exception) {
                _costState.value = CostState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun saveOtherExpense(
        date: String,
        typeOfCostId: String,
        typeOfCostOffline: String,
        amount: String,
        licensePlate: String,
        imageUris: List<Uri>,
        context: Context
    ) {
        _otherExpenseState.value = OtherExpenseState.Loading
        viewModelScope.launch {
            try {
                // Local-first: always persist, then auto-sync when network is good
                repository.saveOtherExpenseOffline(date,typeOfCostId, typeOfCostOffline, amount, licensePlate, imageUris)
                forceNextExpenseRefresh = true
                _otherExpenseState.value = OtherExpenseState.SavedOffline
                clearAddExpense()
            } catch (e: Exception) {
                _otherExpenseState.value = OtherExpenseState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getAllOtherExpenses(startDate: String, endDate: String, force: Boolean = false) {
        val sameRange = lastExpenseStart == startDate && lastExpenseEnd == endDate
        if (!force &&
            !forceNextExpenseRefresh &&
            sameRange &&
            _allOtherExpense.value is AllOtherExpenseState.Success
        ) {
            return
        }
        forceNextExpenseRefresh = false
        // Keep the current page visible while refreshing the same range, but never show
        // another range's rows while a new range is loading.
        val keepList = sameRange && _allOtherExpense.value is AllOtherExpenseState.Success
        if (!keepList) {
            _allOtherExpense.value = AllOtherExpenseState.Loading
        }
        expenseJob?.cancel()
        expenseJob = viewModelScope.launch {
            try {
                lastExpenseStart = startDate
                lastExpenseEnd = endDate
                // Visiting/refreshing the list re-attempts any pending offline records
                runCatching { repository.scheduleSyncIfPending() }
                currentPage = 1
                val response = repository.getOthersExpense(startDate, endDate)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    allExpense.clear()
                    allExpense.addAll(body.data)
                    _allOtherExpense.value = AllOtherExpenseState.Success(
                        allExpense.toList(),
                        currentPage,
                        body.meta.lastPage.toInt()
                    )
                } else if (!keepList) {
                    _allOtherExpense.value = AllOtherExpenseState.Error(ErrorHandler.fromResponse(response))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_allOtherExpense.value !is AllOtherExpenseState.Success) {
                    _allOtherExpense.value = AllOtherExpenseState.Error(ErrorHandler.getMessage(e))
                }
            }
        }
    }

    /** Refresh the visible range without touching Loading/Error state, e.g. right after a sync. */
    private suspend fun refreshExpensesSilent() {
        val start = lastExpenseStart ?: return
        val end = lastExpenseEnd ?: return
        try {
            val response = repository.getOthersExpense(start, end)
            if (response.isSuccessful) {
                val body = response.body()!!
                currentPage = 1
                allExpense.clear()
                allExpense.addAll(body.data)
                _allOtherExpense.value = AllOtherExpenseState.Success(
                    allExpense.toList(),
                    currentPage,
                    body.meta.lastPage.toInt()
                )
            }
        } catch (_: Exception) {
            // Keep current list; pending local rows still show via the unified flow
        }
    }

    fun loadMoreExpense(start: String, end: String) {
        val currentState = _allOtherExpense.value
        if (currentState is AllOtherExpenseState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _allOtherExpense.value = currentState.copy(isLoadingMore = true)
                    val nextPage = currentPage + 1
                    val result = repository.getOthersExpense(start, end, nextPage)
                    if (result.isSuccessful) {
                        val body = result.body()!!
                        currentPage = nextPage
                        allExpense.addAll(body.data)
                        _allOtherExpense.value = AllOtherExpenseState.Success(allExpense.toList(), currentPage, body.meta.lastPage.toInt())
                    } else {
                        _allOtherExpense.value = currentState.copy(isLoadingMore = false)
                    }
                } catch (e: Exception) {
                    _allOtherExpense.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun editOtherExpense(
        recordId: String,
        date: String,
        typeOfCost: String,
        amount: String,
        licensePlate: String,
        imageUris: List<Uri>,
        deletedIds: List<String>
    ) {
        _otherExpenseState.value = OtherExpenseState.Loading
        viewModelScope.launch {
            try {
                val response = repository.editOtherExpense(recordId, date, typeOfCost, amount, licensePlate, imageUris, deletedIds)
                if (response.isSuccessful) {
                    _otherExpenseState.value = OtherExpenseState.Success(response.body() ?: OtherExpenseResponse("No message"))
                } else {
                    _otherExpenseState.value = OtherExpenseState.Error(ErrorHandler.fromResponse(response))
                }
            } catch (e: Exception) {
                _otherExpenseState.value = OtherExpenseState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getAssignedVehicle() {
        viewModelScope.launch {
            try {
                _assignedVehicle.value = AssignedVehicleState.Loading
                val result = repository.getAssignedVehicle()
                if (result.isSuccessful) {
                    _assignedVehicle.value = AssignedVehicleState.Success(result.body()!!)
                } else {
                    _assignedVehicle.value = AssignedVehicleState.Error(ErrorHandler.fromResponse(result))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _assignedVehicle.value = AssignedVehicleState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun resetOtherExpenseState() {
        _otherExpenseState.value = OtherExpenseState.Idle
    }
}


