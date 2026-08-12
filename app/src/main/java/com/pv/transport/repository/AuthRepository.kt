package com.pv.transport.repository

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.pv.transport.data.log.CorporateUser
import com.pv.transport.data.log.CorporateUsersResponse
import com.pv.transport.data.log.DriverLogResponse
import com.pv.transport.data.log.GenerateQR
import com.pv.transport.data.log.GenerateQRResponse
import com.pv.transport.data.log.LogSheetResponse
import com.pv.transport.data.log.LoginResponse
import com.pv.transport.data.log.OtherExpenseResponse
import com.pv.transport.data.log.ReasonListResponse
import com.pv.transport.data.log.ReasonResponse
import com.pv.transport.data.log.TripType
import com.pv.transport.data.log.TripTypeResponse
import com.pv.transport.extension.createMultipart
import com.pv.transport.extension.createMultipartList
import com.pv.transport.extension.toRequestBody
import com.pv.transport.local.dao.CorporateUserCacheDao
import com.pv.transport.local.dao.CostTypeCacheDao
import com.pv.transport.local.dao.DriverLogCacheDao
import com.pv.transport.local.dao.OfflineCheckInDao
import com.pv.transport.local.dao.OfflineCheckOutDao
import com.pv.transport.local.dao.OfflineOtherExpenseDao
import com.pv.transport.local.dao.OtherExpenseCacheDao
import com.pv.transport.local.dao.ReasonCacheDao
import com.pv.transport.local.dao.TripTypeCacheDao
import com.pv.transport.local.data.CorporateUserCacheEntity
import com.pv.transport.local.data.CostTypeCacheEntity
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.FuelLogCacheEntity
import com.pv.transport.local.data.OfflineCheckInEntity
import com.pv.transport.local.data.OfflineCheckOutEntity
import com.pv.transport.local.data.OfflineFuelLogEntity
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.local.data.OtherExpenseCacheEntity
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
    private val driverLogCacheDao: DriverLogCacheDao,
    private val corporateUserCacheDao: CorporateUserCacheDao,
    private val otherExpenseCacheDao: OtherExpenseCacheDao
) {
    suspend fun login(username: String, password: String): Response<LoginResponse> =
        api.login(username, password)

    // ── Reasons ───────────────────────────────────────────────────────────────

    suspend fun getReason(): Response<ReasonResponse> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getReasons()
            println("Hey Reason------ ${response.body()}")
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

    // ── Trip Types ────────────────────────────────────────────────────────────

    suspend fun getTripTypes(): Response<TripTypeResponse> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getTripTypes()
            println("Hey Trip Type------ ${response.body()}")
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

    // ── Cost Types ────────────────────────────────────────────────────────────

    suspend fun getCostTypes(): Response<TypeCostResponse> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getTypeCost()
            println("Hey Type Cost------ ${response.body()}")
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

    // ── Check-In ──────────────────────────────────────────────────────────────

    suspend fun checkInDriverLog(
        date: String,
        type: String,
        reasonId: String,
        site: String,
        purpose: String,
        remark: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri
    ): Response<DriverLogResponse> {
        println("Hey checkInDriverLog------ $date $type $reasonId $site $purpose $remark $startTime $startKm")
        return api.checkInDriverLog(
            date = toRequestBody(date),
            type = toRequestBody(type),
            reason = toRequestBody(reasonId),
            site = toRequestBody(site),
            purpose = toRequestBody(purpose),
            remark = toRequestBody(remark),
            startTime = toRequestBody(startTime),
            startKm = toRequestBody(startKm),
            createMultipart(startPhoto, "start_photo", context)
        )
    }

    suspend fun checkInDriverLogOffline(
        date: String,
        type: String,
        reasonId: String,
        site: String,
        purpose: String,
        remark: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri
    ) {
        val photoPath = OfflineImageHelper.copyUriToInternalStorage(context, startPhoto, "checkin")
            ?: throw IllegalStateException("Failed to save photo locally")

        val entity = OfflineCheckInEntity(
            uuid = UUID.randomUUID().toString(),
            date = date,
            type = type,
            reason = reasonId,
            site = site,
            purpose = purpose,
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
        reasonId: String,
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
            reason = toRequestBody(reasonId),
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
        reasonId: String,
        startTime: String,
        startKm: String,
        startPhoto: Uri
    ) {
        val photoPath = OfflineImageHelper.copyUriToInternalStorage(context, startPhoto, "checkin")
            ?: throw IllegalStateException("Failed to save photo locally")
        val entity = OfflineCheckInEntity(
            uuid = UUID.randomUUID().toString(),
            date = date,
            type = type,
            reason = reasonId,
            site = "",
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
        val photoPath = OfflineImageHelper.copyUriToInternalStorage(context, endPhoto, "checkout")
            ?: throw IllegalStateException("Failed to save photo locally")
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

    // ── Other Expense ─────────────────────────────────────────────────────────

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
        typeCostId: String,
        typeOfCost: String,
        amount: String,
        licensePlate: String,
        photo: List<Uri>
    ) {
        val paths = OfflineImageHelper.copyUrisToInternalStorage(context, photo, "expense")
        val entity = OfflineOtherExpenseEntity(
            uuid = UUID.randomUUID().toString(),
            date = date,
            typeOfCostId = typeCostId,
            typeOfCost = typeOfCost,
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

    // ── Other existing methods ────────────────────────────────────────────────

    suspend fun getApprovalStatus(startDate: String, endDate: String, status: String, page: Int? = null, perPage: Int = 20): Response<AllDriverLogResponse> =
        api.getApprovals(startDate, endDate, status, page, perPage)

    suspend fun getCorporateUsers(): Response<List<CorporateUsersResponse>> {
        return if (NetworkUtils.isInternetAvailable(context)) {
            val response = api.getCorporateUsers()

            if (response.isSuccessful) {
                response.body()?.let { list ->
                    corporateUserCacheDao.clear()
                    corporateUserCacheDao.insertAll(
                        list.map {
                            CorporateUserCacheEntity(
                                id = it.id.toInt(),
                                corporateId = it.corporateId.toInt(),
                                name = it.name,
                                email = it.email?: "",
                                phone = it.phone?: "",
                                pinSet = it.pinSet
                            )
                        }
                    )
                }
            }

            response
        } else {
            val cached = corporateUserCacheDao.getAll()

            val list = cached.map {
                CorporateUsersResponse(
                    id = it.id.toString(),
                    corporateId = it.corporateId.toString(),
                    name = it.name,
                    email = it.email,
                    phone = it.phone,
                    pinSet = it.pinSet
                )
            }

            Response.success(list)
        }
    }

    suspend fun getGenerateQR(generateQR: GenerateQR): Response<GenerateQRResponse> = api.getGenerateQR(generateQR)

    suspend fun getOthersExpense(startDate: String, endDate: String, page: Int? = null, perPage: Int = 20): Response<AllOtherExpense> {
        val response = api.getOtherExpense(startDate, endDate, page, perPage)

        // 🌟 API Success ဖြစ်ပြီး Page 1 (သို့) First Fetch ဆိုရင် Cache ထဲ သိမ်းမည်
        if (response.isSuccessful && (page == null || page == 1)) {
            response.body()?.data?.let { expenseList ->
                otherExpenseCacheDao.insertCache(
                    OtherExpenseCacheEntity(
                        id = "last_fetched_logs",
                        logs = expenseList,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                Log.d("ExpenseRepository", "Successfully cached ${expenseList.size} expense logs to Room DB")
            }
        }

        return response
    }

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

    suspend fun approveDriverLog(
        token: String, 
        password: String,
        signature: Uri
    ): Response<ApproveDriverLogResponse> {
        return api.approveDriverLog(
            id = token,
            password = toRequestBody(password),
            signature = createMultipart(signature, "signature", context)
        )
    }

    suspend fun getAssignedVehicle(): Response<AssignedVehicleResponse> = api.getAssignedVehicles()

    suspend fun saveLogSheet(date: String, uploadPhoto: Uri): Response<LogSheetResponse> {
       return api.saveLogSheet(
            date = toRequestBody(date),
            uploadPhoto = createMultipart(uploadPhoto, "logsheet", context)
        )
    }

    fun scheduleSyncWorker() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            // Add exponential backoff to avoid hammering the server on repeated failures
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30_000L,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        // Chain another sync after the current one so newly saved local rows are uploaded
        WorkManager.getInstance(context).enqueueUniqueWork(
            "offline_sync",
            androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }


    fun observeCachedOtherExpenseLogs(): Flow<OtherExpenseCacheEntity?> =
        otherExpenseCacheDao.getCachedLogs()
}
