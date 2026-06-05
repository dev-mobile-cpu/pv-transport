package com.pv.transport.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.ExpenseData
import com.pv.transport.data.TypeCostResponse
import com.pv.transport.data.log.AssignedVehicleResponse
import com.pv.transport.data.log.OtherExpenseResponse
import com.pv.transport.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.adapter.rxjava2.Result.response
import javax.inject.Inject


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

    private var currentPage = 1
    private var allExpense = mutableListOf<ExpenseData>()

    fun getCostTypes() {
        _costState.value = CostState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getCostTypes()
                if (response.isSuccessful) {
                    val body = response.body() ?: TypeCostResponse(emptyList())
                    _costState.value = CostState.Success(body)
                } else {
                    _costState.value = CostState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _costState.value = CostState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun saveOtherExpense(
        date: String,
        typeOfCost: String,
        amount: String,
        licensePlate: String,
        imageUris: List<Uri>
    ) {
        _otherExpenseState.value = OtherExpenseState.Loading
        viewModelScope.launch {
            try {
                val response = repository.saveOtherExpense(date, typeOfCost, amount,licensePlate, imageUris)
                if (response.isSuccessful) {
                    val body = response.body() ?: OtherExpenseResponse("No message")
                    _otherExpenseState.value = OtherExpenseState.Success(body)
                } else {
                    _otherExpenseState.value = OtherExpenseState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _otherExpenseState.value = OtherExpenseState.Error(e.localizedMessage ?: "Unknown error")

            }
        }
    }

    fun getAllOtherExpenses(startDate: String, endDate: String) {
        _allOtherExpense.value = AllOtherExpenseState.Loading
        viewModelScope.launch {
            try {
                currentPage = 1
                allExpense.clear()
                val response = repository.getOthersExpense(startDate,endDate)
                if (response.isSuccessful) {
                    val body = response.body()
                    allExpense.addAll(body!!.data)
                    _allOtherExpense.value = AllOtherExpenseState.Success(allExpense.toList(), currentPage, body.meta.lastPage.toInt())
                    println("Other Expense retrieved successfully: $body")

                } else {
                    _allOtherExpense.value = AllOtherExpenseState.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _allOtherExpense.value = AllOtherExpenseState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadMoreExpense(start: String, end: String) {
        val currentState = _allOtherExpense.value
        if (currentState is AllOtherExpenseState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _allOtherExpense.value = currentState.copy(isLoadingMore = true)
                    currentPage++
                    val result = repository.getOthersExpense(start, end, currentPage)
                    if (result.isSuccessful) {
                        val body = result.body()!!
                        allExpense.addAll(body.data)
                        _allOtherExpense.value = AllOtherExpenseState.Success(allExpense.toList(), currentPage, body.meta.lastPage.toInt())
                    } else {
                        _allOtherExpense.value = currentState.copy(isLoadingMore = false)
                        // Optionally handle error
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
        println("Editing Other Expense with recordId: $recordId, date: $date, typeOfCost: $typeOfCost, amount: $amount, imageUris: $imageUris, deletedIds: $deletedIds")
        viewModelScope.launch {
            try {
                val response = repository.editOtherExpense(recordId,date,typeOfCost,amount,licensePlate,imageUris,deletedIds)
                println("Edit Other Expense response: ${response.code()} - ${response.message()}")
                if (response.isSuccessful) {
                    val body = response.body() ?: OtherExpenseResponse("No message")
                    _otherExpenseState.value = OtherExpenseState.Success(body)
                    println("Other Expense edited successfully: $body")
                } else {
                    _otherExpenseState.value = OtherExpenseState.Error("Failed: ${response.code()}")
                    println("Failed to edit Other Expense: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _otherExpenseState.value = OtherExpenseState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun getAssignedVehicle() {
        viewModelScope.launch {
            try {
                _assignedVehicle.value = AssignedVehicleState.Loading
                val result = repository.getAssignedVehicle()
                println("Hey Assigned Vehicle Data-----$result")
                if (result.isSuccessful) {
                    val body = result.body()
                    _assignedVehicle.value = AssignedVehicleState.Success(body!!)
                    println("Assigned vehicle retrieved successfully: $body")
                } else {
                    _assignedVehicle.value = AssignedVehicleState.Error("Failed: ${result.code()}")
                    println("Failed to retrieve assigned vehicle: ${result.code()} - ${result.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _assignedVehicle.value = AssignedVehicleState.Error(e.localizedMessage ?: "Unknown error")
                println("Error retrieving assigned vehicle: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }
}