package com.pv.transport.repository

import com.pv.transport.data.SessionEvents
import com.pv.transport.local.dao.CorporateUserCacheDao
import com.pv.transport.local.dao.DriverLogCacheDao
import com.pv.transport.local.dao.FuelLogCacheDao
import com.pv.transport.local.dao.OfflineCheckInDao
import com.pv.transport.local.dao.OfflineCheckOutDao
import com.pv.transport.local.dao.OfflineFuelLogDao
import com.pv.transport.local.dao.OfflineOtherExpenseDao
import com.pv.transport.local.dao.OtherExpenseCacheDao
import com.pv.transport.util.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCacheCleaner @Inject constructor(
    private val driverLogCacheDao: DriverLogCacheDao,
    private val fuelLogCacheDao: FuelLogCacheDao,
    private val otherExpenseCacheDao: OtherExpenseCacheDao,
    private val corporateUserCacheDao: CorporateUserCacheDao,
    private val checkInDao: OfflineCheckInDao,
    private val checkOutDao: OfflineCheckOutDao,
    private val fuelLogDao: OfflineFuelLogDao,
    private val expenseDao: OfflineOtherExpenseDao
) {
    suspend fun clearServerCaches() {
        clearServerCachesInternal()
        SessionEvents.triggerSessionDataCleared()
    }

    suspend fun clearAll() {
        clearServerCachesInternal()
        withContext(Dispatchers.IO) {
            clearStep("offline check-ins") { checkInDao.deleteAll() }
            clearStep("offline check-outs") { checkOutDao.deleteAll() }
            clearStep("offline fuel logs") { fuelLogDao.deleteAll() }
            clearStep("offline expenses") { expenseDao.deleteAll() }
        }
        SessionEvents.triggerSessionDataCleared()
    }

    private suspend fun clearServerCachesInternal() {
        withContext(Dispatchers.IO) {
            clearStep("driver log cache") { driverLogCacheDao.clearCache() }
            clearStep("fuel log cache") { fuelLogCacheDao.clearCache() }
            clearStep("other expense cache") { otherExpenseCacheDao.clearCache() }
            clearStep("corporate user cache") { corporateUserCacheDao.clear() }
        }
    }

    private suspend fun clearStep(name: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            DebugLog.w(TAG, "Failed to clear $name", e)
        }
    }

    private companion object {
        const val TAG = "SessionCacheCleaner"
    }
}
