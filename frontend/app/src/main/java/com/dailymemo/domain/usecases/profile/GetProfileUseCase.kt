package com.dailymemo.domain.usecases.profile

import com.dailymemo.domain.models.Profile
import com.dailymemo.domain.repositories.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Result<Profile> {
        return profileRepository.getProfile()
    }
}
