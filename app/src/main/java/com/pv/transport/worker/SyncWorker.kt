package com.pv.transport.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pv.transport.api.AuthApi
import com.pv.transport.api.FuelApi
import com.pv.transport.local.dao.OfflineCheckInDao
import com.pv.transport.local.dao.OfflineCheckOutDao
import com.pv.transport.local.dao.OfflineFuelLogDao
import com.pv.transport.local.dao.OfflineOtherExpenseDao
import com.pv.transport.offline.OfflineImageHelper
import com.pv.transport.util.DebugLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import com.google.gson.Gson
import com.pv.transport.BuildConfig
import com.pv.transport.local.dao.DriverLogCacheDao
import com.pv.transport.local.dao.FuelLogCacheDao
import com.pv.transport.local.dao.OtherExpenseCacheDao
import com.pv.transport.data.log.Data
import com.pv.transport.data.log.stableKey
import com.pv.transport.data.log.withCheckout
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.FuelLogCacheEntity
import com.pv.transport.local.data.OfflineCheckOutEntity
import com.pv.transport.local.data.OtherExpenseCacheEntity
import java.time.LocalDate

private const val TAG = "SyncWorker"
private const val CACHE_DAYS = 7L
private const val SYNCED_ROW_RETENTION_MS = 24 * 60 * 60 * 1000L

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val authApi: AuthApi,
    private val fuelApi: FuelApi,
    private val checkInDao: OfflineCheckInDao,
    private val checkOutDao: OfflineCheckOutDao,
    private val fuelLogDao: OfflineFuelLogDao,
    private val expenseDao: OfflineOtherExpenseDao,
    private val driverLogCacheDao: DriverLogCacheDao,
    private val fuelLogCacheDao: FuelLogCacheDao,
    private val expenseCacheDao: OtherExpenseCacheDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Reset all syncing status first in case of previous crash
            checkInDao.resetSyncingStatus()
            checkOutDao.resetSyncingStatus()
            fuelLogDao.resetSyncingStatus()
            expenseDao.resetSyncingStatus()

            val pendingInCount = checkInDao.getPendingCheckIns().size
            val pendingOutCount = checkOutDao.getPendingCheckOuts().size
            val pendingFuelCount = fuelLogDao.getPendingFuelLogs().size
            val pendingExpenseCount = expenseDao.getPendingExpenses().size
            DebugLog.d(TAG, "Pending offline counts - checkIns: $pendingInCount, checkOuts: $pendingOutCount, fuel: $pendingFuelCount, expenses: $pendingExpenseCount")

            val hasDataToSync = hasPendingData()

            if (hasDataToSync) {
                syncCheckInsSequentially()
                DebugLog.d(TAG, "Finished syncing check-ins")
                syncRemainingCheckOuts()
                DebugLog.d(TAG, "Finished syncing remaining check-outs")
                syncFuelLogs()
                DebugLog.d(TAG, "Finished syncing fuel logs")
                syncOtherExpenses()
                DebugLog.d(TAG, "Finished syncing other expenses")
                // Cleanup local synced rows to prevent offline cards from persisting
                try {
                    val syncedCutoff = System.currentTimeMillis() - SYNCED_ROW_RETENTION_MS
                    checkInDao.deleteSyncedOlderThan(syncedCutoff)
                    checkOutDao.deleteSyncedOlderThan(syncedCutoff)
                    fuelLogDao.deleteSyncedOlderThan(syncedCutoff)
                    expenseDao.deleteSyncedOlderThan(syncedCutoff)
                    DebugLog.d(TAG, "Deleted synced offline rows from local DB")

                    // Refresh driver log cache so UI shows up-to-date server data
                    refreshDriverLogCache()
                    refreshFuelLogCache()
                    refreshExpenseCache()


                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete synced offline rows", e)
                }

                // Records that failed (server error / missing photo) stay pending — say so
                val remaining = checkInDao.getPendingCheckIns().size +
                        checkOutDao.getPendingCheckOuts().size +
                        fuelLogDao.getPendingFuelLogs().size +
                        expenseDao.getPendingExpenses().size
                if (remaining > 0) {
                    Log.w(TAG, "Sync finished with $remaining record(s) still pending")
                } else {
                    DebugLog.d(TAG, "Sync completed: uploaded pending offline data")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            checkInDao.resetSyncingStatus()
            checkOutDao.resetSyncingStatus()
            fuelLogDao.resetSyncingStatus()
            expenseDao.resetSyncingStatus()
            Result.retry()
        }
    }


    private suspend fun refreshDriverLogCache() {
        try {
            val resp = authApi.getDriverLogList(startDate = cacheStart(), endDate = cacheEnd(), page = null, perPage = 50)
            if (resp.isSuccessful) {
                resp.body()?.data?.let { list ->
                    val existing = driverLogCacheDao.getCachedLogsOnce()?.logs ?: emptyList()
                    val existingByKey = existing.associateBy { it.stableKey }
                    val existingById = existing.associateBy { it.id }
                    val merged = list.map { incoming ->
                        val old = existingByKey[incoming.stableKey]
                            ?: existingById[incoming.id]
                            ?: incoming.clientUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
                                existing.find { it.clientUuid == uuid }
                            }
                        if (old != null) preferRicherDriverLog(incoming, old) else incoming
                    }
                    driverLogCacheDao.insertCache(DriverLogCacheEntity(logs = merged))
                    DebugLog.d(TAG, "Refreshed driver log cache with ${merged.size} records")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh driver log cache", e)
        }
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
    private suspend fun refreshFuelLogCache(){

        try {
            val response = fuelApi.getFuelLogs(startDate = cacheStart(), endDate = cacheEnd(), page = null, perPage = 50)

            if(response.isSuccessful){
                val logs = response.body()?.data ?: emptyList()
                fuelLogCacheDao.insertCache(FuelLogCacheEntity(logs = logs))
                DebugLog.d(TAG, "Fuel cache updated ${logs.size}")
            }

        }catch(e:Exception){

            Log.e(TAG, "Fuel cache refresh error", e)
        }

    }

    /** Cache a few days, not just today, so a list filtered to a nearby date isn't left empty. */
    private fun cacheStart() = LocalDate.now().minusDays(CACHE_DAYS).toString()

    private fun cacheEnd() = LocalDate.now().toString()

    private suspend fun refreshExpenseCache() {
        try {
            val response = authApi.getOtherExpense(startDate = cacheStart(), endDate = cacheEnd(), page = null, perPage = 50)

            if (response.isSuccessful) {
                val expenses = response.body()?.data ?: emptyList()
                expenseCacheDao.insertCache(OtherExpenseCacheEntity(logs = expenses))
                DebugLog.d(TAG, "Expense cache updated with ${expenses.size} records")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh expense cache", e)
        }
    }

    private suspend fun hasPendingData(): Boolean {
        // Include all offline tables (check-ins, check-outs, fuel logs and other expenses)
        // so the worker will run uploads even when only fuel/expense entries are pending.
        return checkInDao.getPendingCheckIns().isNotEmpty() ||
               checkOutDao.getPendingCheckOuts().isNotEmpty() ||
               fuelLogDao.getPendingFuelLogs().isNotEmpty() ||
               expenseDao.getPendingExpenses().isNotEmpty()
    }

    private suspend fun syncCheckInsSequentially() {
        val pendingCheckIns = checkInDao.getPendingCheckIns()
        for (checkIn in pendingCheckIns) {
            try {
                checkInDao.updateSyncingStatus(checkIn.uuid, true)
                val photoFile = OfflineImageHelper.fileFromPath(checkIn.startPhotoPath)
                if (photoFile == null) {
                    Log.w(TAG, "Missing start photo for check-in ${checkIn.uuid}; keeping record pending")
                    checkInDao.updateSyncingStatus(checkIn.uuid, false)
                    continue
                }
                val photoBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("start_photo", photoFile.name, photoBody)

                val response = if (checkIn.tripTypeId != null) {
                    authApi.checkInTripDriverLogSync(
                        date = checkIn.date.toRB(),
                        type = checkIn.type.toRB(),
                        tripTypeId = checkIn.tripTypeId.toRB(),
                        from = (checkIn.fromLocation ?: "").toRB(),
                        to = (checkIn.toLocation ?: "").toRB(),
                        purpose = (checkIn.purpose ?: "").toRB(),
                        reason = checkIn.reason.toRB(),
                        startTime = checkIn.startTime.toRB(),
                        startKm = checkIn.startKm.toRB(),
                        startPhoto = photoPart,
                        uuid = checkIn.uuid.toRB(),
                        clientTimestamp = checkIn.clientTimestamp.toString().toRB()
                    )
                } else {
                    authApi.checkInDriverLogSync(
                        date = checkIn.date.toRB(),
                        type = checkIn.type.toRB(),
                        reason = checkIn.reason.toRB(),
                        site = checkIn.site.toRB(),
                        purpose = (checkIn.purpose ?: "").toRB(),
                        remark = checkIn.remark.toRB(),
                        startTime = checkIn.startTime.toRB(),
                        startKm = checkIn.startKm.toRB(),
                        startPhoto = photoPart,
                        uuid = checkIn.uuid.toRB(),
                        clientTimestamp = checkIn.clientTimestamp.toString().toRB()
                    )
                }

                // Log response body (success or error) for diagnostics (only in debug)
                if (BuildConfig.DEBUG) {
                    try {
                        val respText = if (response.isSuccessful) Gson().toJson(response.body()) else response.errorBody()?.string() ?: "(no error body)"
                        Log.d(TAG, "Check-in sync response for uuid=${checkIn.uuid}, code=${response.code()}: $respText")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read check-in response body for uuid=${checkIn.uuid}", e)
                    }
                }

                if (response.isSuccessful) {
                    // Use the server record ID returned directly in the response
                    val serverRecordId = response.body()?.data?.id

                    if (!serverRecordId.isNullOrEmpty()) {
                        checkInDao.markSynced(checkIn.uuid, serverRecordId)
                        syncCheckOutForCheckIn(checkIn.uuid, serverRecordId)
                    } else {
                        // If ID is missing, fallback to enhanced search for backward compatibility or safety
                        val foundId = findServerRecordIdEnhanced(checkIn.date, checkIn.clientTimestamp, checkIn.startTime, checkIn.startKm)
                        if (!foundId.isNullOrEmpty()) {
                            checkInDao.markSynced(checkIn.uuid, foundId)
                            syncCheckOutForCheckIn(checkIn.uuid, foundId)
                        } else {
                            checkInDao.updateSyncingStatus(checkIn.uuid, false)
                            Log.w(TAG, "Check-in uploaded but server record ID not found for uuid=${checkIn.uuid}")
                        }
                    }
                } else {
                    checkInDao.updateSyncingStatus(checkIn.uuid, false)
                    Log.w(TAG, "Check-in sync failed: ${response.code()}")
                }
            } catch (e: Exception) {
                checkInDao.updateSyncingStatus(checkIn.uuid, false)
                Log.e(TAG, "Error syncing check-in ${checkIn.uuid}", e)
            }
        }
    }

    /**
     * Enhanced server record finder (Fallback):
     * - First tries to match by startTime and startKm within a +/-1 day window
     * - If exact match not found, falls back to closest createdAt by clientTimestamp
     */
    private suspend fun findServerRecordIdEnhanced(date: String, clientTimestamp: Long, startTime: String, startKm: String): String? {
        return try {
            val localDate = java.time.LocalDate.parse(date)
            val start = localDate.minusDays(1).toString()
            val end = localDate.plusDays(1).toString()

            val perPage = 100
            val response = authApi.getDriverLogList(startDate = start, endDate = end, page = null, perPage = perPage)
            if (!response.isSuccessful) return null
            val logs = response.body()?.data ?: return null

            // Try exact match first using startTime and startKm
            val exact = logs.find { log ->
                try {
                    // Fix: Use safe calls (?.) because log.driverLog is now nullable
                    // Also check root fields which are often more accurate for segments
                    (log.startTime == startTime && log.startKm == startKm) ||
                    (log.driverLog?.startTime == startTime && log.driverLog.startKm == startKm)
                } catch (_: Exception) {
                    false
                }
            }
            if (exact != null) return exact.id

            // Fallback to closest createdAt
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val best = logs.minByOrNull { log ->
                try {
                    val ts = dateFormat.parse(log.createdAt)?.time ?: Long.MAX_VALUE
                    abs(ts - clientTimestamp)
                } catch (e: Exception) {
                    Long.MAX_VALUE
                }
            }
            best?.id
        } catch (e: Exception) {
            Log.w(TAG, "Error finding server record id", e)
            null
        }
    }

    private suspend fun syncCheckOutForCheckIn(localCheckInUuid: String, serverRecordId: String) {
        val checkOut = checkOutDao.getPendingCheckOutForCheckIn(localCheckInUuid) ?: return
        checkOutDao.updateSyncingStatus(checkOut.uuid, true)
        syncCheckOut(checkOut, serverRecordId)
    }

    private suspend fun syncRemainingCheckOuts() {
        val pending = checkOutDao.getPendingCheckOuts()
        for (checkOut in pending) {
            val recordId = resolveCheckOutRecordId(checkOut) ?: continue
            checkOutDao.updateSyncingStatus(checkOut.uuid, true)
            syncCheckOut(checkOut, recordId)
        }
    }

    private suspend fun resolveCheckOutRecordId(checkOut: OfflineCheckOutEntity): String? {
        checkOut.serverRecordId?.takeIf { it.isNotBlank() }?.let { return it }
        val localUuid = checkOut.localCheckInUuid ?: return null
        return checkInDao.getByUuid(localUuid)?.serverRecordId?.takeIf { it.isNotBlank() }
    }

    private suspend fun syncCheckOut(checkOut: OfflineCheckOutEntity, recordId: String) {
        val uuid = checkOut.uuid
        try {
            val photoFile = OfflineImageHelper.fileFromPath(checkOut.endPhotoPath)
            if (photoFile == null) {
                Log.w(TAG, "Missing end photo for check-out $uuid; keeping record pending")
                checkOutDao.updateSyncingStatus(uuid, false)
                return
            }
            val photoBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("end_photo", photoFile.name, photoBody)

            val response = authApi.checkOutDriverLogSync(
                recordId = recordId.toRB(),
                remark = checkOut.remark.toRB(),
                site = checkOut.site.toRB(),
                purpose = checkOut.purpose.toRB(),
                endTime = checkOut.endTime.toRB(),
                endKm = checkOut.endKm.toRB(),
                endPhoto = photoPart,
                uuid = uuid.toRB(),
                clientTimestamp = checkOut.clientTimestamp.toString().toRB()
            )

            // Log response for diagnostics (only in debug builds)
            if (BuildConfig.DEBUG) {
                try {
                    val respText = if (response.isSuccessful) Gson().toJson(response.body()) else response.errorBody()?.string() ?: "(no error body)"
                    Log.d(TAG, "Check-out sync response for uuid=$uuid, recordId=$recordId, code=${response.code()}: $respText")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read check-out response body for uuid=$uuid", e)
                }
            }

            if (response.isSuccessful) {
                checkOutDao.markSynced(uuid)
            } else {
                checkOutDao.updateSyncingStatus(uuid, false)
                Log.w(TAG, "Check-out sync failed: ${response.code()}")
            }
        } catch (e: Exception) {
            checkOutDao.updateSyncingStatus(uuid, false)
            Log.e(TAG, "Error syncing check-out $uuid", e)
        }
    }

    private suspend fun syncFuelLogs() {
        val pending = fuelLogDao.getPendingFuelLogs()
        for (fuelLog in pending) {
            try {
                fuelLogDao.updateSyncingStatus(fuelLog.uuid, true)
                val filePaths = OfflineImageHelper.jsonToPaths(fuelLog.filesPaths)
                val fileParts = filePaths.mapNotNull { path ->
                    OfflineImageHelper.fileFromPath(path)?.let { file ->
                        val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("files[]", file.name, body)
                    }
                }

                val kmPhotoFile = OfflineImageHelper.fileFromPath(fuelLog.currentKmPhotoPath)
                if (kmPhotoFile == null) {
                    Log.w(TAG, "Missing KM photo for fuel log ${fuelLog.uuid}; keeping record pending")
                    fuelLogDao.updateSyncingStatus(fuelLog.uuid, false)
                    continue
                }
                val kmPhotoBody = kmPhotoFile.asRequestBody("image/*".toMediaTypeOrNull())
                val kmPhotoPart = MultipartBody.Part.createFormData("current_km_photo", kmPhotoFile.name, kmPhotoBody)

                val response = fuelApi.saveFuelLogSync(
                    carPlateNo = fuelLog.carPlateNo.toRB(),
                    date = fuelLog.date.toRB(),
                    fuelCompanyId = fuelLog.fuelCompanyId.toRB(),
                    fuelShop = fuelLog.fuelShop.toRB(),
                    fuelTypeId = fuelLog.fuelTypeId.toRB(),
                    fuelAmount = fuelLog.fuelAmount.toRB(),
                    fuelLiter = fuelLog.fuelLiter.toRB(),
                    files = fileParts,
                    currentKm = fuelLog.currentKm.toRB(),
                    currentKmPhoto = kmPhotoPart,
                    walletBucket = fuelLog.walletBucket.toRB(),
                    uuid = fuelLog.uuid.toRB(),
                    clientTimestamp = fuelLog.clientTimestamp.toString().toRB()
                )

                // Log fuel upload response for diagnostics (debug only)
                if (BuildConfig.DEBUG) {
                    try {
                        val respText = if (response.isSuccessful) Gson().toJson(response.body()) else response.errorBody()?.string() ?: "(no error body)"
                        Log.d(TAG, "Fuel log sync response for uuid=${fuelLog.uuid}, code=${response.code()}: $respText")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read fuel sync response body for uuid=${fuelLog.uuid}", e)
                    }
                }

                if (response.isSuccessful) {
                    val serverRecordId = response.body()?.data?.id
                    if (serverRecordId.isNullOrEmpty()) {
                        Log.w(TAG, "Fuel log uploaded but server record ID was not returned for uuid=${fuelLog.uuid}")
                    }
                    fuelLogDao.markSynced(fuelLog.uuid, serverRecordId)
                }else{
                    fuelLogDao.updateSyncingStatus(fuelLog.uuid, false)
                }
            } catch (e: Exception) {
                fuelLogDao.updateSyncingStatus(fuelLog.uuid, false)
                Log.e(TAG, "Error syncing fuel log ${fuelLog.uuid}", e)
            }
        }
    }

    private suspend fun syncOtherExpenses() {
        val pending = expenseDao.getPendingExpenses()
        for (expense in pending) {
            try {
                expenseDao.updateSyncingStatus(expense.uuid, true)
                val filePaths = OfflineImageHelper.jsonToPaths(expense.filesPaths)
                val fileParts = filePaths.mapNotNull { path ->
                    OfflineImageHelper.fileFromPath(path)?.let { file ->
                        val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("files[]", file.name, body)
                    }
                }

                val response = authApi.saveOtherExpenseSync(
                    date = expense.date.toRB(),
                    typeOfCostId = expense.typeOfCostId.toRB(),
                    amount = expense.amount.toRB(),
                    licensePlate = expense.licensePlate.toRB(),
                    files = fileParts,
                    uuid = expense.uuid.toRB(),
                    clientTimestamp = expense.clientTimestamp.toString().toRB()
                )

                // Log expense upload response for diagnostics (debug only)
                if (BuildConfig.DEBUG) {
                    try {
                        val respText = if (response.isSuccessful) Gson().toJson(response.body()) else response.errorBody()?.string() ?: "(no error body)"
                        Log.d(TAG, "Expense sync response for uuid=${expense.uuid}, code=${response.code()}: $respText")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read expense sync response body for uuid=${expense.uuid}", e)
                    }
                }

                if (response.isSuccessful) {
                    val serverRecordId = response.body()?.data?.id
                    if (serverRecordId.isNullOrEmpty()) {
                        Log.w(TAG, "Expense uploaded but server record ID was not returned for uuid=${expense.uuid}")
                    }
                    expenseDao.markSynced(expense.uuid, serverRecordId)
                }else{
                    expenseDao.updateSyncingStatus(expense.uuid, false)
                }
            } catch (e: Exception) {
                expenseDao.updateSyncingStatus(expense.uuid, false)
                Log.e(TAG, "Error syncing expense ${expense.uuid}", e)
            }
        }
    }

    private fun String.toRB() = toRequestBody("text/plain".toMediaTypeOrNull())
}
