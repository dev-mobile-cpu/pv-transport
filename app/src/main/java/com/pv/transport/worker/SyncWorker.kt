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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

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
    private val expenseDao: OfflineOtherExpenseDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncCheckInsSequentially()
            syncRemainingCheckOuts()
            syncFuelLogs()
            syncOtherExpenses()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.retry()
        }
    }

    private suspend fun syncCheckInsSequentially() {
        val pendingCheckIns = checkInDao.getPendingCheckIns()
        for (checkIn in pendingCheckIns) {
            try {
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

                if (response.isSuccessful) {
                    // Fetch server logs for this date to retrieve the record_id
                    val serverRecordId = findServerRecordId(checkIn.date, checkIn.clientTimestamp)
                    checkInDao.markSynced(checkIn.uuid, serverRecordId ?: "")
                    Log.d(TAG, "Check-in synced: ${checkIn.uuid}, serverId=$serverRecordId")

                    // Sync the associated checkout if one was saved while offline
                    if (!serverRecordId.isNullOrEmpty()) {
                        syncCheckOutForCheckIn(checkIn.uuid, serverRecordId)
                    }
                } else {
                    Log.w(TAG, "Check-in sync failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing check-in ${checkIn.uuid}", e)
            }
        }
    }

    /**
     * Fetch driver logs for the given date and find the record whose created_at
     * is closest to clientTimestamp. Returns its id (the record_id needed for checkout).
     */
    private suspend fun findServerRecordId(date: String, clientTimestamp: Long): String? {
        return try {
            val response = authApi.getDriverLogList(
                startDate = date,
                endDate = date,
                page = null,
                perPage = 50
            )
            if (!response.isSuccessful) return null
            val logs = response.body()?.data ?: return null

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
            Log.e(TAG, "Could not fetch logs to find record_id", e)
            null
        }
    }

    private suspend fun syncCheckOutForCheckIn(localCheckInUuid: String, serverRecordId: String) {
        val checkOut = checkOutDao.getPendingCheckOutForCheckIn(localCheckInUuid) ?: return
        syncCheckOut(checkOut.uuid, serverRecordId, checkOut.remark, checkOut.endTime, checkOut.endKm, checkOut.endPhotoPath, checkOut.clientTimestamp)
    }

    private suspend fun syncRemainingCheckOuts() {
        val pending = checkOutDao.getPendingCheckOuts()
        for (checkOut in pending) {
            // Checkouts linked to a local check-in are handled in syncCheckInsSequentially
            if (checkOut.localCheckInUuid != null) continue
            val recordId = checkOut.serverRecordId ?: continue
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

            if (response.isSuccessful) {
                checkOutDao.markSynced(uuid)
                Log.d(TAG, "Check-out synced: $uuid")
            } else {
                Log.w(TAG, "Check-out sync failed: ${response.code()}")
            }
        } catch (e: Exception) {
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

                if (response.isSuccessful) {
                    fuelLogDao.markSynced(fuelLog.uuid)
                    Log.d(TAG, "Fuel log synced: ${fuelLog.uuid}")
                } else {
                    Log.w(TAG, "Fuel log sync failed: ${response.code()}")
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

                if (response.isSuccessful) {
                    expenseDao.markSynced(expense.uuid)
                    Log.d(TAG, "Expense synced: ${expense.uuid}")
                } else {
                    Log.w(TAG, "Expense sync failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing expense ${expense.uuid}", e)
            }
        }
    }

    private fun String.toRB() = toRequestBody("text/plain".toMediaTypeOrNull())
}
