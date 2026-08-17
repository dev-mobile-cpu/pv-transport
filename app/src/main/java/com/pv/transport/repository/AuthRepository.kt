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
import com.pv.transport.data.log.ReasonResponse
import com.pv.transport.data.log.TripTypeResponse
import com.pv.transport.data.log.Data
import com.pv.transport.data.log.stableKey
import com.pv.transport.data.log.withCheckout
import com.pv.transport.extension.createMultipart
import com.pv.transport.extension.createMultipartFromFile
import com.pv.transport.extension.preloadImageThumb
import com.pv.transport.extension.createMultipartList
import com.pv.transport.extension.toRequestBody
import com.pv.transport.local.dao.CorporateUserCacheDao
import com.pv.transport.local.dao.DriverLogCacheDao
import com.pv.transport.local.dao.OfflineCheckInDao
import com.pv.transport.local.dao.OfflineCheckOutDao
import com.pv.transport.local.dao.OfflineOtherExpenseDao
import com.pv.transport.local.dao.OtherExpenseCacheDao
import com.pv.transport.local.data.CorporateUserCacheEntity
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.OfflineCheckInEntity
import com.pv.transport.local.data.OfflineCheckOutEntity
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.local.data.OtherExpenseCacheEntity
import com.pv.transport.local.data.SyncedRecordMapping
import com.pv.transport.network.NetworkUtils
import com.pv.transport.offline.OfflineImageHelper
import com.pv.transport.util.DebugLog
import com.pv.transport.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

private const val SYNCED_ROW_RETENTION_MS = 24 * 60 * 60 * 1000L

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    @ApplicationContext private val context: Context,
    private val checkInDao: OfflineCheckInDao,
    private val checkOutDao: OfflineCheckOutDao,
    private val expenseDao: OfflineOtherExpenseDao,
    private val masterDataRepository: MasterDataRepository,
    private val driverLogCacheDao: DriverLogCacheDao,
    private val corporateUserCacheDao: CorporateUserCacheDao,
    private val otherExpenseCacheDao: OtherExpenseCacheDao
) {
    suspend fun login(username: String, password: String): Response<LoginResponse> =
        api.login(username, password)

    // ── Master data (served from the local store) ─────────────────────────────

    suspend fun getReason(): Response<ReasonResponse> =
        Response.success(ReasonResponse(masterDataRepository.getReasons()))

    suspend fun getTripTypes(): Response<TripTypeResponse> =
        Response.success(TripTypeResponse(masterDataRepository.getTripTypes()))

    suspend fun getCostTypes(): Response<TypeCostResponse> =
        Response.success(TypeCostResponse(masterDataRepository.getCostTypes()))

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

        val uuid = UUID.randomUUID().toString()
        val entity = OfflineCheckInEntity(
            uuid = uuid,
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
        preloadImageThumb(context, photoPath, "$uuid-start")

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
        val uuid = UUID.randomUUID().toString()
        val entity = OfflineCheckInEntity(
            uuid = uuid,
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
        preloadImageThumb(context, photoPath, "$uuid-start")
        scheduleSyncWorker()
    }

    suspend fun checkOutDriverLog(
        recordId: String,
        remark: String,
        endTime: String,
        endKm: String,
        endPhoto: Uri,
        site: String = "",
        purpose: String = ""
    ): Response<DriverLogResponse> {
        return api.checkOutDriverLog(
            recordId = toRequestBody(recordId),
            remark = toRequestBody(remark),
            site = toRequestBody(site),
            purpose = toRequestBody(purpose),
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
        endPhoto: Uri,
        site: String = "",
        purpose: String = ""
    ) {
        val photoPath = OfflineImageHelper.copyUriToInternalStorage(context, endPhoto, "checkout")
            ?: throw IllegalStateException("Failed to save photo locally")
        val entity = OfflineCheckOutEntity(
            uuid = UUID.randomUUID().toString(),
            serverRecordId = serverRecordId,
            localCheckInUuid = localCheckInUuid,
            remark = remark,
            site = site,
            purpose = purpose,
            endTime = endTime,
            endKm = endKm,
            endPhotoPath = photoPath,
            clientTimestamp = System.currentTimeMillis()
        )
        checkOutDao.insert(entity)
        val endCacheKey = (localCheckInUuid?.takeIf { it.isNotBlank() } ?: serverRecordId.orEmpty())
            .takeIf { it.isNotBlank() }
        if (endCacheKey != null) {
            preloadImageThumb(context, photoPath, "$endCacheKey-end")
        }
        scheduleSyncWorker()
    }

    fun observePendingCheckIns(): Flow<List<OfflineCheckInEntity>> =
        checkInDao.observePendingCheckIns()

    fun observeRecentlySyncedCheckIns(): Flow<List<OfflineCheckInEntity>> =
        checkInDao.observeRecentlySyncedCheckIns(System.currentTimeMillis() - SYNCED_ROW_RETENTION_MS)

    fun observePendingCheckOuts(): Flow<List<OfflineCheckOutEntity>> =
        checkOutDao.observePendingCheckOuts()

    fun observeRecentlySyncedCheckOuts(): Flow<List<OfflineCheckOutEntity>> =
        checkOutDao.observeRecentlySyncedCheckOuts(System.currentTimeMillis() - SYNCED_ROW_RETENTION_MS)

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

    fun observeRecentlySyncedExpenseMappings(): Flow<List<SyncedRecordMapping>> =
        expenseDao.observeRecentlySyncedMappings(System.currentTimeMillis() - SYNCED_ROW_RETENTION_MS)

    fun observeRecentlySyncedExpenses(): Flow<List<OfflineOtherExpenseEntity>> =
        expenseDao.observeRecentlySyncedExpenses(System.currentTimeMillis() - SYNCED_ROW_RETENTION_MS)

    // ── Driver Logs Caching ───────────────────────────────────────────────────

    suspend fun getDriverLogs(startDate: String, endDate: String, page: Int? = null, perPage: Int = 20): Response<AllDriverLogResponse> {
        val response = api.getDriverLogList(startDate, endDate, page, perPage)
        if (response.isSuccessful && (page == null || page == 1)) {
            response.body()?.let { body ->
                upsertDriverLogCache(body.data, startDate, endDate)
            }
        }
        return response
    }

    private suspend fun upsertDriverLogCache(incoming: List<Data>, startDate: String, endDate: String) {
        val existing = driverLogCacheDao.getCachedLogsOnce()?.logs ?: emptyList()
        val incomingKeys = incoming.map { it.stableKey }.toSet()
        val incomingIds = incoming.map { it.id }.toSet()
        val incomingClientUuids = incoming.mapNotNull { it.clientUuid?.takeIf { uuid -> uuid.isNotBlank() } }.toSet()
        val existingByKey = existing.associateBy { it.stableKey }
        val existingById = existing.associateBy { it.id }
        val mergedIncoming = incoming.map { new ->
            val old = existingByKey[new.stableKey]
                ?: existingById[new.id]
                ?: new.clientUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
                    existing.find { it.clientUuid == uuid }
                }
            if (old != null) preferRicherDriverLog(new, old) else new
        }
        val kept = existing.filter { old ->
            val date = old.driverLog?.date?.take(10)
            val inFetchedRange = date == null || date in startDate..endDate
            if (!inFetchedRange) return@filter true
            val localKey = old.stableKey
            looksLikeLocalUuid(localKey) &&
                localKey !in incomingKeys &&
                old.id !in incomingIds &&
                old.id !in incomingClientUuids &&
                old.clientUuid?.takeIf { it.isNotBlank() } !in incomingClientUuids
        }
        driverLogCacheDao.insertCache(DriverLogCacheEntity(logs = mergedIncoming + kept))
    }

    private fun preferRicherDriverLog(primary: Data, secondary: Data): Data {
        val primaryHasEnd = !primary.endTime.isNullOrBlank() || !primary.endKm.isNullOrBlank()
        val secondaryHasEnd = !secondary.endTime.isNullOrBlank() || !secondary.endKm.isNullOrBlank()
        if (primaryHasEnd || !secondaryHasEnd) return primary
        return primary.copy(
            endTime = secondary.endTime,
            endKm = secondary.endKm,
            isCheckout = "false",
            checkoutClientUuid = primary.checkoutClientUuid ?: secondary.checkoutClientUuid,
            endImagePath = primary.endImagePath ?: secondary.endImagePath,
            driverLog = primary.driverLog?.withCheckout(secondary.endTime, secondary.endKm)
        )
    }

    private fun looksLikeLocalUuid(value: String): Boolean =
        value.length == 36 && value.contains("-")

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
                DebugLog.d("ExpenseRepository", "Successfully cached ${expenseList.size} expense logs to Room DB")
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
        signature: File
    ): Response<ApproveDriverLogResponse> {
        return api.approveDriverLog(
            id = token,
            password = toRequestBody(password),
            signature = createMultipartFromFile(signature, "signature")
        )
    }

    suspend fun getAssignedVehicle(): Response<AssignedVehicleResponse> = api.getAssignedVehicles()

    suspend fun saveLogSheet(date: String, uploadPhoto: Uri): Response<LogSheetResponse> {
       return api.saveLogSheet(
            date = toRequestBody(date),
            uploadPhoto = createMultipart(uploadPhoto, "logsheet", context)
        )
    }

    /** Re-trigger sync when a list page is opened/refreshed, but only if something is pending. */
    suspend fun scheduleSyncIfPending() {
        val hasPending = checkInDao.getPendingCheckIns().isNotEmpty() ||
                checkOutDao.getPendingCheckOuts().isNotEmpty() ||
                expenseDao.getPendingExpenses().isNotEmpty()
        if (hasPending) scheduleSyncWorker()
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
