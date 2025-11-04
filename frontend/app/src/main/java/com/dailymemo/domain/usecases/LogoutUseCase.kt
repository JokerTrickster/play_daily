package com.dailymemo.domain.usecases

import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.domain.repositories.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend operator fun invoke() {
        // Clear all local data including room_id, tokens, and user info
        authLocalDataSource.clearTokens()
        authLocalDataSource.setCurrentRoomId(null)

        // Repository logout (for any additional cleanup)
        authRepository.logout()
    }
}
