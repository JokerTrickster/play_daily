package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.MemoApiService
import com.dailymemo.data.models.request.CreateMemoRequestDto
import com.dailymemo.data.models.request.UpdateMemoRequestDto
import com.dailymemo.data.models.response.MemoDto
import com.dailymemo.domain.models.Comment
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.repositories.MemoRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepositoryImpl @Inject constructor(
    private val memoApiService: MemoApiService
) : MemoRepository {

    override suspend fun getMemos(
        isWishlist: Boolean?
    ): Result<List<Memo>> {
        return try {
            val response = memoApiService.getMemos(isWishlist)
            if (response.isSuccessful && response.body() != null) {
                val memos = response.body()!!.memos.map { it.toDomain() }
                Result.success(memos)
            } else {
                Result.failure(Exception("메모 목록 조회에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun getMemo(id: Long): Result<Memo> {
        return try {
            val response = memoApiService.getMemo(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("메모 조회에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun createMemo(
        title: String,
        content: String,
        imageUrl: String?,
        imageUrls: List<String>,
        rating: Int,
        isPinned: Boolean,
        latitude: Double?,
        longitude: Double?,
        locationName: String?,
        category: PlaceCategory?,
        isWishlist: Boolean,
        businessName: String?,
        businessPhone: String?,
        businessAddress: String?,
        naverPlaceUrl: String?
    ): Result<Memo> {
        return try {
            // multipart/form-data 요청 파라미터 생성
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val ratingPart = rating.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val isPinnedPart = isPinned.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val latitudePart = latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val longitudePart = longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val locationNamePart = locationName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryPart = category?.name?.toRequestBody("text/plain".toMediaTypeOrNull())
            val isWishlistPart = isWishlist.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val businessNamePart = businessName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val businessPhonePart = businessPhone?.toRequestBody("text/plain".toMediaTypeOrNull())
            val businessAddressPart = businessAddress?.toRequestBody("text/plain".toMediaTypeOrNull())
            val naverPlaceUrlPart = naverPlaceUrl?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = memoApiService.createMemo(
                title = titlePart,
                content = contentPart,
                rating = ratingPart,
                isPinned = isPinnedPart,
                latitude = latitudePart,
                longitude = longitudePart,
                locationName = locationNamePart,
                category = categoryPart,
                isWishlist = isWishlistPart,
                businessName = businessNamePart,
                businessPhone = businessPhonePart,
                businessAddress = businessAddressPart,
                naverPlaceUrl = naverPlaceUrlPart,
                image = null // TODO: 이미지 업로드 구현 시 추가
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("메모 생성에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun updateMemo(
        id: Long,
        title: String,
        content: String,
        imageUrl: String?,
        imageUrls: List<String>,
        rating: Int,
        isPinned: Boolean,
        latitude: Double?,
        longitude: Double?,
        locationName: String?,
        isWishlist: Boolean,
        businessName: String?,
        businessPhone: String?,
        businessAddress: String?
    ): Result<Memo> {
        return try {
            val request = UpdateMemoRequestDto(
                title = title,
                content = content,
                imageUrl = imageUrl,
                rating = rating,
                isPinned = isPinned,
                latitude = latitude,
                longitude = longitude,
                locationName = locationName,
                isWishlist = isWishlist,
                businessName = businessName,
                businessPhone = businessPhone,
                businessAddress = businessAddress
            )
            val response = memoApiService.updateMemo(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("메모 수정에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun deleteMemo(id: Long): Result<Unit> {
        return try {
            val response = memoApiService.deleteMemo(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("메모 삭제에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun uploadImage(imageUri: android.net.Uri): Result<String> {
        // TODO: 백엔드 연동 시 실제 이미지 업로드 구현
        return Result.success("https://example.com/image/${System.currentTimeMillis()}.jpg")
    }

    private fun MemoDto.toDomain(): Memo {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return Memo(
            id = id,
            userId = userId,
            title = title,
            content = content,
            imageUrl = imageUrl,
            rating = rating,
            isPinned = isPinned,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            category = PlaceCategory.fromString(category),
            isWishlist = isWishlist,
            businessName = businessName,
            businessPhone = businessPhone,
            businessAddress = businessAddress,
            naverPlaceUrl = naverPlaceUrl,
            comments = comments?.map { commentDto ->
                Comment(
                    id = commentDto.id,
                    memoId = commentDto.memo_id,
                    userId = commentDto.user_id,
                    userName = commentDto.user_name,
                    content = commentDto.content,
                    rating = commentDto.rating,
                    createdAt = LocalDateTime.parse(commentDto.created_at, formatter),
                    updatedAt = LocalDateTime.parse(commentDto.updated_at, formatter)
                )
            } ?: emptyList(),
            createdAt = LocalDateTime.parse(createdAt, formatter),
            updatedAt = LocalDateTime.parse(updatedAt, formatter)
        )
    }
}
