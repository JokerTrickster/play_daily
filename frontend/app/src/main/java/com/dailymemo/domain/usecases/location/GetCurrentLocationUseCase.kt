package com.dailymemo.domain.usecases.location

import com.dailymemo.domain.models.Location
import com.dailymemo.domain.repositories.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<Location> {
        if (!locationRepository.hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }
        return locationRepository.getCurrentLocation()
    }
}
