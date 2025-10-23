package com.dailymemo.presentation.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Participant
import com.dailymemo.domain.models.Room
import com.dailymemo.domain.usecases.profile.GetProfileUseCase
import com.dailymemo.domain.usecases.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    // Profile Management States (New - Task #36)
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            _isLoading.value = true

            getProfileUseCase().fold(
                onSuccess = { profile ->
                    _uiState.value = ProfileUiState.Success(profile)
                    _nickname.value = profile.nickname
                    _profileImageUrl.value = profile.profileImageUrl
                    _isLoading.value = false
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "프로필을 불러올 수 없습니다. 다시 시도해주세요."
                    _uiState.value = ProfileUiState.Error(errorMsg)
                    _errorMessage.value = errorMsg
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateNickname(value: String) {
        _nickname.value = value
        clearMessages()
    }

    fun updateCurrentPassword(value: String) {
        _currentPassword.value = value
        clearMessages()
    }

    fun updateNewPassword(value: String) {
        _newPassword.value = value
        clearMessages()
    }

    fun updateConfirmPassword(value: String) {
        _confirmPassword.value = value
        clearMessages()
    }

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
        clearMessages()
    }

    fun clearSelectedImage() {
        _selectedImageUri.value = null
    }

    fun saveProfile() {
        viewModelScope.launch {
            // Validate form
            if (!validateForm()) {
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            // TODO: Upload image to S3 if selected
            // For now, we'll pass null for profileImageUrl
            // In future implementation:
            // val uploadedImageUrl = _selectedImageUri.value?.let { uploadImageToS3(it) }

            val uploadedImageUrl: String? = null // Placeholder for S3 upload

            updateProfileUseCase(
                currentPassword = _currentPassword.value,
                nickname = if (_nickname.value.isNotBlank()) _nickname.value else null,
                newPassword = if (_newPassword.value.isNotBlank()) _newPassword.value else null,
                confirmPassword = if (_confirmPassword.value.isNotBlank()) _confirmPassword.value else null,
                profileImageUrl = uploadedImageUrl
            ).fold(
                onSuccess = { profile ->
                    _uiState.value = ProfileUiState.Success(profile)
                    _nickname.value = profile.nickname
                    _profileImageUrl.value = profile.profileImageUrl

                    // Determine success message based on what was changed
                    _successMessage.value = if (_newPassword.value.isNotBlank()) {
                        "프로필이 업데이트되었습니다 ✅\n새 비밀번호로 로그인할 수 있습니다."
                    } else {
                        "프로필이 성공적으로 업데이트되었습니다 ✅"
                    }

                    _isLoading.value = false

                    // Clear password fields after successful update
                    _currentPassword.value = ""
                    _newPassword.value = ""
                    _confirmPassword.value = ""
                    _selectedImageUri.value = null
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "프로필 업데이트에 실패했습니다. 다시 시도해주세요."
                    _isLoading.value = false
                }
            )
        }
    }

    private fun validateForm(): Boolean {
        // Nickname validation
        if (_nickname.value.isBlank()) {
            _errorMessage.value = "이름을 입력해주세요."
            return false
        }

        // Current password is required for any update
        if (_currentPassword.value.isBlank()) {
            _errorMessage.value = "현재 비밀번호를 입력해주세요."
            return false
        }

        // If new password is provided, validate it
        if (_newPassword.value.isNotBlank()) {
            if (_newPassword.value.length < 6) {
                _errorMessage.value = "새 비밀번호는 최소 6자 이상이어야 합니다."
                return false
            }

            if (_newPassword.value != _confirmPassword.value) {
                _errorMessage.value = "새 비밀번호가 일치하지 않습니다."
                return false
            }
        }

        // At least one field should be changed
        val currentProfile = (_uiState.value as? ProfileUiState.Success)?.profile
        val hasChanges = currentProfile?.let {
            _nickname.value.trim() != it.nickname ||
                    _newPassword.value.isNotBlank() ||
                    _selectedImageUri.value != null
        } ?: true

        if (!hasChanges) {
            _errorMessage.value = "변경된 내용이 없습니다."
            return false
        }

        return true
    }

    /**
     * Compress image to reduce file size
     * Target: max 1024x1024, 80% quality
     * TODO: Implement when S3 upload is ready
     */
    private fun compressImage(uri: Uri, inputStream: InputStream): ByteArray {
        // Decode image
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        // Calculate sample size
        val maxDimension = 1024
        var sampleSize = 1
        val width = options.outWidth
        val height = options.outHeight

        if (width > maxDimension || height > maxDimension) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            while ((halfWidth / sampleSize) >= maxDimension && (halfHeight / sampleSize) >= maxDimension) {
                sampleSize *= 2
            }
        }

        // Decode with sample size
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        // Re-open input stream for actual decoding
        // Note: In actual implementation, this should be done with proper InputStream management
        val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            ?: throw Exception("Failed to decode image")

        // Compress to JPEG with 80% quality
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        bitmap.recycle()

        return outputStream.toByteArray()
    }

    /**
     * Upload image to S3 and return the URL
     * TODO: Implement S3 upload logic
     * This is a placeholder for future implementation
     */
    private suspend fun uploadImageToS3(uri: Uri): String? {
        // TODO: Implement S3 upload
        // 1. Compress image using compressImage()
        // 2. Upload to S3 using AWS SDK or presigned URL
        // 3. Return the uploaded image URL
        return null
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    fun dismissSuccess() {
        _successMessage.value = null
    }

    // ============================================================
    // Room Management States and Methods (Legacy - kept for compatibility)
    // TODO: Move to separate RoomViewModel when room management is implemented
    // ============================================================

    private val _userName = MutableStateFlow("사용자")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("user@example.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _memoCount = MutableStateFlow(0)
    val memoCount: StateFlow<Int> = _memoCount.asStateFlow()

    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()

    private val _currentUserId = MutableStateFlow(1L)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    private val _roomIdInput = MutableStateFlow("")
    val roomIdInput: StateFlow<String> = _roomIdInput.asStateFlow()

    private val _showJoinDialog = MutableStateFlow(false)
    val showJoinDialog: StateFlow<Boolean> = _showJoinDialog.asStateFlow()

    init {
        loadProfile()
        loadUserInfo()
        loadCurrentRoom()
    }

    private fun loadUserInfo() {
        // TODO: 백엔드 연동 시 실제 사용자 정보 로드
        _userName.value = "홍길동"
        _userEmail.value = "hong@example.com"
        _memoCount.value = 42
    }

    private fun loadCurrentRoom() {
        viewModelScope.launch {
            // TODO: 백엔드 연동 시 실제 데이터 로드
            _currentRoom.value = Room(
                id = "room_${_currentUserId.value}",
                name = "내 일상 메모",
                ownerId = _currentUserId.value,
                ownerName = _userName.value,
                participants = listOf(
                    Participant(
                        id = _currentUserId.value,
                        name = _userName.value,
                        isOwner = true,
                        joinedAt = LocalDateTime.now()
                    )
                ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }

    fun isOwner(): Boolean {
        return _currentRoom.value?.ownerId == _currentUserId.value
    }

    fun onRoomIdInputChange(input: String) {
        _roomIdInput.value = input
    }

    fun showJoinDialog() {
        _showJoinDialog.value = true
    }

    fun hideJoinDialog() {
        _showJoinDialog.value = false
        _roomIdInput.value = ""
    }

    fun joinRoom(roomId: String) {
        if (roomId.isBlank()) return

        viewModelScope.launch {
            // TODO: 백엔드 연동 시 실제 방 참여 API 호출
            _currentRoom.value = Room(
                id = roomId,
                name = "친구의 일상 메모",
                ownerId = 2L,
                ownerName = "친구",
                participants = listOf(
                    Participant(
                        id = 2L,
                        name = "친구",
                        isOwner = true,
                        joinedAt = LocalDateTime.now().minusDays(1)
                    ),
                    Participant(
                        id = _currentUserId.value,
                        name = _userName.value,
                        isOwner = false,
                        joinedAt = LocalDateTime.now()
                    )
                ),
                createdAt = LocalDateTime.now().minusDays(5),
                updatedAt = LocalDateTime.now()
            )
            hideJoinDialog()
        }
    }

    fun leaveRoom() {
        if (isOwner()) {
            // 방장은 나갈 수 없음
            return
        }

        viewModelScope.launch {
            // TODO: 백엔드 연동 시 실제 방 나가기 API 호출
            loadCurrentRoom()
        }
    }

    fun kickParticipant(participantId: Long) {
        if (!isOwner()) {
            // 방장만 추방 가능
            return
        }

        viewModelScope.launch {
            // TODO: 백엔드 연동 시 실제 추방 API 호출
            val currentRoom = _currentRoom.value ?: return@launch
            _currentRoom.value = currentRoom.copy(
                participants = currentRoom.participants.filter { it.id != participantId }
            )
        }
    }

    fun logout() {
        // TODO: 백엔드 연동 시 실제 로그아웃 처리
        // 1. 토큰 삭제
        // 2. 로컬 데이터 정리
        // 3. 로그인 화면으로 이동
    }
}
