package com.pv.transport.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pv.transport.api.FuelApi
import com.pv.transport.data.fuel.FuelCompaniesResponse
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.FuelLogResponse
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelRequestResponse
import com.pv.transport.data.fuel.FuelTypeResponse
import com.pv.transport.data.fuel.GeneralResponse
import com.pv.transport.data.fuel.WalletResponse
import com.pv.transport.extension.createMultipart
import com.pv.transport.extension.createMultipartList
import com.pv.transport.extension.toRequestBody
import com.pv.transport.local.dao.FuelLogCacheDao
import com.pv.transport.local.dao.OfflineFuelLogDao
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.FuelLogCacheEntity
import com.pv.transport.local.data.OfflineFuelLogEntity
import com.pv.transport.offline.OfflineImageHelper
import com.pv.transport.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

class FuelRepository @Inject constructor(
    private val api: FuelApi,
    @ApplicationContext private val context: Context,
    private val masterDataRepository: MasterDataRepository,
    private val offlineFuelLogDao: OfflineFuelLogDao,
    private val fuelLogCacheDao: FuelLogCacheDao
) {
    // ── Fuel Types (served from the local store) ───────────────────────────────

    suspend fun getFuelTypes(): Response<FuelTypeResponse> =
        Response.success(FuelTypeResponse(masterDataRepository.getFuelTypes()))

    // ── Save Fuel Log ──────────────────────────────────────────────────────────

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
            files = parts,
            currentKm = toRequestBody(currentKm),
            currentKmPhoto = createMultipart(currentKmPhoto, "current_km_photo", context),
            walletBucket = toRequestBody(walletBucket)
        )
    }

    suspend fun saveFuelLogOffline(
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
    ) {
        println("🔥 saveFuelLogOffline CALLED")

        val uuid = UUID.randomUUID().toString()

        println("🔥 Offline Fuel UUID = $uuid")

        val filePaths = OfflineImageHelper.copyUrisToInternalStorage(context, files, "fuellog")
        val kmPhotoPath = OfflineImageHelper.copyUriToInternalStorage(context, currentKmPhoto, "fuelkm") ?: return
        val entity = OfflineFuelLogEntity(
            uuid = uuid,
            carPlateNo = carPlateNo,
            date = date,
            fuelCompanyId = fuelCompanyId,
            fuelShop = fuelShop,
            fuelTypeId = fuelTypeId,
            fuelAmount = fuelAmount,
            fuelLiter = fuelLiter,
            filesPaths = OfflineImageHelper.pathsToJson(filePaths),
            currentKm = currentKm,
            currentKmPhotoPath = kmPhotoPath,
            walletBucket = walletBucket,
            clientTimestamp = System.currentTimeMillis()
        )
        offlineFuelLogDao.insert(entity)
        println("🔥 Offline Fuel INSERT DONE $uuid")
        scheduleSyncWorker()
    }

    fun observePendingFuelLogs(): Flow<List<OfflineFuelLogEntity>> =
        offlineFuelLogDao.observePendingFuelLogs()

    // ── Fuel Request ───────────────────────────────────────────────────────────

    suspend fun saveFundRequest(
        fuelRequest: FuelRequest,
        files: List<Uri> = emptyList()
    ): Response<GeneralResponse> {
        // Match Postman: Only send parts that are needed
        val fileParts = if (files.isNotEmpty()) {
            createMultipartList(files, "files[]", context)
        } else {
            null // Postman unchecked state
        }

        return api.saveFundRequest(
            requestCategory = toRequestBody(fuelRequest.requestCategory),
            amount = toRequestBody(fuelRequest.amount),
            fuelTypeId = fuelRequest.fuelTypeId?.let { if (it.isNotEmpty()) toRequestBody(it) else null },
            remark = fuelRequest.remark?.let { if (it.isNotEmpty()) toRequestBody(it) else null },
            requestType = fuelRequest.requestType?.let { if (it.isNotEmpty()) toRequestBody(it) else null },
            files = fileParts
        )
    }

    suspend fun getFuelRequest(startDate: String, endDate: String, page: Int? = null, perPage: Int = 20): Response<FuelRequestResponse> =
        api.getFuelRequestLogs(startDate, endDate, page, perPage)


    suspend fun getFuelLogs(startDate: String, endDate: String, page: Int? = null, perPage: Int = 20): Response<FuelLogResponse> {
        val response = api.getFuelLogs(startDate, endDate, page, perPage)

        // 🌟 API Success ဖြစ်ပြီး Page 1 (သို့) First Fetch ဆိုရင် Cache ထဲ သိမ်းမည်
        if (response.isSuccessful && (page == null || page == 1)) {
            response.body()?.data?.let { fuelList ->
                fuelLogCacheDao.insertCache(
                    FuelLogCacheEntity(
                        id = "last_fetched_logs",
                        logs = fuelList,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                Log.d("FuelRepository", "Successfully cached ${fuelList.size} fuel logs to Room DB")
            }
        }

        return response
    }
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
            files = parts,
            currentKm = toRequestBody(currentKm),
            currentKmPhoto = createMultipart(currentKmPhoto, "current_km_photo", context)
        )
    }

    suspend fun getWalletBalance(transactionPerPage: Int): Response<WalletResponse> = api.getWallet(transactionPerPage)
    suspend fun getWalletTransactions(transactionPerPage: Int): Response<WalletResponse> = api.getWalletTransactions(transactionPerPage)
    suspend fun getFuelCompanies(): Response<FuelCompaniesResponse> =
        Response.success(FuelCompaniesResponse(masterDataRepository.getFuelCompanies()))

    private fun scheduleSyncWorker() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }




    fun observeCachedFuelLogs(): Flow<FuelLogCacheEntity?> =
        fuelLogCacheDao.getCachedLogs()

}
