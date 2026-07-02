package com.pv.transport.repository

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pv.transport.api.FuelApi
import com.pv.transport.data.fuel.FuelCompaniesResponse
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
import com.pv.transport.local.dao.FuelTypeCacheDao
import com.pv.transport.local.dao.OfflineFuelLogDao
import com.pv.transport.local.data.FuelTypeCacheEntity
import com.pv.transport.local.data.OfflineFuelLogEntity
import com.pv.transport.network.NetworkUtils
import com.pv.transport.offline.OfflineImageHelper
import com.pv.transport.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

class FuelRepository @Inject constructor(
    private val api: FuelApi,
    @ApplicationContext private val context: Context,
    private val fuelTypeCacheDao: FuelTypeCacheDao,
    private val offlineFuelLogDao: OfflineFuelLogDao
) {
    // ── Fuel Types (network-first, fall back to cache) ─────────────────────────

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

    // ── Save Fuel Log (offline-first) ──────────────────────────────────────────

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
        val filePaths = OfflineImageHelper.copyUrisToInternalStorage(context, files, "fuellog")
        val kmPhotoPath = OfflineImageHelper.copyUriToInternalStorage(context, currentKmPhoto, "fuelkm") ?: return
        val entity = OfflineFuelLogEntity(
            uuid = UUID.randomUUID().toString(),
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
        scheduleSyncWorker()
    }

    fun observePendingFuelLogs(): Flow<List<OfflineFuelLogEntity>> =
        offlineFuelLogDao.observePendingFuelLogs()

    // ── Other existing methods (unchanged) ─────────────────────────────────────

    suspend fun saveFundRequest(fuelRequest: FuelRequest): Response<GeneralResponse> =
        api.saveFundRequest(fuelRequest)

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
    suspend fun getFuelCompanies(): Response<FuelCompaniesResponse> = api.getFuelCompanies()

    // ── WorkManager trigger ────────────────────────────────────────────────────

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
}
