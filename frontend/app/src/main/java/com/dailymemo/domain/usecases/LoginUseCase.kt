package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.AuthResult
import com.dailymemo.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): AuthResult {
        return authRepository.login(username, password)
    }
}
