package com.dailymemo

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.dailymemo.BuildConfig
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class DailyMemoApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // 카카오 맵 SDK 초기화 (테스트 앱 네이티브 키)
        KakaoMapSdk.init(this, "b707af9016b2d598ce8cc4313c7adda1")
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // 메모리 캐시 설정 (최대 25% RAM 사용)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // RAM의 25% 사용
                    .build()
            }
            // 디스크 캐시 설정 (최대 250MB)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024) // 250MB
                    .build()
            }
            // OkHttp 클라이언트 커스텀 (타임아웃 설정)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()
            }
            // 캐시 정책
            .respectCacheHeaders(false) // 서버 캐시 헤더 무시 (항상 캐시 사용)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // 크로스페이드 애니메이션 (부드러운 로딩)
            .crossfade(true)
            .crossfade(300)
            // 디버그 로깅 (개발 시에만)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}
