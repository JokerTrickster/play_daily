package com.dailymemo.presentation.map

import com.dailymemo.R

/**
 * 카카오맵 마커에 사용할 색상별 핀 아이콘을 정의합니다.
 *
 * @property resourceId drawable 리소스 ID
 */
sealed class MarkerPinIcon(val resourceId: Int) {
    /**
     * 방문한 장소를 나타내는 파란색 핀
     */
    object Blue : MarkerPinIcon(R.drawable.ic_pin_blue)

    /**
     * 위시리스트(가고 싶은 곳)를 나타내는 빨간색 핀
     */
    object Red : MarkerPinIcon(R.drawable.ic_pin_red)

    /**
     * 고정된 메모를 나타내는 노란색 핀
     */
    object Yellow : MarkerPinIcon(R.drawable.ic_pin_yellow)
}
