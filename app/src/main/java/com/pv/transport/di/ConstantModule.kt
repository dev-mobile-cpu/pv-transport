package com.pv.transport.di

import com.pv.transport.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ConstantModule {
    init {
        val libPath = System.getProperty("java.library.path")
        println("java.library.path=$libPath")

        val libraryName = "native-lib"
        println("Trying to load '$libraryName'")

        System.loadLibrary(libraryName)
    }

    private external fun getBaseURL(): String?

    private external fun getBaseStagingURL(): String?


}