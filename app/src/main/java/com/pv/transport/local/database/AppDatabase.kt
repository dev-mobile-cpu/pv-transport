package com.pv.transport.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pv.transport.local.dao.CostTypeCacheDao
import com.pv.transport.local.dao.DriverLogCacheDao
import com.pv.transport.local.dao.FuelTypeCacheDao
import com.pv.transport.local.dao.OfflineCheckInDao
import com.pv.transport.local.dao.OfflineCheckOutDao
import com.pv.transport.local.dao.OfflineFuelLogDao
import com.pv.transport.local.dao.OfflineOtherExpenseDao
import com.pv.transport.local.dao.ReasonCacheDao
import com.pv.transport.local.dao.TripTypeCacheDao
import com.pv.transport.local.data.CostTypeCacheEntity
import com.pv.transport.local.data.DataConverters
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.DriverLogEntity
import com.pv.transport.local.data.FuelTypeCacheEntity
import com.pv.transport.local.data.OfflineCheckInEntity
import com.pv.transport.local.data.OfflineCheckOutEntity
import com.pv.transport.local.data.OfflineFuelLogEntity
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.local.data.ReasonCacheEntity
import com.pv.transport.local.data.TripTypeCacheEntity

@Database(
    entities = [
        DriverLogEntity::class,
        DriverLogCacheEntity::class,
        OfflineCheckInEntity::class,
        OfflineCheckOutEntity::class,
        OfflineFuelLogEntity::class,
        OfflineOtherExpenseEntity::class,
        ReasonCacheEntity::class,
        TripTypeCacheEntity::class,
        FuelTypeCacheEntity::class,
        CostTypeCacheEntity::class
    ],
    version = 4, // Increased version because we added isSyncing field to entities
    exportSchema = false
)
@TypeConverters(DataConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offlineCheckInDao(): OfflineCheckInDao
    abstract fun offlineCheckOutDao(): OfflineCheckOutDao
    abstract fun offlineFuelLogDao(): OfflineFuelLogDao
    abstract fun offlineOtherExpenseDao(): OfflineOtherExpenseDao
    abstract fun reasonCacheDao(): ReasonCacheDao
    abstract fun tripTypeCacheDao(): TripTypeCacheDao
    abstract fun fuelTypeCacheDao(): FuelTypeCacheDao
    abstract fun costTypeCacheDao(): CostTypeCacheDao
    abstract fun driverLogCacheDao(): DriverLogCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
