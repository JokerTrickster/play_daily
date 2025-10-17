package com.dailymemo.data.datasources.remote

import com.dailymemo.data.datasources.remote.api.AuthApiService
import com.dailymemo.data.models.AuthTokenDto
import com.dailymemo.data.models.request.SignInRequestDto
import com.dailymemo.data.models.request.SignUpRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val authApiService: AuthApiService
) {

    suspend fun signup(
        username: String,
        password: String,
        authCode: String
    ): Result<AuthTokenDto> {
        return try {
            val request = SignUpRequestDto(
                accountId = username,
                password = password,
                authCode = authCode,
                nickname = username // Using username as nickname for now
            )

            val response = authApiService.signup(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                Result.success(
                    AuthTokenDto(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                        userId = authResponse.userId.toString(),
                        username = authResponse.accountId,
                        memoSpaceId = authResponse.userId.toString() // Using userId as memoSpaceId
                    )
                )
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "잘못된 요청입니다"
                    401 -> "인증에 실패했습니다"
                    else -> "회원가입에 실패했습니다"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류가 발생했습니다: ${e.message}"))
        }
    }

    suspend fun login(
        username: String,
        password: String
    ): Result<AuthTokenDto> {
        return try {
            val request = SignInRequestDto(
                accountId = username,
                password = password
            )

            val response = authApiService.signIn(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                Result.success(
                    AuthTokenDto(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                        userId = authResponse.userId.toString(),
                        username = authResponse.accountId,
                        memoSpaceId = authResponse.userId.toString() // Using userId as memoSpaceId
                    )
                )
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "잘못된 요청입니다"
                    401 -> "아이디 또는 비밀번호가 일치하지 않습니다"
                    else -> "로그인에 실패했습니다"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류가 발생했습니다: ${e.message}"))
        }
    }
}
