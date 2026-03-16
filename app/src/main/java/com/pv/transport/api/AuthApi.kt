package com.pv.transport.api

import com.pv.transport.data.AllDriverLogResponse
import com.pv.transport.data.AllOtherExpense
import com.pv.transport.data.ApproveDriverLogRequest
import com.pv.transport.data.ApproveDriverLogResponse
import com.pv.transport.data.CorporateUsersResponse
import com.pv.transport.data.DriverLogResponse
import com.pv.transport.data.GenerateQR
import com.pv.transport.data.GenerateQRResponse
import com.pv.transport.data.LoginResponse
import com.pv.transport.data.OtherExpense
import com.pv.transport.data.OtherExpenseResponse
import com.pv.transport.data.ReasonResponse
import com.pv.transport.data.TypeCostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("driver/login")
    suspend fun login(@Query("login_id")loginId: String, @Query("password")password: String): Response<LoginResponse>

    @GET ("driver/reasons")
    suspend fun getReasons () :Response<ReasonResponse>

    @Multipart
    @POST("driver/save_driver_log")
    suspend fun checkInDriverLog(
        @Part("date") date: RequestBody,
        @Part("type") type: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part("remark") remark: RequestBody,
        @Part("start_time") startTime: RequestBody,
        @Part("start_km") startKm: RequestBody,
        @Part startPhoto: MultipartBody.Part
    ): Response<DriverLogResponse>

    @Multipart
    @POST("driver/save_driver_log")
    suspend fun checkInTripDriverLog(
        @Part("date") date: RequestBody,
        @Part("type") type: RequestBody,
        @Part("trip_type") tripType: RequestBody,
        @Part("from") from: RequestBody,
        @Part("to") to: RequestBody,
        @Part("purpose") purpose: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part("start_time") startTime: RequestBody,
        @Part("start_km") startKm: RequestBody,
        @Part startPhoto: MultipartBody.Part
    ): Response<DriverLogResponse>

    @Multipart
    @POST("driver/edit_driver_log")
    suspend fun checkOutDriverLog(
        @Part("record_id") remark: RequestBody,
        @Part("end_time") startTime: RequestBody,
        @Part("end_km") startKm: RequestBody,
        @Part endPhoto: MultipartBody.Part
    ): Response<DriverLogResponse>

    @GET("driver/get_driver_logs")
    suspend fun getDriverLogList(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<AllDriverLogResponse>

    @GET("driver/get_approvals")
    suspend fun getApprovals(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("status") status: String): Response<AllDriverLogResponse>

    @GET("driver/corporate_users")
    suspend fun getCorporateUsers(): Response<List<CorporateUsersResponse>>

    @POST("driver/generate_qr")
    suspend fun getGenerateQR(@Body generateQR: GenerateQR): Response<GenerateQRResponse>

    @GET("driver/get_type_of_costs")
    suspend fun getTypeCost(): Response<TypeCostResponse>

    @Multipart
    @POST("driver/save_other_expense")
    suspend fun saveOtherExpense(
        @Part("date") date: RequestBody,
        @Part("type_of_cost_id") typeOfCostId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Response<OtherExpenseResponse>

    @GET("driver/get_other_expenses")
    suspend fun getOtherExpense(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<AllOtherExpense>

    @POST("driver/edit_other_expense")
    suspend fun editOtherExpense(@Body otherExpense: OtherExpense): Response<OtherExpenseResponse>

    @POST("driver/approve_driver_log/{token}")
    suspend fun approveDriverLog(
        @Path("token") id: String,
        @Body request: ApproveDriverLogRequest
    ): Response<ApproveDriverLogResponse>


}