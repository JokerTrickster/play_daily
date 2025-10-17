package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.User
import com.dailymemo.domain.repositories.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}
