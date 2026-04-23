package com.pv.transport.repository

import android.content.Context
import android.net.Uri
import com.pv.transport.api.FuelApi
import com.pv.transport.data.fuel.FuelCompaniesResponse
import com.pv.transport.data.fuel.FuelLogResponse
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelRequestResponse
import com.pv.transport.data.fuel.FuelTypeResponse
import com.pv.transport.data.fuel.GeneralResponse
import com.pv.transport.data.fuel.WalletResponse
import com.pv.transport.extension.createMultipart
import com.pv.transport.extension.createMultipartList
import com.pv.transport.extension.toRequestBody
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

class FuelRepository @Inject constructor(private val api: FuelApi,@ApplicationContext private val  context: Context) {

    suspend fun getFuelTypes(): Response<FuelTypeResponse> = api.getFuelTypes()
    suspend fun saveFundRequest(
        fuelRequest: FuelRequest

    ): Response<GeneralResponse> {
        return api.saveFundRequest(
          fuelRequest
        )
    }

    suspend fun getFuelRequest(startDate: String, endDate: String,page: Int? = null, perPage: Int=20): Response<FuelRequestResponse>{
        return api.getFuelRequestLogs(startDate, endDate,page,perPage)

    }
    suspend fun saveFuelLog(
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
    ): Response<GeneralResponse> {
        val parts = createMultipartList(files, "files[]", context)
        return api.saveFuelLog(
            carPlateNo = toRequestBody(carPlateNo),
            date = toRequestBody(date),
            fuelCompanyId = toRequestBody(fuelCompanyId),
            fuelShop = toRequestBody(fuelShop),
            fuelTypeId = toRequestBody(fuelTypeId),
            fuelAmount = toRequestBody(fuelAmount),
            fuelLiter = toRequestBody(fuelLiter),
            files = parts, // Placeholder for files, actual files are sent as Multipart
            currentKm = toRequestBody(currentKm),
            currentKmPhoto = createMultipart(currentKmPhoto, "current_km_photo", context),
            walletBucket = toRequestBody(walletBucket)
        )
    }

    suspend fun getFuelLogs(startDate: String, endDate: String,page: Int? = null, perPage: Int=20): Response<FuelLogResponse> = api.getFuelLogs(startDate, endDate,page,perPage)


        suspend fun approveFuelLog(
            carPlateNo: String,
            date: String,
            fuelShop: String,
            fuelTypeId: String,
            fuelAmount: String,
            fuelLiter: String,
            files: List<Uri>,
            currentKm: String,
            currentKmPhoto: Uri,
            walletBucket: String
        ): Response<GeneralResponse> {
            val parts = createMultipartList(files, "files[]", context)
            return api.approveFuelLog(
                carPlateNo = toRequestBody(carPlateNo),
                date = toRequestBody(date),
                fuelShop = toRequestBody(fuelShop),
                fuelTypeId = toRequestBody(fuelTypeId),
                fuelAmount = toRequestBody(fuelAmount),
                fuelLiter = toRequestBody(fuelLiter),
                files = parts, // Placeholder for files, actual files are sent as Multipart
                currentKm = toRequestBody(currentKm),
                currentKmPhoto = createMultipart(currentKmPhoto, "current_km_photo", context),

            )
        }

    suspend fun getWalletBalance(transactionPerPage: Int): Response<WalletResponse> = api.getWallet()
    suspend fun getFuelCompanies(): Response<FuelCompaniesResponse> = api.getFuelCompanies()

}