package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.Profile

interface ProfileRepository {
    suspend fun getProfile(): Result<Profile>
    suspend fun updateProfile(
        currentPassword: String,
        nickname: String? = null,
        newPassword: String? = null,
        profileImageUrl: String? = null
    ): Result<Profile>
}
