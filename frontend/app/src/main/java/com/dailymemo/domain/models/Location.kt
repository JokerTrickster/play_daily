package com.dailymemo.domain.models

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val address: String? = null
)
