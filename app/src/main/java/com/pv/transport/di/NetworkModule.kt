package com.pv.transport.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.network.NetworkExceptionInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import com.pv.transport.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.nerdythings.okhttp.profiler.OkHttpProfilerInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @Named("base_url")
    fun providesBaseUrl(): String = "https://uat.pvmyanmar.com/api/v1/"

    @Provides
    @Singleton
    fun providesSharePrefUtils(@ApplicationContext context: Context) = // ✅ ADD @ApplicationContext
        AuthPrefs(context)

    @Provides
    @Named("bearer_token")
    fun providesBearerToken(sharePrefUtils: AuthPrefs) =
        sharePrefUtils.load(AuthPrefs.KEYS.ACCESS_TOKEN)

    @Suppress("DEPRECATION")
    @Provides
    @Named("primary")
    fun providesPrimaryRetrofitBuilder(
        gson: Gson,
        @Named("base_url") baseUrl: String
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(SimpleXmlConverterFactory.create())
    }

    @Suppress("DEPRECATION")
    @Provides
    @Named("auth")
    fun providesAuthRetrofitBuilder(
        gson: Gson,
        @Named("base_url") baseUrl: String
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(SimpleXmlConverterFactory.create())
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object Providers {
        @Singleton
        @Provides
        @Named("okhttp")
        fun providesOkHttpClientBuilder(
            @ApplicationContext context: Context // ✅ ADD @ApplicationContext
        ): OkHttpClient.Builder {
            return OkHttpClient.Builder().apply {
                val loggerInterceptor = HttpLoggingInterceptor().apply {
                    level = when (BuildConfig.DEBUG) {
                        true -> HttpLoggingInterceptor.Level.HEADERS
                        false -> HttpLoggingInterceptor.Level.NONE
                    }
                }

                addInterceptor(loggerInterceptor)
                    .addInterceptor(NetworkExceptionInterceptor())
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .addInterceptor(OkHttpProfilerInterceptor())
                    .readTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .cache(null)
            }
        }

        @Singleton
        @Provides
        @Named("authOkhttp")
        fun providesAuthOkHttpClientBuilder(
            @ApplicationContext context: Context // ✅ ADD @ApplicationContext
        ): OkHttpClient.Builder {
            return OkHttpClient.Builder().apply {
                val loggerInterceptor = HttpLoggingInterceptor().apply {
                    level = when (BuildConfig.DEBUG) {
                        true -> HttpLoggingInterceptor.Level.HEADERS
                        false -> HttpLoggingInterceptor.Level.NONE
                    }
                }
                addInterceptor(loggerInterceptor)
                    .addInterceptor(
                        ChuckerInterceptor.Builder(context)
                            .alwaysReadResponseBody(false)
                            .build()
                    )
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .cache(null)
            }
        }
    }

    @Provides
    @Singleton
    fun gson(): Gson = GsonBuilder()
        .setLenient()
        .create()
}