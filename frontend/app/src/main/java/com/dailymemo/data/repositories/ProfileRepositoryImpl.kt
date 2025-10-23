package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.ProfileApiService
import com.dailymemo.data.remote.dto.UpdateProfileRequest
import com.dailymemo.domain.models.Profile
import com.dailymemo.domain.repositories.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileApiService: ProfileApiService
) : ProfileRepository {

    override suspend fun getProfile(): Result<Profile> {
        return try {
            val response = profileApiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.toDomain()
                Result.success(profile)
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "로그인이 필요합니다. 다시 로그인해주세요."
                    404 -> "프로필 정보를 찾을 수 없습니다."
                    500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    else -> "프로필 조회에 실패했습니다. 다시 시도해주세요."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("요청 시간이 초과되었습니다. 네트워크 연결을 확인해주세요."))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("인터넷 연결을 확인해주세요."))
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 연결을 확인해주세요."))
        }
    }

    override suspend fun updateProfile(
        currentPassword: String,
        nickname: String?,
        newPassword: String?,
        profileImageUrl: String?
    ): Result<Profile> {
        return try {
            val request = UpdateProfileRequest(
                currentPassword = currentPassword,
                nickname = nickname,
                newPassword = newPassword,
                profileImageUrl = profileImageUrl
            )

            val response = profileApiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.toDomain()
                Result.success(profile)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "현재 비밀번호가 올바르지 않습니다. 다시 확인해주세요."
                    401 -> "로그인이 필요합니다. 다시 로그인해주세요."
                    500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    else -> "프로필 수정에 실패했습니다. 다시 시도해주세요."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("요청 시간이 초과되었습니다. 네트워크 연결을 확인해주세요."))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("인터넷 연결을 확인해주세요."))
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 연결을 확인해주세요."))
        }
    }
}
