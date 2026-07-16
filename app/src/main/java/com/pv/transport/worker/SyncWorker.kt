package com.pv.transport.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pv.transport.R
import com.pv.transport.api.AuthApi
import com.pv.transport.api.FuelApi
import com.pv.transport.local.dao.OfflineCheckInDao
import com.pv.transport.local.dao.OfflineCheckOutDao
import com.pv.transport.local.dao.OfflineFuelLogDao
import com.pv.transport.local.dao.OfflineOtherExpenseDao
import com.pv.transport.offline.OfflineImageHelper
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
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.FuelLogCacheEntity
import java.time.LocalDate

private const val TAG = "SyncWorker"

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
    private val fuelLogCacheDao: FuelLogCacheDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Reset all syncing status first in case of previous crash
            checkInDao.resetSyncingStatus()
            checkOutDao.resetSyncingStatus()

            val pendingInCount = checkInDao.getPendingCheckIns().size
            val pendingOutCount = checkOutDao.getPendingCheckOuts().size
            val pendingFuelCount = fuelLogDao.getPendingFuelLogs().size
            val pendingExpenseCount = expenseDao.getPendingExpenses().size
            Log.d(TAG, "Pending offline counts - checkIns: $pendingInCount, checkOuts: $pendingOutCount, fuel: $pendingFuelCount, expenses: $pendingExpenseCount")

            val hasDataToSync = hasPendingData()

            if (hasDataToSync) {
                syncCheckInsSequentially()
                Log.d(TAG, "Finished syncing check-ins")
                syncRemainingCheckOuts()
                Log.d(TAG, "Finished syncing remaining check-outs")
                syncFuelLogs()
                Log.d(TAG, "Finished syncing fuel logs")
                syncOtherExpenses()
                Log.d(TAG, "Finished syncing other expenses")
                // Cleanup local synced rows to prevent offline cards from persisting
                try {
                    checkInDao.deleteSynced()
                    checkOutDao.deleteSynced()
                    fuelLogDao.deleteSynced()
                    expenseDao.deleteSynced()
                    Log.d(TAG, "Deleted synced offline rows from local DB")

                    // Refresh driver log cache so UI shows up-to-date server data
                    refreshDriverLogCache()
                    refreshFuelLogCache()

                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete synced offline rows", e)
                }

                Log.d(TAG, "Sync completed: uploaded pending offline data")
                showSyncNotification("Sync Completed", "All offline data has been uploaded successfully.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            checkInDao.resetSyncingStatus()
            checkOutDao.resetSyncingStatus()
            Result.retry()
        }
    }


    private suspend fun refreshDriverLogCache() {
        try {
            // val start = java.time.LocalDate.now().minusDays(7).toString()
            val start = LocalDate.now().toString()
            val end = LocalDate.now().toString()
            val resp = authApi.getDriverLogList(startDate = start, endDate = end, page = null, perPage = 50)
            if (resp.isSuccessful) {
                resp.body()?.data?.let { list ->
                    driverLogCacheDao.insertCache(DriverLogCacheEntity(logs = list))
                    Log.d(TAG, "Refreshed driver log cache with ${list.size} records")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh driver log cache", e)
        }
    }
    private suspend fun refreshFuelLogCache(){

        try {
            val start =  LocalDate.now().toString()
            val end = LocalDate.now().toString()
            val response = fuelApi.getFuelLogs(startDate = start, endDate = end, page = null, perPage = 50)

            if(response.isSuccessful){
                val logs = response.body()?.data ?: emptyList()
                fuelLogCacheDao.insertCache(FuelLogCacheEntity(logs = logs))
                Log.d(TAG, "Fuel cache updated ${logs.size}")
            }

        }catch(e:Exception){

            Log.e(TAG, "Fuel cache refresh error", e)
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
                val photoFile = OfflineImageHelper.fileFromPath(checkIn.startPhotoPath) ?: continue
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
        syncCheckOut(checkOut.uuid, serverRecordId, checkOut.remark, checkOut.endTime, checkOut.endKm, checkOut.endPhotoPath, checkOut.clientTimestamp)
    }

    private suspend fun syncRemainingCheckOuts() {
        val pending = checkOutDao.getPendingCheckOuts()
        for (checkOut in pending) {
            if (checkOut.localCheckInUuid != null) continue
            val recordId = checkOut.serverRecordId ?: continue
            checkOutDao.updateSyncingStatus(checkOut.uuid, true)
            syncCheckOut(checkOut.uuid, recordId, checkOut.remark, checkOut.endTime, checkOut.endKm, checkOut.endPhotoPath, checkOut.clientTimestamp)
        }
    }

    private suspend fun syncCheckOut(uuid: String, recordId: String, remark: String, endTime: String, endKm: String, endPhotoPath: String, clientTimestamp: Long) {
        try {
            val photoFile = OfflineImageHelper.fileFromPath(endPhotoPath) ?: return
            val photoBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("end_photo", photoFile.name, photoBody)

            val response = authApi.checkOutDriverLogSync(
                recordId = recordId.toRB(),
                remark = remark.toRB(),
                endTime = endTime.toRB(),
                endKm = endKm.toRB(),
                endPhoto = photoPart,
                uuid = uuid.toRB(),
                clientTimestamp = clientTimestamp.toString().toRB()
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
                val filePaths = OfflineImageHelper.jsonToPaths(fuelLog.filesPaths)
                val fileParts = filePaths.mapNotNull { path ->
                    OfflineImageHelper.fileFromPath(path)?.let { file ->
                        val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("files[]", file.name, body)
                    }
                }

                val kmPhotoFile = OfflineImageHelper.fileFromPath(fuelLog.currentKmPhotoPath) ?: continue
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
                    fuelLogDao.markSynced(fuelLog.uuid)
                }else{
                    fuelLogDao.updateSyncingStatus(fuelLog.uuid, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing fuel log ${fuelLog.uuid}", e)
            }
        }
    }

    private suspend fun syncOtherExpenses() {
        val pending = expenseDao.getPendingExpenses()
        for (expense in pending) {
            try {
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
                    expenseDao.markSynced(expense.uuid)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing expense ${expense.uuid}", e)
            }
        }
    }

    private fun showSyncNotification(title: String, message: String) {
        val channelId = "sync_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Data Sync", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this icon exists
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun String.toRB() = toRequestBody("text/plain".toMediaTypeOrNull())
}
