package com.kero.anbu.core.network.di

import com.kero.anbu.core.datastore.TokenDataStore
import com.kero.anbu.core.network.ApiService
import com.kero.anbu.core.network.FakeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(tokenDataStore: TokenDataStore): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = runBlocking { tokenDataStore.getDeviceAccessToken() }
                val request = if (token.isNullOrBlank()) {
                    chain.request()
                } else {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                }
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideApiService(fakeApiService: FakeApiService): ApiService =
        fakeApiService
}
