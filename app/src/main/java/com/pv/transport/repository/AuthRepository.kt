package com.pv.transport.repository

import android.content.Context
import android.net.Uri
import com.pv.transport.api.AuthApi
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
import com.pv.transport.extension.createMultipart
import com.pv.transport.extension.createMultipartList
import com.pv.transport.extension.createMultipleMultipart
import com.pv.transport.extension.toRequestBody
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(private val api: AuthApi, @ApplicationContext private val  context: Context) {
    suspend fun login(username: String, password: String): Response<LoginResponse> {
        return api.login(username, password)
    }
    suspend fun getReason(): Response<ReasonResponse> {
        return api.getReasons()
    }
    suspend fun checkInDriverLog(
        date: String,
        type: String,
        reason: String,
        remark: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri
    ): Response<DriverLogResponse> {
        return api.checkInDriverLog(
            date = toRequestBody(date),
            type = toRequestBody(type),
            reason = toRequestBody(reason),
            remark = toRequestBody(remark),
            startTime = toRequestBody(startTime),
            startKm = toRequestBody(startKm),
            createMultipart(startPhoto, "start_photo", context)
        )
    }

    suspend fun checkInTripDriverLog(
        date: String,
        type: String,
        tripType: String,
        from: String,
        to: String,
        purpose: String,
        reason: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri
    ): Response<DriverLogResponse> {

        return api.checkInTripDriverLog(
            date = toRequestBody(date),
            type = toRequestBody(type),
            tripType = toRequestBody(tripType),
            from = toRequestBody(from),
            to = toRequestBody(to),
            purpose = toRequestBody(purpose),
            reason = toRequestBody(reason),
            startTime = toRequestBody(startTime),
            startKm = toRequestBody(startKm),
            createMultipart(startPhoto, "start_photo", context)
        )
    }

    suspend fun checkOutDriverLog(
        recordId: String,
        endTime: String,
        endKm: String,
        endPhoto: Uri
    ): Response<DriverLogResponse> {
        return api.checkOutDriverLog(
            remark = toRequestBody(recordId),
            startTime = toRequestBody(endTime),
            startKm = toRequestBody(endKm),
            createMultipart(endPhoto, "end_photo", context)
        )
    }


    suspend fun getDriverLogs(startDate: String, endDate: String): Response<AllDriverLogResponse> {
        return api.getDriverLogList(startDate, endDate)
    }

   suspend fun getApprovalStatus(startDate: String, endDate: String,status: String): Response<AllDriverLogResponse> {
        return api.getApprovals(startDate,endDate,status)
    }

    suspend fun getCorporateUsers(): Response<List<CorporateUsersResponse>> {
        return api.getCorporateUsers()
    }

    suspend fun getGenerateQR(generateQR: GenerateQR): Response<GenerateQRResponse>{
        return api.getGenerateQR(generateQR)
    }

    suspend fun getCostTypes(): Response<TypeCostResponse> {
        return api.getTypeCost()
    }

    suspend fun saveOtherExpense(
        date: String,
        typeCost: String,
        amount: String,
        photo: List<Uri>
    ): Response<OtherExpenseResponse> {
        val parts = createMultipartList(photo, "files[]", context)

        return api.saveOtherExpense(
            date = toRequestBody(date),
            typeOfCostId = toRequestBody(typeCost),
            amount = toRequestBody(amount),
            files = parts
        )
    }

    suspend fun getOthersExpense(
        startDate: String, endDate: String
    ): Response<AllOtherExpense> {
        return api.getOtherExpense(startDate, endDate)
    }

    suspend fun editOtherExpense(
     otherExpense: OtherExpense
    ): Response<OtherExpenseResponse> {
        return api.editOtherExpense(otherExpense)
    }

    suspend fun approveDriverLog(
        token: String,
        password: ApproveDriverLogRequest,
    ): Response<ApproveDriverLogResponse> {
        return api.approveDriverLog(token, password)
    }
}