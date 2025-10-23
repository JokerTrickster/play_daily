package com.dailymemo.domain.usecases.profile

import com.dailymemo.domain.models.Profile
import com.dailymemo.domain.repositories.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        currentPassword: String,
        nickname: String? = null,
        newPassword: String? = null,
        confirmPassword: String? = null,
        profileImageUrl: String? = null
    ): Result<Profile> {
        // Validate current password is not empty
        if (currentPassword.isBlank()) {
            return Result.failure(Exception("현재 비밀번호를 입력해주세요."))
        }

        // Validate new password matches confirm password
        if (newPassword != null && newPassword != confirmPassword) {
            return Result.failure(Exception("새 비밀번호가 일치하지 않습니다."))
        }

        // Validate password length if provided
        if (newPassword != null && newPassword.length < 6) {
            return Result.failure(Exception("새 비밀번호는 최소 6자 이상이어야 합니다."))
        }

        // Validate nickname if provided
        if (nickname != null && nickname.trim().isEmpty()) {
            return Result.failure(Exception("이름을 입력해주세요."))
        }

        // Call repository to update profile
        return profileRepository.updateProfile(
            currentPassword = currentPassword,
            nickname = nickname?.trim(),
            newPassword = newPassword,
            profileImageUrl = profileImageUrl
        )
    }
}
