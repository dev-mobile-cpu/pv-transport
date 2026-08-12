package com.pv.transport.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.fuel.FuelCompaniesResponse
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.data.fuel.FuelTypeResponse
import com.pv.transport.data.fuel.GeneralResponse
import com.pv.transport.data.fuel.Transaction
import com.pv.transport.data.fuel.WalletResponse
import com.pv.transport.local.data.OfflineFuelLogEntity
import com.pv.transport.local.data.toFuelLogData
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.network.ErrorHandler
import com.pv.transport.network.NetworkUtils
import com.pv.transport.repository.FuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate

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
            repo.observeCachedFuelLogs()
        ) { currentState, pending, cache ->

            val today = LocalDate.now().toString()
            val pendingLogs = pending.map { it.toFuelLogData() }

            val cacheLogs = cache?.logs
                ?.filter { it.date.startsWith(today) }
                ?: emptyList()

            if (currentState is AllFuelLogState.Loading ||
                currentState is AllFuelLogState.Error
            ) {
                if (cache != null) {
                    val merged = (cacheLogs + pendingLogs).distinctBy { it.uuid ?: it.id }

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
                val merged = (currentState.response + pendingLogs)
                    .distinctBy { it.uuid ?: it.id }
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
                    _state.value = FuelTypeState.Error(result.message())
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
                    lastFuelRequestStart = null
                    lastFuelRequestEnd = null

                } else {
                    // Parse real error message from server
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val json = com.google.gson.JsonParser.parseString(errorBody)
                        json.asJsonObject.get("message").asString
                    } catch (e: Exception) {
                        errorBody ?: response.message()
                    }
                    _requestState.value = FuelRequestState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _requestState.value = FuelRequestState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getFuelRequest(startDate: String, endDate: String, force: Boolean = false) {
        if (!force &&
            lastFuelRequestStart == startDate &&
            lastFuelRequestEnd == endDate &&
            _allRequestState.value is AllFuelRequestState.Success
        ) {
            return
        }
        viewModelScope.launch {
            try {
                lastFuelRequestStart = startDate
                lastFuelRequestEnd = endDate
                // Keep existing list visible while refreshing (avoid flash/empty on detail back)
                val keepList = _allRequestState.value is AllFuelRequestState.Success
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
                    _allRequestState.value = AllFuelRequestState.Error(response.message())
                }
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
                _fuelLogState.value = FuelLogState.SavedOffline
                clearAddFuelLog()
                lastFuelLogStart = null
                lastFuelLogEnd = null
            } catch (e: Exception) {
                _fuelLogState.value = FuelLogState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getFuelLog(startDate: String, endDate: String, force: Boolean = false) {
        if (!force &&
            lastFuelLogStart == startDate &&
            lastFuelLogEnd == endDate &&
            _allFuelLogState.value is AllFuelLogState.Success
        ) {
            return
        }
        viewModelScope.launch {
            try {
                lastFuelLogStart = startDate
                lastFuelLogEnd = endDate
                // Keep existing list visible while refreshing (avoid flash/empty on detail back)
                val keepList = _allFuelLogState.value is AllFuelLogState.Success
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
                    _allFuelLogState.value = AllFuelLogState.Error(response.message())
                }
            } catch (e: Exception) {
                if (_allFuelLogState.value !is AllFuelLogState.Success) {
                    _allFuelLogState.value = AllFuelLogState.Error(ErrorHandler.getMessage(e))
                }
            }
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
                    _walletState.value = WalletState.Error(response.message())
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
                    _fuelCompaniesState.value = FuelCompaniesState.Error(response.message())
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
