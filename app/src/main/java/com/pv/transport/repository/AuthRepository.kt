package com.pv.transport.repository

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pv.transport.api.AuthApi
import com.pv.transport.data.AllOtherExpense
import com.pv.transport.data.TypeCostResponse
import com.pv.transport.data.CostType
import com.pv.transport.data.log.AllDriverLogResponse
import com.pv.transport.data.log.ApproveDriverLogRequest
import com.pv.transport.data.log.ApproveDriverLogResponse
import com.pv.transport.data.log.AssignedVehicleResponse
import com.pv.transport.data.log.CorporateUsersResponse
import com.pv.transport.data.log.DriverLogResponse
import com.pv.transport.data.log.GenerateQR
import com.pv.transport.data.log.GenerateQRResponse
import com.pv.transport.data.log.LoginResponse
import com.pv.transport.data.log.OtherExpenseResponse
import com.pv.transport.data.log.ReasonListResponse
import com.pv.transport.data.log.ReasonResponse
import com.pv.transport.data.log.TripType
import com.pv.transport.data.log.TripTypeResponse
import com.pv.transport.extension.createMultipart
import com.pv.transport.extension.createMultipartList
import com.pv.transport.extension.toRequestBody
import com.pv.transport.local.dao.CostTypeCacheDao
import com.pv.transport.local.dao.DriverLogCacheDao
import com.pv.transport.local.dao.OfflineCheckInDao
import com.pv.transport.local.dao.OfflineCheckOutDao
import com.pv.transport.local.dao.OfflineOtherExpenseDao
import com.pv.transport.local.dao.ReasonCacheDao
import com.pv.transport.local.dao.TripTypeCacheDao
import com.pv.transport.local.data.CostTypeCacheEntity
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.OfflineCheckInEntity
import com.pv.transport.local.data.OfflineCheckOutEntity
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.local.data.ReasonCacheEntity
import com.pv.transport.local.data.TripTypeCacheEntity
import com.pv.transport.network.NetworkUtils
import com.pv.transport.offline.OfflineImageHelper
import com.pv.transport.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    @ApplicationContext private val context: Context,
    private val checkInDao: OfflineCheckInDao,
    private val checkOutDao: OfflineCheckOutDao,
    private val expenseDao: OfflineOtherExpenseDao,
    private val reasonCacheDao: ReasonCacheDao,
    private val tripTypeCacheDao: TripTypeCacheDao,
    private val costTypeCacheDao: CostTypeCacheDao,
    private val driverLogCacheDao: DriverLogCacheDao
) {
    suspend fun login(username: String, password: String): Response<LoginResponse> =
        api.login(username, password)

    // ── Reasons (network-first, fall back to cache) ────────────────────────────

    suspend fun getReason(): Response<ReasonResponse> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getReasons()
            if (response.isSuccessful) {
                response.body()?.data?.let { list ->
                    reasonCacheDao.clear()
                    reasonCacheDao.insertAll(list.map { ReasonCacheEntity(it.id, it.value) })
                }
            }
            response
        } else {
            val cached = reasonCacheDao.getAll()
            val fakeResponse = ReasonResponse(cached.map { ReasonListResponse(it.id, it.value) })
            Response.success(fakeResponse)
        }
    }

    // ── Trip Types (network-first, fall back to cache) ─────────────────────────

    suspend fun getTripTypes(): Response<TripTypeResponse> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getTripTypes()
            if (response.isSuccessful) {
                response.body()?.data?.let { list ->
                    tripTypeCacheDao.clear()
                    tripTypeCacheDao.insertAll(list.map { TripTypeCacheEntity(it.id, it.value) })
                }
            }
            response
        } else {
            val cached = tripTypeCacheDao.getAll()
            val fakeResponse = TripTypeResponse(cached.map { TripType(it.id, it.value) })
            Response.success(fakeResponse)
        }
    }

    // ── Cost Types (network-first, fall back to cache) ─────────────────────────

    suspend fun getCostTypes(): Response<TypeCostResponse> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getTypeCost()
            if (response.isSuccessful) {
                response.body()?.data?.let { list ->
                    costTypeCacheDao.clear()
                    costTypeCacheDao.insertAll(list.map { CostTypeCacheEntity(it.id, it.name) })
                }
            }
            response
        } else {
            val cached = costTypeCacheDao.getAll()
            val fakeResponse = TypeCostResponse(cached.map { CostType(it.id, it.name) })
            Response.success(fakeResponse)
        }
    }

    // ── Check-In (offline-first) ───────────────────────────────────────────────

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

    suspend fun checkInDriverLogOffline(
        date: String,
        type: String,
        reason: String,
        remark: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri
    ) {
        val photoPath = OfflineImageHelper.copyUriToInternalStorage(context, startPhoto, "checkin") ?: return
        val entity = OfflineCheckInEntity(
            uuid = UUID.randomUUID().toString(),
            date = date,
            type = type,
            reason = reason,
            remark = remark,
            startTime = startTime,
            startKm = startKm,
            startPhotoPath = photoPath,
            clientTimestamp = System.currentTimeMillis()
        )
        checkInDao.insert(entity)
        scheduleSyncWorker()
    }

    suspend fun checkInTripDriverLog(
        date: String,
        type: String,
        tripTypeId: String,
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
            tripTypeId = toRequestBody(tripTypeId),
            from = toRequestBody(from),
            to = toRequestBody(to),
            purpose = toRequestBody(purpose),
            reason = toRequestBody(reason),
            startTime = toRequestBody(startTime),
            startKm = toRequestBody(startKm),
            createMultipart(startPhoto, "start_photo", context)
        )
    }

    suspend fun checkInTripDriverLogOffline(
        date: String,
        type: String,
        tripTypeId: String,
        from: String,
        to: String,
        purpose: String,
        reason: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri
    ) {
        val photoPath = OfflineImageHelper.copyUriToInternalStorage(context, startPhoto, "checkin") ?: return
        val entity = OfflineCheckInEntity(
            uuid = UUID.randomUUID().toString(),
            date = date,
            type = type,
            reason = reason,
            remark = "",
            startTime = startTime,
            startKm = startKm,
            startPhotoPath = photoPath,
            tripTypeId = tripTypeId,
            fromLocation = from,
            toLocation = to,
            purpose = purpose,
            clientTimestamp = System.currentTimeMillis()
        )
        checkInDao.insert(entity)
        scheduleSyncWorker()
    }

    suspend fun checkOutDriverLog(
        recordId: String,
        remark: String,
        endTime: String,
        endKm: String,
        endPhoto: Uri
    ): Response<DriverLogResponse> {
        return api.checkOutDriverLog(
            recordId = toRequestBody(recordId),
            remark = toRequestBody(remark),
            startTime = toRequestBody(endTime),
            startKm = toRequestBody(endKm),
            createMultipart(endPhoto, "end_photo", context)
        )
    }

    suspend fun checkOutDriverLogOffline(
        serverRecordId: String?,
        localCheckInUuid: String?,
        remark: String,
        endTime: String,
        endKm: String,
        endPhoto: Uri
    ) {
        val photoPath = OfflineImageHelper.copyUriToInternalStorage(context, endPhoto, "checkout") ?: return
        val entity = OfflineCheckOutEntity(
            uuid = UUID.randomUUID().toString(),
            serverRecordId = serverRecordId,
            localCheckInUuid = localCheckInUuid,
            remark = remark,
            endTime = endTime,
            endKm = endKm,
            endPhotoPath = photoPath,
            clientTimestamp = System.currentTimeMillis()
        )
        checkOutDao.insert(entity)
        scheduleSyncWorker()
    }

    fun observePendingCheckIns(): Flow<List<OfflineCheckInEntity>> =
        checkInDao.observePendingCheckIns()

    fun observePendingCheckOuts(): Flow<List<OfflineCheckOutEntity>> =
        checkOutDao.observePendingCheckOuts()

    // ── Other Expense (offline-first) ──────────────────────────────────────────

    suspend fun saveOtherExpense(
        date: String,
        typeCost: String,
        amount: String,
        licensePlate: String,
        photo: List<Uri>
    ): Response<OtherExpenseResponse> {
        val parts = createMultipartList(photo, "files[]", context)
        return api.saveOtherExpense(
            date = toRequestBody(date),
            typeOfCostId = toRequestBody(typeCost),
            amount = toRequestBody(amount),
            licensePlate = toRequestBody(licensePlate),
            files = parts
        )
    }

    suspend fun saveOtherExpenseOffline(
        date: String,
        typeCost: String,
        amount: String,
        licensePlate: String,
        photo: List<Uri>
    ) {
        val paths = OfflineImageHelper.copyUrisToInternalStorage(context, photo, "expense")
        val entity = OfflineOtherExpenseEntity(
            uuid = UUID.randomUUID().toString(),
            date = date,
            typeOfCostId = typeCost,
            amount = amount,
            licensePlate = licensePlate,
            filesPaths = OfflineImageHelper.pathsToJson(paths),
            clientTimestamp = System.currentTimeMillis()
        )
        expenseDao.insert(entity)
        scheduleSyncWorker()
    }

    fun observePendingExpenses(): Flow<List<OfflineOtherExpenseEntity>> =
        expenseDao.observePendingExpenses()

    // ── Driver Logs Caching ───────────────────────────────────────────────────

    suspend fun getDriverLogs(startDate: String, endDate: String, page: Int? = null, perPage: Int = 20): Response<AllDriverLogResponse> {
        val response = api.getDriverLogList(startDate, endDate, page, perPage)
        if (response.isSuccessful && page == null || page == 1) {
            response.body()?.let { body ->
                driverLogCacheDao.insertCache(DriverLogCacheEntity(logs = body.data))
            }
        }
        return response
    }

    fun observeCachedDriverLogs(): Flow<DriverLogCacheEntity?> =
        driverLogCacheDao.getCachedLogs()

    // ── Other existing methods (unchanged) ─────────────────────────────────────

    suspend fun getApprovalStatus(startDate: String, endDate: String, status: String, page: Int? = null, perPage: Int = 20): Response<AllDriverLogResponse> =
        api.getApprovals(startDate, endDate, status, page, perPage)

    suspend fun getCorporateUsers(): Response<List<CorporateUsersResponse>> = api.getCorporateUsers()

    suspend fun getGenerateQR(generateQR: GenerateQR): Response<GenerateQRResponse> = api.getGenerateQR(generateQR)

    suspend fun getOthersExpense(startDate: String, endDate: String, page: Int? = null, perPage: Int = 20): Response<AllOtherExpense> =
        api.getOtherExpense(startDate, endDate, page, perPage)

    suspend fun editOtherExpense(
        id: String,
        date: String,
        typeCost: String,
        amount: String,
        licensePlate: String,
        photo: List<Uri>,
        deleteDocs: List<String>
    ): Response<OtherExpenseResponse> {
        val parts = createMultipartList(photo, "files[]", context)
        val deleteDocsParts = deleteDocs.map { docId ->
            MultipartBody.Part.createFormData("delete_docs[]", docId)
        }
        return api.editOtherExpense(
            id = id.toRequestBody("text/plain".toMediaTypeOrNull()),
            date = date.toRequestBody("text/plain".toMediaTypeOrNull()),
            typeOfCostId = typeCost.toRequestBody("text/plain".toMediaTypeOrNull()),
            amount = amount.toRequestBody("text/plain".toMediaTypeOrNull()),
            licensePlate = licensePlate.toRequestBody("text/plain".toMediaTypeOrNull()),
            files = parts,
            deleteDocs = deleteDocsParts
        )
    }

    suspend fun approveDriverLog(token: String, password: ApproveDriverLogRequest): Response<ApproveDriverLogResponse> =
        api.approveDriverLog(token, password)

    suspend fun getAssignedVehicle(): Response<AssignedVehicleResponse> = api.getAssignedVehicles()

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
