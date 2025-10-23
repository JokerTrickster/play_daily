package com.dailymemo.data.repositories

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.dailymemo.data.datasources.remote.api.MemoApiService
import com.dailymemo.data.models.request.CreateMemoRequestDto
import com.dailymemo.data.models.request.UpdateMemoRequestDto
import com.dailymemo.data.models.response.MemoDto
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
                val errorMsg = "메모 생성 실패 (${response.code()}): ${errorBody ?: response.message()}"
                android.util.Log.e("MemoRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("MemoRepository", "메모 생성 예외", e)
            Result.failure(Exception("네트워크 오류: ${e.message}"))
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

    private fun prepareFilePart(uri: Uri): MultipartBody.Part {
        // Uri에서 파일 정보 가져오기
        val contentResolver = context.contentResolver
        val fileName = getFileName(uri) ?: "image_${System.currentTimeMillis()}.jpg"

        // ContentResolver를 사용하여 임시 파일로 복사
        val tempFile = File(context.cacheDir, fileName)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // MIME 타입 가져오기
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        // RequestBody 생성
        val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

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
