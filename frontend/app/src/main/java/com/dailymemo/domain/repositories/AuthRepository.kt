package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.AuthResult
import com.dailymemo.domain.models.User

interface AuthRepository {
    suspend fun signup(username: String, password: String, authCode: String): AuthResult
    suspend fun login(username: String, password: String): AuthResult
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}
