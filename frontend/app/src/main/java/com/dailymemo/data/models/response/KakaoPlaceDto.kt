package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

data class KakaoPlaceSearchResponse(
    @SerializedName("documents")
    val documents: List<KakaoPlaceDto>,
    @SerializedName("meta")
    val meta: KakaoSearchMeta
)

data class KakaoPlaceDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("place_name")
    val placeName: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("category_group_code")
    val categoryGroupCode: String?,
    @SerializedName("category_group_name")
    val categoryGroupName: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("road_address_name")
    val roadAddressName: String?,
    @SerializedName("x")
    val longitude: String,
    @SerializedName("y")
    val latitude: String,
    @SerializedName("place_url")
    val placeUrl: String?
)

data class KakaoSearchMeta(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("pageable_count")
    val pageableCount: Int,
    @SerializedName("is_end")
    val isEnd: Boolean
)
