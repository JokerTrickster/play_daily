package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location>
    fun getLocationUpdates(): Flow<Location>
    fun hasLocationPermission(): Boolean
}
