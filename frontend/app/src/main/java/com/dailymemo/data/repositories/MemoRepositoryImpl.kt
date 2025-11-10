package com.dailymemo.data.repositories

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.dailymemo.data.datasources.remote.api.MemoApiService
import com.dailymemo.data.models.request.CreateMemoRequestDto
import com.dailymemo.data.models.request.UpdateMemoRequestDto
import com.dailymemo.data.models.response.MemoDto
import com.dailymemo.domain.error.DomainError
import com.dailymemo.domain.models.Comment
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.repositories.MemoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memoApiService: MemoApiService,
    private val profileApiService: com.dailymemo.data.datasources.remote.api.ProfileApiService
) : MemoRepository {

    override suspend fun getMemos(
        isWishlist: Boolean?,
        roomId: Int?,
        categoryIds: List<Int>?,
        page: Int,
        limit: Int
    ): Result<com.dailymemo.domain.models.MemoListResult> {
        return try {
            android.util.Log.d("MemoRepositoryImpl", "getMemos - isWishlist: $isWishlist, roomId: $roomId, categoryIds: $categoryIds, page: $page, limit: $limit")
            val response = memoApiService.getMemos(isWishlist, roomId, categoryIds, page, limit)
            android.util.Log.d("MemoRepositoryImpl", "getMemos response - code: ${response.code()}, isSuccessful: ${response.isSuccessful}, body: ${response.body()}")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val memos = body.memos.map { it.toDomain() }
                val hasMore = (page * limit) < body.total
                val result = com.dailymemo.domain.models.MemoListResult(
                    memos = memos,
                    total = body.total,
                    page = body.page,
                    limit = body.limit,
                    hasMore = hasMore
                )
                android.util.Log.d("MemoRepositoryImpl", "getMemos success - total: ${body.total}, memos count: ${memos.size}")
                Result.success(result)
            } else {
                android.util.Log.e("MemoRepositoryImpl", "getMemos failed - code: ${response.code()}, message: ${response.message()}")
                Result.failure(DomainError.MemoLoadFailed)
            }
        } catch (e: Exception) {
            android.util.Log.e("MemoRepositoryImpl", "getMemos exception", e)
            val error = handleException(e)
            Result.failure(error)
        }
    }

    override suspend fun getMemo(id: Long): Result<Memo> {
        return try {
            val response = memoApiService.getMemo(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val error = if (response.code() == 404) {
                    DomainError.MemoNotFound
                } else {
                    DomainError.MemoLoadFailed
                }
                Result.failure(error)
            }
        } catch (e: Exception) {
            val error = handleException(e)
            Result.failure(error)
        }
    }

    override suspend fun createMemo(
        title: String,
        content: String,
        creationMode: com.dailymemo.domain.models.CreationMode,
        categoryIds: List<Int>,
        imageUri: Uri?,
        imageUrls: List<String>,
        rating: Float,
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
            val creationModePart = creationMode.toApiValue().toRequestBody("text/plain".toMediaTypeOrNull()) // NEW
            val categoryIdsPart = com.google.gson.Gson().toJson(categoryIds).toRequestBody("text/plain".toMediaTypeOrNull()) // NEW
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

            // 이미지 Uri를 MultipartBody.Part로 변환
            val imagePart = imageUri?.let { prepareFilePart(it) }

            val response = memoApiService.createMemo(
                title = titlePart,
                content = contentPart,
                creationMode = creationModePart, // NEW
                categoryIds = categoryIdsPart, // NEW
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
                image = imagePart
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("MemoRepository", "메모 생성 실패 (${response.code()}): ${errorBody ?: response.message()}")
                Result.failure(DomainError.MemoCreateFailed)
            }
        } catch (e: Exception) {
            android.util.Log.e("MemoRepository", "메모 생성 예외", e)
            val error = if (e.message?.contains("Failed to read image") == true) {
                DomainError.ImageReadFailed
            } else {
                handleException(e)
            }
            Result.failure(error)
        }
    }

    override suspend fun updateMemo(
        id: Long,
        title: String,
        content: String,
        imageUrl: String?,
        imageUrls: List<String>,
        rating: Float,
        isPinned: Boolean,
        latitude: Double?,
        longitude: Double?,
        locationName: String?,
        isWishlist: Boolean,
        businessName: String?,
        businessPhone: String?,
        businessAddress: String?,
        categoryIds: List<Int>
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
                businessAddress = businessAddress,
                categoryIds = categoryIds
            )
            val response = memoApiService.updateMemo(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(DomainError.MemoUpdateFailed)
            }
        } catch (e: Exception) {
            val error = handleException(e)
            Result.failure(error)
        }
    }

    override suspend fun deleteMemo(id: Long): Result<Unit> {
        return try {
            val response = memoApiService.deleteMemo(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(DomainError.MemoDeleteFailed)
            }
        } catch (e: Exception) {
            val error = handleException(e)
            Result.failure(error)
        }
    }

    override suspend fun uploadImage(imageUri: android.net.Uri): Result<String> {
        return try {
            // 이미지를 MultipartBody.Part로 변환
            val imagePart = prepareFilePart(imageUri)

            // 프로필 이미지 업로드 API 호출
            val response = profileApiService.uploadProfileImage(imagePart)

            if (response.isSuccessful && response.body() != null) {
                val imageUrl = response.body()!!["image_url"]
                if (imageUrl != null) {
                    android.util.Log.d("MemoRepository", "이미지 업로드 성공: $imageUrl")
                    Result.success(imageUrl)
                } else {
                    android.util.Log.e("MemoRepository", "이미지 URL이 응답에 없음")
                    Result.failure(DomainError.ImageUploadFailed)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("MemoRepository", "이미지 업로드 실패 (${response.code()}): ${errorBody ?: response.message()}")
                Result.failure(DomainError.ImageUploadFailed)
            }
        } catch (e: Exception) {
            android.util.Log.e("MemoRepository", "이미지 업로드 예외", e)
            val error = if (e.message?.contains("Failed to read image") == true) {
                DomainError.ImageReadFailed
            } else {
                handleException(e)
            }
            Result.failure(error)
        }
    }

    override suspend fun toggleMemoLike(memoId: Long): Result<Boolean> {
        return try {
            val response = memoApiService.toggleMemoLike(memoId)
            if (response.isSuccessful) {
                val isLiked = response.body()?.get("is_liked") as? Boolean ?: false
                Result.success(isLiked)
            } else {
                Result.failure(DomainError.LikeToggleFailed)
            }
        } catch (e: Exception) {
            val error = handleException(e)
            Result.failure(error)
        }
    }

    private fun handleException(e: Exception): DomainError {
        return when (e) {
            is java.net.UnknownHostException -> DomainError.NoConnection
            is java.net.SocketTimeoutException -> DomainError.Timeout
            is java.io.IOException -> DomainError.NetworkError(e)
            else -> DomainError.UnknownError
        }
    }

    private fun prepareFilePart(uri: Uri): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val fileName = getFileName(uri) ?: "image_${System.currentTimeMillis()}.jpg"

        // 이미지 압축 처리
        val compressedBytes = contentResolver.openInputStream(uri)?.use { inputStream ->
            // 비트맵 디코딩
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

            // 크기 조정 (최대 1920x1920)
            val scaledBitmap = if (bitmap.width > 1920 || bitmap.height > 1920) {
                val scale = minOf(1920f / bitmap.width, 1920f / bitmap.height)
                android.graphics.Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }

            // JPEG 압축 (85% 품질)
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()

            // 메모리 해제
            if (scaledBitmap != bitmap) scaledBitmap.recycle()
            bitmap.recycle()

            bytes
        } ?: throw Exception("Failed to read image")

        // RequestBody 생성
        val requestBody = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

        // MultipartBody.Part 생성
        return MultipartBody.Part.createFormData("image", fileName, requestBody)
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    private fun MemoDto.toDomain(): Memo {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return Memo(
            id = id,
            userId = userId,
            title = title,
            content = content,
            creationMode = when (creationMode) {
                "map" -> com.dailymemo.domain.models.CreationMode.MAP
                else -> com.dailymemo.domain.models.CreationMode.LIST
            },
            categories = categories?.map { categoryDto ->
                com.dailymemo.domain.models.MemoCategory(
                    id = categoryDto.id,
                    name = categoryDto.name,
                    sentiment = com.dailymemo.domain.models.CategorySentiment.fromString(categoryDto.sentiment),
                    color = categoryDto.color,
                    displayOrder = categoryDto.displayOrder
                )
            } ?: emptyList(),
            imageUrl = imageUrl,
            rating = rating,
            isPinned = isPinned,
            likesCount = likesCount,
            isLiked = isLiked,
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
