package com.pv.transport.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.ExpenseData
import com.pv.transport.data.TypeCostResponse
import com.pv.transport.data.log.AssignedVehicleResponse
import com.pv.transport.data.log.OtherExpenseResponse
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.network.ErrorHandler
import com.pv.transport.network.NetworkUtils
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate

@HiltViewModel
class OtherExpenseViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

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
        data class Success(val response: List<ExpenseData>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false) : AllOtherExpenseState()
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
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var currentPage = 1
    private var allExpense = mutableListOf<ExpenseData>()

    // --- Persistent Form States (Option 1) ---
    var addExpenseDate = MutableStateFlow(LocalDate.now())
    var addExpenseAmount = MutableStateFlow("")
    var addExpenseType = MutableStateFlow("")
    var addExpenseTypeId = MutableStateFlow(0)
    var addExpenseVehicle = MutableStateFlow("")
    var addExpenseUriList = MutableStateFlow<List<Uri>>(emptyList())

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
                    _costState.value = CostState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _costState.value = CostState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun saveOtherExpense(
        date: String,
        typeOfCost: String,
        amount: String,
        licensePlate: String,
        imageUris: List<Uri>,
        context: Context
    ) {
        _otherExpenseState.value = OtherExpenseState.Loading
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isInternetAvailable(context)) {
                    repository.saveOtherExpenseOffline(date, typeOfCost, amount, licensePlate, imageUris)
                    _otherExpenseState.value = OtherExpenseState.SavedOffline
                    clearAddExpense()
                    return@launch
                }
                val response = repository.saveOtherExpense(date, typeOfCost, amount, licensePlate, imageUris)
                if (response.isSuccessful) {
                    _otherExpenseState.value = OtherExpenseState.Success(response.body() ?: OtherExpenseResponse("No message"))
                    clearAddExpense()
                } else {
                    _otherExpenseState.value = OtherExpenseState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _otherExpenseState.value = OtherExpenseState.Error(ErrorHandler.getMessage(e))
            }
        }
    }

    fun getAllOtherExpenses(startDate: String, endDate: String) {
        _allOtherExpense.value = AllOtherExpenseState.Loading
        viewModelScope.launch {
            try {
                currentPage = 1
                allExpense.clear()
                val response = repository.getOthersExpense(startDate, endDate)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    allExpense.addAll(body.data)
                    _allOtherExpense.value = AllOtherExpenseState.Success(allExpense.toList(), currentPage, body.meta.lastPage.toInt())
                } else {
                    _allOtherExpense.value = AllOtherExpenseState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _allOtherExpense.value = AllOtherExpenseState.Error(ErrorHandler.getMessage(e))
            }
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
                    _otherExpenseState.value = OtherExpenseState.Error("Failed: ${response.code()}")
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
                    _assignedVehicle.value = AssignedVehicleState.Error("Failed: ${result.code()}")
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


