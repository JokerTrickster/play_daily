package com.dailymemo.di

import android.content.Context
import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.data.datasources.remote.AuthRemoteDataSource
import com.dailymemo.data.datasources.remote.api.AuthApiService
import com.dailymemo.data.repositories.AuthRepositoryImpl
import com.dailymemo.domain.repositories.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2:7000"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMemoApiService(retrofit: Retrofit): com.dailymemo.data.datasources.remote.api.MemoApiService {
        return retrofit.create(com.dailymemo.data.datasources.remote.api.MemoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthLocalDataSource(
        @ApplicationContext context: Context
    ): AuthLocalDataSource {
        return AuthLocalDataSource(context)
    }

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        authApiService: AuthApiService
    ): AuthRemoteDataSource {
        return AuthRemoteDataSource(authApiService)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        localDataSource: AuthLocalDataSource,
        remoteDataSource: AuthRemoteDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(localDataSource, remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideMemoRepository(
        memoApiService: com.dailymemo.data.datasources.remote.api.MemoApiService
    ): com.dailymemo.domain.repositories.MemoRepository {
        return com.dailymemo.data.repositories.MemoRepositoryImpl(memoApiService)
    }
}
