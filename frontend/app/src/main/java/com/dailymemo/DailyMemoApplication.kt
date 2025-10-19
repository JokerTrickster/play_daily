package com.dailymemo

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DailyMemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 카카오 맵 SDK 초기화
        KakaoMapSdk.init(this, "9747e11e65cc5484030160a803fb603b")
    }
}
