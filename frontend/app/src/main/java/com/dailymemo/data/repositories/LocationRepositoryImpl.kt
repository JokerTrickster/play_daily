package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.local.LocationDataSource
import com.dailymemo.domain.models.Location
import com.dailymemo.domain.repositories.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationDataSource: LocationDataSource
) : LocationRepository {

    override suspend fun getCurrentLocation(): Result<Location> {
        return try {
            val androidLocation = locationDataSource.getCurrentLocation()
            if (androidLocation != null) {
                Result.success(
                    Location(
                        latitude = androidLocation.latitude,
                        longitude = androidLocation.longitude,
                        accuracy = androidLocation.accuracy
                    )
                )
            } else {
                Result.failure(Exception("Location not available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLocationUpdates(): Flow<Location> {
        return locationDataSource.getLocationUpdates().map { androidLocation ->
            Location(
                latitude = androidLocation.latitude,
                longitude = androidLocation.longitude,
                accuracy = androidLocation.accuracy
            )
        }
    }

    override fun hasLocationPermission(): Boolean {
        return locationDataSource.hasLocationPermission()
    }
}
