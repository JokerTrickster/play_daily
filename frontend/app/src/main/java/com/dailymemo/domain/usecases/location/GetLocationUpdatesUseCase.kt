package com.dailymemo.domain.usecases.location

import com.dailymemo.domain.models.Location
import com.dailymemo.domain.repositories.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocationUpdatesUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<Location> {
        return locationRepository.getLocationUpdates()
    }
}
