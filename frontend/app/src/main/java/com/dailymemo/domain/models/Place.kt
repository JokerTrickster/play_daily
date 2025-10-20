package com.dailymemo.domain.models

data class Place(
    val id: String,
    val name: String,
    val category: String,
    val phone: String?,
    val address: String,
    val roadAddress: String?,
    val latitude: Double,
    val longitude: Double,
    val placeUrl: String?
) {
    fun toPlaceCategory(): PlaceCategory {
        return when {
            category.contains("카페", ignoreCase = true) -> PlaceCategory.CAFE
            category.contains("음식점", ignoreCase = true) ||
            category.contains("식당", ignoreCase = true) -> PlaceCategory.RESTAURANT
            category.contains("편의점", ignoreCase = true) -> PlaceCategory.CONVENIENCE
            category.contains("병원", ignoreCase = true) -> PlaceCategory.HOSPITAL
            category.contains("약국", ignoreCase = true) -> PlaceCategory.PHARMACY
            category.contains("관광", ignoreCase = true) ||
            category.contains("명소", ignoreCase = true) -> PlaceCategory.ENTERTAINMENT
            category.contains("쇼핑", ignoreCase = true) -> PlaceCategory.SHOPPING
            category.contains("문화", ignoreCase = true) ||
            category.contains("공연", ignoreCase = true) -> PlaceCategory.CULTURAL
            category.contains("숙박", ignoreCase = true) ||
            category.contains("호텔", ignoreCase = true) -> PlaceCategory.ACCOMMODATION
            category.contains("스포츠", ignoreCase = true) ||
            category.contains("운동", ignoreCase = true) -> PlaceCategory.SPORTS
            category.contains("주차", ignoreCase = true) -> PlaceCategory.PARKING
            category.contains("주유", ignoreCase = true) -> PlaceCategory.GAS_STATION
            category.contains("지하철", ignoreCase = true) -> PlaceCategory.SUBWAY
            category.contains("은행", ignoreCase = true) -> PlaceCategory.BANK
            category.contains("서점", ignoreCase = true) -> PlaceCategory.BOOKSTORE
            else -> PlaceCategory.OTHER
        }
    }
}
