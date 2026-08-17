package com.pv.transport.di

import com.pv.transport.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/** Environment URLs from the uat / production product flavor via BuildConfig. */
@Module
@InstallIn(SingletonComponent::class)
object ConstantModule {

    @Provides
    @Singleton
    @Named("base_url")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL
}
