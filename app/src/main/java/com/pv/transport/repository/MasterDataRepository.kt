package com.pv.transport.repository

import android.content.Context
import android.util.Log
import com.pv.transport.api.AuthApi
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.CostType
import com.pv.transport.data.fuel.FuelCompany
import com.pv.transport.data.fuel.FuelType
import com.pv.transport.data.log.ReasonListResponse
import com.pv.transport.data.log.TripType
import com.pv.transport.data.master.InitialData
import com.pv.transport.local.dao.CostTypeCacheDao
import com.pv.transport.local.dao.FuelCompanyCacheDao
import com.pv.transport.local.dao.FuelTypeCacheDao
import com.pv.transport.local.dao.ReasonCacheDao
import com.pv.transport.local.dao.TripTypeCacheDao
import com.pv.transport.local.data.CostTypeCacheEntity
import com.pv.transport.local.data.FuelCompanyCacheEntity
import com.pv.transport.local.data.FuelTypeCacheEntity
import com.pv.transport.local.data.ReasonCacheEntity
import com.pv.transport.local.data.TripTypeCacheEntity
import com.pv.transport.network.NetworkUtils
import com.pv.transport.util.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the five master data lists (reasons, cost types, trip types, fuel types, fuel companies).
 *
 * Forms always read from Room. The only way the lists change is [syncInitialData], which calls
 * `driver/get_initial_data` once instead of the five old per-list endpoints.
 */
@Singleton
class MasterDataRepository @Inject constructor(
    private val api: AuthApi,
    @ApplicationContext private val context: Context,
    private val authPrefs: AuthPrefs,
    private val reasonCacheDao: ReasonCacheDao,
    private val tripTypeCacheDao: TripTypeCacheDao,
    private val costTypeCacheDao: CostTypeCacheDao,
    private val fuelTypeCacheDao: FuelTypeCacheDao,
    private val fuelCompanyCacheDao: FuelCompanyCacheDao
) {

    private val syncMutex = Mutex()

    // ── Local reads ───────────────────────────────────────────────────────────

    suspend fun getReasons(): List<ReasonListResponse> {
        val synced = ensureInitialData()
        val list = reasonCacheDao.getAll().map { ReasonListResponse(it.id, it.value) }
        failIfUnavailable(list.isEmpty(), synced)
        return list
    }

    suspend fun getTripTypes(): List<TripType> {
        val synced = ensureInitialData()
        val list = tripTypeCacheDao.getAll().map { TripType(it.id, it.value) }
        failIfUnavailable(list.isEmpty(), synced)
        return list
    }

    suspend fun getCostTypes(): List<CostType> {
        val synced = ensureInitialData()
        val list = costTypeCacheDao.getAll().map { CostType(it.id, it.name) }
        failIfUnavailable(list.isEmpty(), synced)
        return list
    }

    suspend fun getFuelTypes(): List<FuelType> {
        val synced = ensureInitialData()
        val list = fuelTypeCacheDao.getAll().map { FuelType(it.id, it.name) }
        failIfUnavailable(list.isEmpty(), synced)
        return list
    }

    suspend fun getFuelCompanies(): List<FuelCompany> {
        val synced = ensureInitialData()
        val list = fuelCompanyCacheDao.getAll()
            .map { FuelCompany(it.id, it.name, it.phone, it.email, it.address) }
        failIfUnavailable(list.isEmpty(), synced)
        return list
    }

    /** No cache and the download failed too -> surface a real error instead of an empty dropdown. */
    private fun failIfUnavailable(listIsEmpty: Boolean, synced: Boolean) {
        if (listIsEmpty && !synced) {
            throw Exception("Could not load data. Please check your connection and try again.")
        }
    }

    // ── Sync ──────────────────────────────────────────────────────────────────

    /**
     * Downloads the master data only when nothing usable is stored yet, so opening a form never
     * hits the network on its own. Returns whether usable data exists afterwards.
     */
    suspend fun ensureInitialData(): Boolean {
        if (!hasLocalData()) {
            return syncInitialData()
        }
        return true
    }

    /**
     * Without a stored `update` this asks for everything; with one it asks for changes only and
     * leaves the local lists alone when the server replies with the timestamp by itself.
     *
     * Returns whether the app is left with usable master data.
     */
    suspend fun syncInitialData(): Boolean = syncMutex.withLock { downloadInitialData() }

    /**
     * Cold start and login are the only other sync points, so an app that is simply left open
     * would otherwise keep yesterday's lists forever. Offline or failed downloads are silent —
     * the cached lists stay in use.
     */
    suspend fun refreshIfStale(): Boolean = syncMutex.withLock {
        val elapsed = System.currentTimeMillis() - authPrefs.getInitialDataSyncedAt()
        if (elapsed in 0 until REFRESH_INTERVAL_MS) {
            return@withLock true
        }
        downloadInitialData()
    }

    private suspend fun downloadInitialData(): Boolean {
        val storedUpdate = authPrefs.getInitialDataUpdate()
        val hasLocalData = storedUpdate != null && hasCachedLists()

        if (!NetworkUtils.isInternetAvailable(context)) {
            return hasLocalData
        }

        val since = if (hasLocalData) storedUpdate else null

        return try {
            val response = api.getInitialData(since)
            val payload = response.body()?.data

            if (!response.isSuccessful || payload == null) {
                Log.w(TAG, "get_initial_data failed with code ${response.code()}")
                return hasLocalData
            }

            if (payload.hasLists) {
                replaceCachedLists(payload)
            }
            DebugLog.d(
                TAG,
                "initial data ${if (payload.hasLists) "replaced" else "unchanged"}, " +
                        "since=$since update=${payload.update}"
            )
            authPrefs.saveInitialDataSync(payload.update, cachedRowCount())
            true
        } catch (e: Exception) {
            Log.e(TAG, "get_initial_data failed", e)
            hasLocalData
        }
    }

    private suspend fun hasLocalData(): Boolean =
        authPrefs.getInitialDataUpdate() != null && hasCachedLists()

    /**
     * A stored `update` with every table empty usually means the database was wiped behind our
     * back, so the timestamp alone is not enough to trust the cache. A server that genuinely has
     * nothing to send is the exception: the last sync then recorded a row count of zero.
     */
    private suspend fun hasCachedLists(): Boolean =
        cachedRowCount() > 0 || authPrefs.getInitialDataRowCount() == 0

    private suspend fun cachedRowCount(): Int =
        reasonCacheDao.getAll().size +
                tripTypeCacheDao.getAll().size +
                costTypeCacheDao.getAll().size +
                fuelTypeCacheDao.getAll().size +
                fuelCompanyCacheDao.getAll().size

    /** Each list that came back replaces its table wholesale so deleted rows disappear too. */
    private suspend fun replaceCachedLists(payload: InitialData) {
        payload.reasons?.let { reasons ->
            reasonCacheDao.clear()
            reasonCacheDao.insertAll(
                reasons.mapNotNull { reason ->
                    val id = reason.id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    ReasonCacheEntity(id, reason.value.orEmpty())
                }
            )
        }

        payload.tripTypes?.let { tripTypes ->
            tripTypeCacheDao.clear()
            tripTypeCacheDao.insertAll(
                tripTypes.mapNotNull { tripType ->
                    val id = tripType.id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    TripTypeCacheEntity(id, tripType.value.orEmpty())
                }
            )
        }

        payload.typeOfCosts?.let { costTypes ->
            costTypeCacheDao.clear()
            costTypeCacheDao.insertAll(
                costTypes.mapNotNull { costType ->
                    val id = costType.id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    CostTypeCacheEntity(id, costType.name.orEmpty())
                }
            )
        }

        payload.fuelTypes?.let { fuelTypes ->
            fuelTypeCacheDao.clear()
            fuelTypeCacheDao.insertAll(
                fuelTypes.mapNotNull { fuelType ->
                    val id = fuelType.id?.toIntOrNull() ?: return@mapNotNull null
                    FuelTypeCacheEntity(id, fuelType.name.orEmpty())
                }
            )
        }

        payload.fuelCompanies?.let { companies ->
            fuelCompanyCacheDao.clear()
            fuelCompanyCacheDao.insertAll(
                companies.mapNotNull { company ->
                    val id = company.id?.toIntOrNull() ?: return@mapNotNull null
                    FuelCompanyCacheEntity(
                        id = id,
                        name = company.name.orEmpty(),
                        phone = company.phone.orEmpty(),
                        email = company.email.orEmpty(),
                        address = company.address.orEmpty()
                    )
                }
            )
        }
    }

    private companion object {
        const val TAG = "MasterDataRepository"

        /** How long a downloaded copy of the master data is treated as fresh. */
        val REFRESH_INTERVAL_MS: Long = TimeUnit.HOURS.toMillis(12)
    }
}
