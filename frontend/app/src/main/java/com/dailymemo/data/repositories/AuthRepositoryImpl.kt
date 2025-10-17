package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.data.datasources.remote.AuthRemoteDataSource
import com.dailymemo.domain.models.AuthResult
import com.dailymemo.domain.models.User
import com.dailymemo.domain.repositories.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val localDataSource: AuthLocalDataSource,
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun signup(username: String, password: String, authCode: String): AuthResult {
        return try {
            val result = remoteDataSource.signup(username, password, authCode)

            result.fold(
                onSuccess = { tokenDto ->
                    localDataSource.saveTokens(
                        accessToken = tokenDto.accessToken,
                        refreshToken = tokenDto.refreshToken,
                        userId = tokenDto.userId,
                        username = tokenDto.username,
                        memoSpaceId = tokenDto.memoSpaceId
                    )

                    AuthResult.Success(
                        User(
                            id = tokenDto.userId,
                            username = tokenDto.username,
                            memoSpaceId = tokenDto.memoSpaceId
                        )
                    )
                },
                onFailure = { exception ->
                    AuthResult.Error(exception.message ?: "회원가입에 실패했습니다")
                }
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "회원가입에 실패했습니다")
        }
    }

    override suspend fun login(username: String, password: String): AuthResult {
        return try {
            val result = remoteDataSource.login(username, password)

            result.fold(
                onSuccess = { tokenDto ->
                    localDataSource.saveTokens(
                        accessToken = tokenDto.accessToken,
                        refreshToken = tokenDto.refreshToken,
                        userId = tokenDto.userId,
                        username = tokenDto.username,
                        memoSpaceId = tokenDto.memoSpaceId
                    )

                    AuthResult.Success(
                        User(
                            id = tokenDto.userId,
                            username = tokenDto.username,
                            memoSpaceId = tokenDto.memoSpaceId
                        )
                    )
                },
                onFailure = { exception ->
                    AuthResult.Error(exception.message ?: "로그인에 실패했습니다")
                }
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "로그인에 실패했습니다")
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return localDataSource.isLoggedIn()
    }

    override suspend fun getCurrentUser(): User? {
        val userId = localDataSource.getUserId() ?: return null
        val username = localDataSource.getUsername() ?: return null
        val memoSpaceId = localDataSource.getMemoSpaceId() ?: return null

        return User(
            id = userId,
            username = username,
            memoSpaceId = memoSpaceId
        )
    }

    override suspend fun logout() {
        localDataSource.clearTokens()
    }
}
