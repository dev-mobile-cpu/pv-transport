package com.pv.transport.di

import android.content.Context
import com.pv.transport.local.dao.CorporateUserCacheDao
import com.pv.transport.local.dao.CostTypeCacheDao
import com.pv.transport.local.dao.DriverLogCacheDao
import com.pv.transport.local.dao.FuelCompanyCacheDao
import com.pv.transport.local.dao.FuelLogCacheDao
import com.pv.transport.local.dao.FuelTypeCacheDao
import com.pv.transport.local.dao.OfflineCheckInDao
import com.pv.transport.local.dao.OfflineCheckOutDao
import com.pv.transport.local.dao.OfflineFuelLogDao
import com.pv.transport.local.dao.OfflineOtherExpenseDao
import com.pv.transport.local.dao.OtherExpenseCacheDao
import com.pv.transport.local.dao.ReasonCacheDao
import com.pv.transport.local.dao.TripTypeCacheDao
import com.pv.transport.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    @Provides
    fun provideOfflineCheckInDao(db: AppDatabase): OfflineCheckInDao = db.offlineCheckInDao()

    @Provides
    fun provideOfflineCheckOutDao(db: AppDatabase): OfflineCheckOutDao = db.offlineCheckOutDao()

    @Provides
    fun provideOfflineFuelLogDao(db: AppDatabase): OfflineFuelLogDao = db.offlineFuelLogDao()

    @Provides
    fun provideOfflineOtherExpenseDao(db: AppDatabase): OfflineOtherExpenseDao = db.offlineOtherExpenseDao()

    @Provides
    fun provideReasonCacheDao(db: AppDatabase): ReasonCacheDao = db.reasonCacheDao()

    @Provides
    fun provideTripTypeCacheDao(db: AppDatabase): TripTypeCacheDao = db.tripTypeCacheDao()

    @Provides
    fun provideFuelTypeCacheDao(db: AppDatabase): FuelTypeCacheDao = db.fuelTypeCacheDao()

    @Provides
    fun provideCostTypeCacheDao(db: AppDatabase): CostTypeCacheDao = db.costTypeCacheDao()

    @Provides
    fun provideDriverLogCacheDao(db: AppDatabase): DriverLogCacheDao = db.driverLogCacheDao()

    @Provides
    fun provideCorporateUserCacheDao(db: AppDatabase): CorporateUserCacheDao = db.corporateUserCacheDao()

    @Provides
    fun provideFuelCompanyCacheDao(db: AppDatabase): FuelCompanyCacheDao = db.fuelCompanyCacheDao()
    @Provides
    fun provideFuelLogCacheDao(db: AppDatabase): FuelLogCacheDao = db.fuelLogCacheDao()

    @Provides
    fun provideOtherExpenseCacheDao(db: AppDatabase): OtherExpenseCacheDao = db.otherExpenseCacheDao()
}

