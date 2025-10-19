package com.dailymemo.di

import android.content.Context
import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.data.datasources.local.LocationDataSource
import com.dailymemo.data.datasources.remote.AuthRemoteDataSource
import com.dailymemo.data.datasources.remote.api.AuthApiService
import com.dailymemo.data.repositories.AuthRepositoryImpl
import com.dailymemo.data.repositories.LocationRepositoryImpl
import com.dailymemo.domain.repositories.AuthRepository
import com.dailymemo.domain.repositories.LocationRepository
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

    // 에뮬레이터: http://10.0.2.2:7001
    // 실제 기기: Mac의 IP 주소 사용
    private const val BASE_URL = "http://192.168.0.10:7001"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authLocalDataSource: AuthLocalDataSource
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = okhttp3.Interceptor { chain ->
            val originalRequest = chain.request()

            // Auth API는 토큰 없이 요청
            if (originalRequest.url.encodedPath.contains("/auth/")) {
                return@Interceptor chain.proceed(originalRequest)
            }

            // 다른 API는 토큰 추가
            val token = authLocalDataSource.getAccessTokenSync()
            if (token != null) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
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

    @Provides
    @Singleton
    fun provideLocationDataSource(
        @ApplicationContext context: Context
    ): LocationDataSource {
        return LocationDataSource(context)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        locationDataSource: LocationDataSource
    ): LocationRepository {
        return LocationRepositoryImpl(locationDataSource)
    }
}
