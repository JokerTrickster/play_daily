package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.AuthResult
import com.dailymemo.domain.repositories.AuthRepository
import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String, authCode: String): AuthResult {
        return authRepository.signup(username, password, authCode)
    }
}
