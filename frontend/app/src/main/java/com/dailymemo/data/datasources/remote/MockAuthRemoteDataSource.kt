package com.dailymemo.data.datasources.remote

import com.dailymemo.data.models.AuthTokenDto
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRemoteDataSource @Inject constructor() {

    private val validAuthCodes = setOf(
        "TEST123",
        "DEMO456",
        "AUTH789",
        "CODE000"
    )

    private val userDatabase = mutableMapOf<String, MockUser>()

    data class MockUser(
        val id: String,
        val username: String,
        val passwordHash: String,
        val memoSpaceId: String
    )

    suspend fun signup(username: String, password: String, authCode: String): Result<AuthTokenDto> {
        delay(500) // Simulate network delay

        if (!validAuthCodes.contains(authCode)) {
            return Result.failure(Exception("유효하지 않은 인증 코드입니다"))
        }

        if (userDatabase.values.any { it.username == username }) {
            return Result.failure(Exception("이미 존재하는 사용자명입니다"))
        }

        if (username.length < 3) {
            return Result.failure(Exception("사용자명은 3자 이상이어야 합니다"))
        }

        if (password.length < 4) {
            return Result.failure(Exception("비밀번호는 4자 이상이어야 합니다"))
        }

        val userId = UUID.randomUUID().toString()
        val memoSpaceId = UUID.randomUUID().toString()
        val passwordHash = password.hashCode().toString()

        val newUser = MockUser(
            id = userId,
            username = username,
            passwordHash = passwordHash,
            memoSpaceId = memoSpaceId
        )

        userDatabase[userId] = newUser

        return Result.success(
            AuthTokenDto(
                accessToken = "mock_access_token_$userId",
                refreshToken = "mock_refresh_token_$userId",
                userId = userId,
                username = username,
                memoSpaceId = memoSpaceId
            )
        )
    }

    suspend fun login(username: String, password: String): Result<AuthTokenDto> {
        delay(500) // Simulate network delay

        val user = userDatabase.values.find { it.username == username }
            ?: return Result.failure(Exception("사용자를 찾을 수 없습니다"))

        val passwordHash = password.hashCode().toString()
        if (user.passwordHash != passwordHash) {
            return Result.failure(Exception("비밀번호가 일치하지 않습니다"))
        }

        return Result.success(
            AuthTokenDto(
                accessToken = "mock_access_token_${user.id}",
                refreshToken = "mock_refresh_token_${user.id}",
                userId = user.id,
                username = user.username,
                memoSpaceId = user.memoSpaceId
            )
        )
    }
}
