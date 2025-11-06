package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.CategoryApiService
import com.dailymemo.domain.models.MemoCategory
import com.dailymemo.domain.repositories.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryApiService: CategoryApiService
) : CategoryRepository {

    // 메모리 캐시: 카테고리는 변경되지 않으므로 앱 재시작 전까지 유지
    private var cachedCategories: List<MemoCategory>? = null

    override suspend fun getCategories(): Result<List<MemoCategory>> {
        return try {
            // 캐시된 데이터가 있으면 즉시 반환
            cachedCategories?.let {
                return Result.success(it)
            }

            // API에서 카테고리 목록 가져오기
            val response = categoryApiService.getCategories()
            if (response.isSuccessful && response.body() != null) {
                val categories = response.body()!!.toDomain()
                // 캐시에 저장
                cachedCategories = categories
                Result.success(categories)
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "로그인이 필요합니다. 다시 로그인해주세요."
                    404 -> "카테고리 정보를 찾을 수 없습니다."
                    500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    else -> "카테고리 조회에 실패했습니다. 다시 시도해주세요."
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

    override fun getCachedCategories(): List<MemoCategory>? {
        return cachedCategories
    }
}
