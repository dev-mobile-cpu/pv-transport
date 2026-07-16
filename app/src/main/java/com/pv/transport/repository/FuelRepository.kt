package com.pv.transport.repository

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pv.transport.api.FuelApi
import com.pv.transport.data.fuel.FuelCompaniesResponse
import com.pv.transport.data.fuel.FuelCompany
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.FuelLogResponse
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelRequestResponse
import com.pv.transport.data.fuel.FuelType
import com.pv.transport.data.fuel.FuelTypeResponse
import com.pv.transport.data.fuel.GeneralResponse
import com.pv.transport.data.fuel.WalletResponse
import com.pv.transport.extension.createMultipart
import com.pv.transport.extension.createMultipartList
import com.pv.transport.extension.toRequestBody
import com.pv.transport.local.dao.FuelCompanyCacheDao
import com.pv.transport.local.dao.FuelLogCacheDao
import com.pv.transport.local.dao.FuelTypeCacheDao
import com.pv.transport.local.dao.OfflineFuelLogDao
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.FuelCompanyCacheEntity
import com.pv.transport.local.data.FuelLogCacheEntity
import com.pv.transport.local.data.FuelTypeCacheEntity
import com.pv.transport.local.data.OfflineFuelLogEntity
import com.pv.transport.network.NetworkUtils
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
    private val fuelTypeCacheDao: FuelTypeCacheDao,
    private val offlineFuelLogDao: OfflineFuelLogDao,
    private val fuelCompanyCacheDao: FuelCompanyCacheDao,
    private val fuelLogCacheDao: FuelLogCacheDao
) {
    // ── Fuel Types ─────────────────────────────────────────────────────────────

    suspend fun getFuelTypes(): Response<FuelTypeResponse> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getFuelTypes()
            if (response.isSuccessful) {
                response.body()?.records?.let { list ->
                    fuelTypeCacheDao.clear()
                    fuelTypeCacheDao.insertAll(list.map { FuelTypeCacheEntity(it.id, it.name) })
                }
            }
            response
        } else {
            val cached = fuelTypeCacheDao.getAll()
            val fakeResponse = FuelTypeResponse(cached.map { FuelType(it.id, it.name) })
            Response.success(fakeResponse)
        }
    }

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

    suspend fun getFuelLogs(startDate: String, endDate: String, page: Int? = null, perPage: Int = 20): Response<FuelLogResponse> =
        api.getFuelLogs(startDate, endDate, page, perPage)

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
    suspend fun getFuelCompanies(): Response<FuelCompaniesResponse> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getFuelCompanies()
            if (response.isSuccessful) {

                response.body()?.data.let { list ->
                    fuelCompanyCacheDao.clear()
                    fuelCompanyCacheDao.insertAll(list!!.map { FuelCompanyCacheEntity(it.id, it.name,it.phone,it.email,it.address) })
                }
            }
            response
        } else {
            val cached = fuelCompanyCacheDao.getAll()
            val fakeResponse = FuelCompaniesResponse(cached.map { FuelCompany(it.id, it.name,it.phone,it.email,it.address) })
            Response.success(fakeResponse)
        }
    }

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
