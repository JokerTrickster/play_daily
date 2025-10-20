package com.dailymemo.domain.models

enum class PlaceCategory(val displayName: String, val icon: String, val kakaoCode: String?) {
    ALL("전체", "🗺️", null),
    RESTAURANT("음식점", "🍽️", "FD6"),
    CAFE("카페", "☕", "CE7"),
    CONVENIENCE("편의점", "🏪", "CS2"),
    HOSPITAL("병원", "🏥", "HP8"),
    PHARMACY("약국", "💊", "PM9"),
    CULTURAL("문화시설", "🎭", "CT1"),
    ACCOMMODATION("숙박", "🏨", "AD5"),
    ENTERTAINMENT("놀거리", "🎮", "AT4"),
    SHOPPING("쇼핑", "🛍️", "MT1"),
    SPORTS("운동", "⚽", "SW8"),
    PARKING("주차장", "🅿️", "PK6"),
    GAS_STATION("주유소", "⛽", "OL7"),
    SUBWAY("지하철", "🚇", "SW8"),
    BANK("은행", "🏦", "BK9"),
    BOOKSTORE("서점", "📚", "HP8"),
    OTHER("기타", "📍", null);

    companion object {
        fun fromString(value: String?): PlaceCategory {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
