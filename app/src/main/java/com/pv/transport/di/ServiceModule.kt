package com.pv.transport.di

import androidx.annotation.NonNull
import com.pv.transport.api.AuthApi
import com.pv.transport.api.AuthenticationService
import com.pv.transport.api.FuelApi
import com.pv.transport.auth.AuthPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Singleton
    @NonNull
    @Provides
    fun provideAuthenticationService(@Named("refreshTokenBuilder") retrofitBuilder: Retrofit.Builder): AuthenticationService {
        return retrofitBuilder.build().create(AuthenticationService::class.java)
    }

    @Singleton
    @Provides
    fun provideLoginService(@Named("authenticatedBuilder") retrofitBuilder: Retrofit.Builder): AuthApi {
        return retrofitBuilder.build().create(AuthApi::class.java)
    }

    @Singleton
    @Provides
    fun provideHomeService(@Named("authenticatedBuilder") retrofitBuilder: Retrofit.Builder): FuelApi {
        return retrofitBuilder.build().create(FuelApi::class.java)
    }


    @Singleton
    @NonNull
    @Named("authenticatedBuilder")
    @Provides
    fun getAuthenticatedBuilder(
        @Named("okhttp") httpClientBuilder: OkHttpClient.Builder,
        @Named("primary") retrofitBuilder: Retrofit.Builder,
        preference: AuthPrefs
    ): Retrofit.Builder {
        val interceptor: Interceptor =
            AuthenticationInterceptor( preference)
        if (!httpClientBuilder.interceptors().contains(interceptor)) {
            httpClientBuilder.addInterceptor(interceptor)
        }
        return retrofitBuilder.client(httpClientBuilder.build())

    }

    @Singleton
    @NonNull
    @Named("refreshTokenBuilder")
    @Provides
    fun getRefreshTokenBuilder(
        @Named("authOkhttp") httpClientBuilder: OkHttpClient.Builder,
        @Named("auth") retrofitBuilder: Retrofit.Builder,
        preference: AuthPrefs
    ): Retrofit.Builder {
        val interceptor: Interceptor =
            RefreshTokenInterceptor(preference)
        if (!httpClientBuilder.interceptors().contains(interceptor)) {
            httpClientBuilder.addInterceptor(interceptor)
        }
        return retrofitBuilder.client(httpClientBuilder.build())
    }
}