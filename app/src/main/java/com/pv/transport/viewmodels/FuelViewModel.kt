package com.pv.transport.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pv.transport.data.fuel.FuelCompaniesResponse
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.FuelLogResponse
import com.pv.transport.data.fuel.FuelRecordResponse
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.data.fuel.FuelRequestResponse
import com.pv.transport.data.fuel.FuelTypeResponse
import com.pv.transport.data.fuel.GeneralResponse
import com.pv.transport.data.fuel.ShowFuelRequest
import com.pv.transport.data.fuel.WalletResponse
import com.pv.transport.data.log.Data
import com.pv.transport.repository.FuelRepository
import com.pv.transport.viewmodels.ApproveDriverLogViewModel.ApprovalState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FuelViewModel @Inject constructor(
    private val repo: FuelRepository
): ViewModel()
{
    sealed class FuelTypeState {
        object Idle: FuelTypeState()
        object Loading: FuelTypeState()
        data class Success(val response: FuelTypeResponse) : FuelTypeState()
        data class Error(val message: String) : FuelTypeState()
    }
    sealed class FuelRequestState {
        object Idle: FuelRequestState()
        object Loading: FuelRequestState()
        data class Success(val response: GeneralResponse): FuelRequestState()
        data class Error(val message: String): FuelRequestState()
    }

    sealed class AllFuelRequestState {
        object Idle: AllFuelRequestState()
        object Loading: AllFuelRequestState()
        data class Success(val response: List<FuelRequestData>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false) : AllFuelRequestState()
        data class Error(val message: String): AllFuelRequestState()
    }
    sealed class FuelLogState {
        object Idle: FuelLogState()
        object Loading: FuelLogState()
        data class Success(val response: GeneralResponse): FuelLogState()
        data class Error(val message: String): FuelLogState()

    }
    sealed class AllFuelLogState {
        object Idle: AllFuelLogState()
        object Loading: AllFuelLogState()
        data class Success(val response: List<FuelLogData>, val currentPage: Int, val lastPage: Int, val isLoadingMore: Boolean = false) : AllFuelLogState()
        data class Error(val message: String): AllFuelLogState()

    }
    sealed class WalletState {
        object Idle: WalletState()
        object Loading: WalletState()
        data class Success(val response: WalletResponse): WalletState()
        data class Error(val message: String): WalletState()

    }
    sealed class FuelCompaniesState {
        object Idle: FuelCompaniesState()
        object Loading: FuelCompaniesState()
        data class Success(val response: FuelCompaniesResponse): FuelCompaniesState()
        data class Error(val message: String): FuelCompaniesState()

    }

    private val _state = MutableStateFlow<FuelTypeState>(FuelTypeState.Idle)
    val state: StateFlow<FuelTypeState> = _state

    private val _allRequestState = MutableStateFlow<AllFuelRequestState>(AllFuelRequestState.Idle)
    val  allRequestState: StateFlow<AllFuelRequestState> = _allRequestState

    private val _requestState = MutableStateFlow<FuelRequestState>(FuelRequestState.Idle)
    val  requestState: StateFlow<FuelRequestState> = _requestState

    private val _fuelLogState = MutableStateFlow<FuelLogState>(FuelLogState.Idle)
    val  fuelLogState: StateFlow<FuelLogState> = _fuelLogState

    private val _allFuelLogState = MutableStateFlow<AllFuelLogState>(AllFuelLogState.Idle)
    val  allFuelLogState: StateFlow<AllFuelLogState> = _allFuelLogState

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Idle)
    val walletState: StateFlow<WalletState> = _walletState

    private val _fuelCompaniesState = MutableStateFlow<FuelCompaniesState>(FuelCompaniesState.Idle)
    val fuelCompaniesState: StateFlow<FuelCompaniesState> = _fuelCompaniesState

    private var currentPage = 1
    private var allFuelRequest = mutableListOf<FuelRequestData>()
    private var allFuelLog = mutableListOf<FuelLogData>()

    fun getFuelType(){
        viewModelScope.launch {
            try {
                _state.value = FuelTypeState.Loading
                val result = repo.getFuelTypes()
                if (result.isSuccessful){
                    val responseBody = result.body()
                    _state.value = FuelTypeState.Success(responseBody!!)
                }else{
                    _state.value = FuelTypeState.Error("Empty response body")
                }

            }catch (e : Exception){
                _state.value = FuelTypeState.Error("Error: ${e.localizedMessage}")

            }
        }
    }

    fun saveFundRequest(fuelRequest: FuelRequest){
        viewModelScope.launch {
            try {
                _requestState.value = FuelRequestState.Loading
                val response = repo.saveFundRequest(fuelRequest)
                Log.e("Hey fund request",response.body().toString())
                if (response.isSuccessful){
                    val responseBody = response.body()
                    _requestState.value = FuelRequestState.Success(responseBody!!)
                    Log.e("Fuel request : ",responseBody.toString())
                }else{
                    _requestState.value = FuelRequestState.Error("Empty response body")
                    println("Failed to retrieve approval : ${response.code()} - ${response.message()}")
                }

            }catch (e: Exception){
                _requestState.value = FuelRequestState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun getFuelRequest(startDate: String, endDate: String){
        viewModelScope.launch {
            try {
                _allRequestState.value = AllFuelRequestState.Loading
                currentPage = 1
                allFuelRequest.clear()
                val response = repo.getFuelRequest(startDate,endDate)
                Log.e("Hey get fund request", response.body()!!.data.toString())
                if (response.isSuccessful){
                    val body = response.body()!!
                    allFuelRequest.addAll(body.data)
                    _allRequestState.value = AllFuelRequestState.Success(allFuelRequest.toList(),currentPage,body.meta.lastPage.toInt())
                }else{
                    _allRequestState.value = AllFuelRequestState.Error("Empty response body")
                    println("Failed to retrieve approval : ${response.code()} - ${response.message()}")
                }

            }catch (e: Exception){
                _allRequestState.value = AllFuelRequestState.Error("Error : ${e.localizedMessage}")
                println("Exception occurred while retrieving approval: ${e.localizedMessage}")
            }
        }
    }

    fun loadMoreRequest(startDate: String, endDate: String) {
        val currentState = _allRequestState.value
        if (currentState is AllFuelRequestState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _allRequestState.value = currentState.copy(isLoadingMore = true)
                    currentPage++
                    val response = repo.getFuelRequest(startDate, endDate, currentPage)
                    if (response.isSuccessful) {
                        val body = response.body()!!
                        allFuelRequest.addAll(body.data)
                        _allRequestState.value = AllFuelRequestState.Success(allFuelRequest.toList(), currentPage, body.meta.lastPage.toInt())
                    } else {
                        _allRequestState.value = currentState.copy(isLoadingMore = false)
                        println("Failed to retrieve approval : ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _allRequestState.value = AllFuelRequestState.Error(e.localizedMessage ?: "Unknown error")
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
        walletBucket: String
    ){
        viewModelScope.launch {
            try {
                _fuelLogState.value = FuelLogState.Loading
                Log.e("Hey save fuel log", "Saving fuel log with carPlateNo: $carPlateNo, date: $date, fuelShop: $fuelShop, fuelTypeId: $fuelTypeId, fuelAmount: $fuelAmount, fuelLiter: $fuelLiter, currentKm: $currentKm, walletBucket: $walletBucket, files count: ${files.size}, currentKmPhoto: $currentKmPhoto")
                val response = repo.saveFuelLog(carPlateNo, date,fuelCompanyId, fuelShop, fuelTypeId, fuelAmount, fuelLiter, files, currentKm, currentKmPhoto,walletBucket)
                Log.e("Hey save fuel log", response.body().toString())
                if (response.isSuccessful){
                    val responseBody = response.body()
                    _fuelLogState.value = FuelLogState.Success(responseBody!!)
                }else{
                    _fuelLogState.value = FuelLogState.Error("Empty response body")
                    println("Failed to save fuel log : ${response.code()} - ${response.message()}")
                }

            }catch (e: Exception){
                _fuelLogState.value = FuelLogState.Error("Error: ${e.localizedMessage}")
                println("Exception occurred while saving fuel log: ${e.localizedMessage}")
            }
        }
    }


    fun getFuelLog(startDate: String, endDate: String){
        viewModelScope.launch {
            try {
                _allFuelLogState.value = AllFuelLogState.Loading
                currentPage = 1
                allFuelLog.clear()
                val response = repo.getFuelLogs(startDate, endDate)
                 Log.e("Hey get fuel log", response.body()!!.data.toString())
                if (response.isSuccessful){
                    val body = response.body()
                    allFuelLog.addAll(body!!.data)
                    _allFuelLogState.value = AllFuelLogState.Success(allFuelLog.toList(),currentPage,body.meta.lastPage.toInt())
                }else{
                    _fuelLogState.value = FuelLogState.Error("Empty response body")
                }

            }catch (e: Exception){
                _fuelLogState.value = FuelLogState.Error("Error : ${e.localizedMessage}")

            }
        }
    }

    fun loadMoreFuelLog(startDate: String, endDate: String) {
        val currentState = _allFuelLogState.value
        if (currentState is AllFuelLogState.Success && !currentState.isLoadingMore && currentState.currentPage < currentState.lastPage) {
            viewModelScope.launch {
                try {
                    _allFuelLogState.value = currentState.copy(isLoadingMore = true)
                    currentPage++
                    val response = repo.getFuelLogs(startDate, endDate, currentPage)
                    if (response.isSuccessful) {
                        val body = response.body()!!
                        allFuelLog.addAll(body.data)
                        _allFuelLogState.value = AllFuelLogState.Success(allFuelLog.toList(), currentPage, body.meta.lastPage.toInt())
                    } else {
                        _allFuelLogState.value = currentState.copy(isLoadingMore = false)
                        println("Failed to retrieve approval : ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _allRequestState.value = AllFuelRequestState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }

  fun getWalletBalance(){
      viewModelScope.launch {
          try {
              _walletState.value = WalletState.Loading
              val response = repo.getWalletBalance(10)
              Log.e("Hey get wallet balance", response.body().toString())
              if (response.isSuccessful){
                  val responseBody = response.body()
                  _walletState.value = WalletState.Success(responseBody!!)
              }else{
                  _walletState.value = WalletState.Error("Empty response body")
                  println("Failed to retrieve wallet balance : ${response.code()} - ${response.message()}")
              }

          }catch (e: Exception){
              _walletState.value = WalletState.Error("Error: ${e.localizedMessage}")
              println("Exception occurred while retrieving wallet balance: ${e.localizedMessage}")
          }
      }
  }
    fun getFuelCompanies(){
        viewModelScope.launch {
            try {
                _fuelCompaniesState.value = FuelCompaniesState.Loading
                val response = repo.getFuelCompanies()
                Log.e("Hey get fuel companies", response.body().toString())
                if (response.isSuccessful){
                    val responseBody = response.body()
                    _fuelCompaniesState.value = FuelCompaniesState.Success(responseBody!!)
                }else{
                    _fuelCompaniesState.value = FuelCompaniesState.Error("Empty response body")
                    println("Failed to retrieve fuel companies : ${response.code()} - ${response.message()}")
                }

            }catch (e: Exception){
                _fuelCompaniesState.value = FuelCompaniesState.Error("Error: ${e.localizedMessage}")
                println("Exception occurred while retrieving fuel companies: ${e.localizedMessage}")
            }
        }
    }
}