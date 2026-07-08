package com.pv.transport.api

import com.pv.transport.data.fuel.FuelCompaniesResponse
import com.pv.transport.data.fuel.FuelLogResponse
import com.pv.transport.data.fuel.FuelRecordResponse
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelRequestResponse
import com.pv.transport.data.fuel.FuelTypeResponse
import com.pv.transport.data.fuel.GeneralResponse
import com.pv.transport.data.fuel.WalletResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FuelApi {

    @GET("driver/get_fuel_types")
    suspend fun getFuelTypes():Response<FuelTypeResponse>

    @Multipart
    @POST("fuel/save_fuel_request")
    suspend fun saveFundRequest(
        @Part("request_category") requestCategory: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("fuel_type_id") fuelTypeId: RequestBody?,
        @Part("remark") remark: RequestBody?,
        @Part("request_type") requestType: RequestBody?,
        @Part files: List<MultipartBody.Part>?
    ): Response<GeneralResponse>

    @GET("fuel/get_fuel_requests")
    suspend fun getFuelRequestLogs(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int
    ):Response<FuelRequestResponse>

    @Multipart
    @POST("driver/save_fuel_log")
    suspend fun saveFuelLog(
        @Part("car_plate_no") carPlateNo: RequestBody,
        @Part("date") date: RequestBody,
        @Part("fuel_company_id") fuelCompanyId: RequestBody,
        @Part("fuel_shop") fuelShop: RequestBody,
        @Part("fuel_type_id") fuelTypeId: RequestBody,
        @Part("fuel_amount") fuelAmount: RequestBody,
        @Part("fuel_liter") fuelLiter: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part("current_km") currentKm: RequestBody,
        @Part currentKmPhoto: MultipartBody.Part,
        @Part("wallet_bucket") walletBucket: RequestBody
    ): Response<GeneralResponse>


    @GET("driver/get_fuel_logs")
    suspend fun getFuelLogs(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int
    ):Response<FuelLogResponse>


    @Multipart
    @POST("fuel/approve_fuel_log")
    suspend fun approveFuelLog(
        @Part("car_plate_no") carPlateNo: RequestBody,
        @Part("date") date: RequestBody,
        @Part("fuel_shop") fuelShop: RequestBody,
        @Part("fuel_type_id") fuelTypeId: RequestBody,
        @Part("fuel_amount") fuelAmount: RequestBody,
        @Part("fuel_liter") fuelLiter: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part("current_km") currentKm: RequestBody,
        @Part currentKmPhoto: MultipartBody.Part
    ): Response<GeneralResponse>

    @GET("driver/wallet")
    suspend fun getWallet(
        @Query("transactions_per_page") page: Int? = null,
    ):Response<WalletResponse>

    @GET("driver/wallet")
    suspend fun getWalletTransactions(@Query("page") page: Int): Response<WalletResponse>

    @GET("fuel/get_fuel_companies")
    suspend fun getFuelCompanies():Response<FuelCompaniesResponse>

    // Offline sync variant
    @Multipart
    @POST("driver/save_fuel_log")
    suspend fun saveFuelLogSync(
        @Part("car_plate_no") carPlateNo: RequestBody,
        @Part("date") date: RequestBody,
        @Part("fuel_company_id") fuelCompanyId: RequestBody,
        @Part("fuel_shop") fuelShop: RequestBody,
        @Part("fuel_type_id") fuelTypeId: RequestBody,
        @Part("fuel_amount") fuelAmount: RequestBody,
        @Part("fuel_liter") fuelLiter: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part("current_km") currentKm: RequestBody,
        @Part currentKmPhoto: MultipartBody.Part,
        @Part("wallet_bucket") walletBucket: RequestBody,
        @Part("uuid") uuid: RequestBody,
        @Part("client_timestamp") clientTimestamp: RequestBody
    ): Response<GeneralResponse>
}
