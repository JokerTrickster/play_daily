package com.dailymemo

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DailyMemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 카카오 맵 SDK 초기화 (테스트 앱 네이티브 키)
        KakaoMapSdk.init(this, "b707af9016b2d598ce8cc4313c7adda1")
    }
}
