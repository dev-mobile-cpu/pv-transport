package com.pv.transport.api

import com.pv.transport.data.AllOtherExpense
import com.pv.transport.data.TypeCostResponse
import com.pv.transport.data.log.AllDriverLogResponse
import com.pv.transport.data.log.ApproveDriverLogRequest
import com.pv.transport.data.log.ApproveDriverLogResponse
import com.pv.transport.data.log.AssignedVehicleResponse
import com.pv.transport.data.CheckVersionResponse
import com.pv.transport.data.log.CorporateUsersResponse
import com.pv.transport.data.log.DriverLogResponse
import com.pv.transport.data.log.GenerateQR
import com.pv.transport.data.log.GenerateQRResponse
import com.pv.transport.data.log.LogSheetResponse
import com.pv.transport.data.log.LoginResponse
import com.pv.transport.data.log.OtherExpenseResponse
import com.pv.transport.data.log.ReasonResponse
import com.pv.transport.data.log.TripTypeResponse
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
        @Part("site") site: RequestBody,
        @Part("purpose") purpose: RequestBody,
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
        @Part("trip_type_id") tripTypeId: RequestBody,
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
        @Part("record_id") recordId: RequestBody,
        @Part("remark") remark: RequestBody,
        @Part("end_time") startTime: RequestBody,
        @Part("end_km") startKm: RequestBody,
        @Part endPhoto: MultipartBody.Part
    ): Response<DriverLogResponse>

    @GET("driver/get_driver_logs")
    suspend fun getDriverLogList(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int
    ): Response<AllDriverLogResponse>

    @GET("driver/get_approvals")
    suspend fun getApprovals(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("status") status: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int): Response<AllDriverLogResponse>

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
        @Part("license_plate") licensePlate: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Response<OtherExpenseResponse>

    @GET("driver/get_other_expenses")
    suspend fun getOtherExpense(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int
    ): Response<AllOtherExpense>


    @Multipart
    @POST("driver/edit_other_expense")
    suspend fun editOtherExpense(
        @Part("id") id: RequestBody,
        @Part("date") date: RequestBody,
        @Part("type_of_cost_id") typeOfCostId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("license_plate") licensePlate: RequestBody,
        @Part files: List<MultipartBody.Part>?,
        @Part deleteDocs: List<MultipartBody.Part>
    ): Response<OtherExpenseResponse>

    @Multipart
    @POST("driver/approve_driver_log/{token}")
    suspend fun approveDriverLog(
        @Path("token") id: String,
        @Part("pin") password: RequestBody,
        @Part signature: MultipartBody.Part
    ): Response<ApproveDriverLogResponse>

    @GET ("driver/trip_types")
    suspend fun getTripTypes () :Response<TripTypeResponse>

    @GET ("driver/get_assigned_vehicles")
    suspend fun getAssignedVehicles () :Response<AssignedVehicleResponse>

    @GET ("driver/app-status/check-version")
    suspend fun getCheckVersion () :Response<CheckVersionResponse>

    // Offline sync variants — include uuid + client_timestamp for idempotency
    @Multipart
    @POST("driver/save_driver_log")
    suspend fun checkInDriverLogSync(
        @Part("date") date: RequestBody,
        @Part("type") type: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part("site") site: RequestBody,
        @Part("purpose") purpose: RequestBody,
        @Part("remark") remark: RequestBody,
        @Part("start_time") startTime: RequestBody,
        @Part("start_km") startKm: RequestBody,
        @Part startPhoto: MultipartBody.Part,
        @Part("uuid") uuid: RequestBody,
        @Part("client_timestamp") clientTimestamp: RequestBody
    ): Response<DriverLogResponse>

    @Multipart
    @POST("driver/save_driver_log")
    suspend fun checkInTripDriverLogSync(
        @Part("date") date: RequestBody,
        @Part("type") type: RequestBody,
        @Part("trip_type_id") tripTypeId: RequestBody,
        @Part("from") from: RequestBody,
        @Part("to") to: RequestBody,
        @Part("purpose") purpose: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part("start_time") startTime: RequestBody,
        @Part("start_km") startKm: RequestBody,
        @Part startPhoto: MultipartBody.Part,
        @Part("uuid") uuid: RequestBody,
        @Part("client_timestamp") clientTimestamp: RequestBody
    ): Response<DriverLogResponse>

    @Multipart
    @POST("driver/edit_driver_log")
    suspend fun checkOutDriverLogSync(
        @Part("record_id") recordId: RequestBody,
        @Part("remark") remark: RequestBody,
        @Part("end_time") endTime: RequestBody,
        @Part("end_km") endKm: RequestBody,
        @Part endPhoto: MultipartBody.Part,
        @Part("uuid") uuid: RequestBody,
        @Part("client_timestamp") clientTimestamp: RequestBody
    ): Response<DriverLogResponse>

    @Multipart
    @POST("driver/save_other_expense")
    suspend fun saveOtherExpenseSync(
        @Part("date") date: RequestBody,
        @Part("type_of_cost_id") typeOfCostId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("license_plate") licensePlate: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part("uuid") uuid: RequestBody,
        @Part("client_timestamp") clientTimestamp: RequestBody
    ): Response<OtherExpenseResponse>

    @Multipart
    @POST("driver/save_logsheet")
    suspend fun saveLogSheet(
        @Part("date")date:  RequestBody,
        @Part uploadPhoto: MultipartBody.Part

    ): Response<LogSheetResponse>

}
