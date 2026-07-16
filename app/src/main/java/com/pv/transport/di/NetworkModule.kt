package com.pv.transport.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.network.NetworkConnectionInterceptor
import com.pv.transport.network.NetworkConnectivityObserver
import com.pv.transport.network.NetworkExceptionInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.nerdythings.okhttp.profiler.BuildConfig
import io.nerdythings.okhttp.profiler.OkHttpProfilerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @Named("base_url")
    fun providesBaseUrl(): String = "https://uat.pvmyanmar.com/api/v1/"

    @Provides
    @Singleton
    fun providesSharePrefUtils(@ApplicationContext context: Context): AuthPrefs =
        AuthPrefs(context)

    @Provides
    @Named("bearer_token")
    fun providesBearerToken(sharePrefUtils: AuthPrefs): String? =
        sharePrefUtils.load(AuthPrefs.KEYS.ACCESS_TOKEN)

    @Provides
    @Singleton
    fun gson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver =
        NetworkConnectivityObserver(context)

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
    }

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
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object Providers {
        @Provides
        @Named("okhttp")
        fun providesOkHttpClientBuilder(
            @ApplicationContext context: Context
        ): OkHttpClient.Builder {
            return OkHttpClient.Builder().apply {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.NONE
                }

                addInterceptor(loggingInterceptor)
                    //.addInterceptor(NetworkExceptionInterceptor())
                    .addInterceptor(NetworkConnectionInterceptor(context))
                    .addInterceptor(OkHttpProfilerInterceptor())
                    .readTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .cache(null)
            }
        }

        @Provides
        @Named("authOkhttp")
        fun providesAuthOkHttpClientBuilder(
            @ApplicationContext context: Context
        ): OkHttpClient.Builder {
            return OkHttpClient.Builder().apply {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.NONE
                }

                addInterceptor(loggingInterceptor)
                    .addInterceptor(
                        ChuckerInterceptor.Builder(context)
                            .alwaysReadResponseBody(false)
                            .build()
                    )
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .cache(null)
            }
        }
    }
}
