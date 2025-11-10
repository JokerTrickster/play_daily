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
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getMemosUseCase: com.dailymemo.domain.usecases.GetMemosUseCase,
    private val getLikedRoomsUseCase: com.dailymemo.domain.usecases.roomlike.GetLikedRoomsUseCase,
    private val authLocalDataSource: com.dailymemo.data.datasources.local.AuthLocalDataSource,
    private val logoutUseCase: com.dailymemo.domain.usecases.LogoutUseCase,
    private val joinRoomUseCase: com.dailymemo.domain.usecases.room.JoinRoomUseCase,
    private val kickUserUseCase: com.dailymemo.domain.usecases.room.KickUserUseCase,
    private val getRoomMembersUseCase: com.dailymemo.domain.usecases.room.GetRoomMembersUseCase,
    private val updateMemberPermissionUseCase: com.dailymemo.domain.usecases.room.UpdateMemberPermissionUseCase,
    private val updateRoomSettingsUseCase: com.dailymemo.domain.usecases.room.UpdateRoomSettingsUseCase,
    private val memoRepository: com.dailymemo.domain.repositories.MemoRepository
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

    private val _roomPassword = MutableStateFlow("")
    val roomPassword: StateFlow<String> = _roomPassword.asStateFlow()

    private val _receivedLikesCount = MutableStateFlow(0)
    val receivedLikesCount: StateFlow<Int> = _receivedLikesCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Memo location data for map visualization
    private val _memosWithLocation = MutableStateFlow<List<com.dailymemo.domain.models.Memo>>(emptyList())
    val memosWithLocation: StateFlow<List<com.dailymemo.domain.models.Memo>> = _memosWithLocation.asStateFlow()

    // Liked rooms data
    private val _likedRooms = MutableStateFlow<List<com.dailymemo.domain.models.LikedRoom>>(emptyList())
    val likedRooms: StateFlow<List<com.dailymemo.domain.models.LikedRoom>> = _likedRooms.asStateFlow()

    init {
        loadMemosWithLocation()
        loadLikedRooms()
    }

    fun loadMemosWithLocation() {
        viewModelScope.launch {
            getMemosUseCase(isWishlist = null, page = 1, limit = 100).fold(
                onSuccess = { result ->
                    _memosWithLocation.value = result.memos.filter {
                        it.latitude != null && it.longitude != null
                    }
                },
                onFailure = {
                    // Silently fail - location visualization is optional
                }
            )
        }
    }

    fun loadLikedRooms() {
        viewModelScope.launch {
            getLikedRoomsUseCase().fold(
                onSuccess = { rooms ->
                    _likedRooms.value = rooms
                },
                onFailure = {
                    // Silently fail - liked rooms is optional
                    _likedRooms.value = emptyList()
                }
            )
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            _isLoading.value = true

            getProfileUseCase().fold(
                onSuccess = { profile ->
                    _uiState.value = ProfileUiState.Success(profile)
                    _nickname.value = profile.nickname
                    _profileImageUrl.value = profile.profileImageUrl
                    _roomPassword.value = profile.roomPassword
                    _receivedLikesCount.value = profile.receivedLikesCount
                    _isLoading.value = false

                    // Store my default room ID
                    _myRoomId.value = profile.defaultRoomId
                    android.util.Log.d("ProfileViewModel", "loadProfile - userId: ${profile.userId}, defaultRoomId: ${profile.defaultRoomId}, currentRoomId: ${authLocalDataSource.getCurrentRoomId()}")

                    // 프로필 로드 시 기본 방 ID를 현재 방으로 설정 (처음 로드 시에만)
                    profile.defaultRoomId?.let { roomId ->
                        viewModelScope.launch {
                            // Only set if not already set
                            if (authLocalDataSource.getCurrentRoomId() == null) {
                                authLocalDataSource.setCurrentRoomId(roomId)
                                android.util.Log.d("ProfileViewModel", "loadProfile - Set current room to default room: $roomId")
                            }
                        }
                    }

                    // Load memo count for MY default room after profile is loaded
                    loadUserInfo()
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

                    // Switch back to my room after profile update
                    _myRoomId.value?.let { myRoomId ->
                        authLocalDataSource.setCurrentRoomId(myRoomId)
                        android.util.Log.d("ProfileViewModel", "saveProfile - Switched to myRoom: $myRoomId")
                    }

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

    // My default room ID (user's own room)
    private val _myRoomId = MutableStateFlow<Int?>(null)
    val myRoomId: StateFlow<Int?> = _myRoomId.asStateFlow()

    private val _currentUserId = MutableStateFlow(1L)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    private val _roomIdInput = MutableStateFlow("")
    val roomIdInput: StateFlow<String> = _roomIdInput.asStateFlow()

    private val _roomPasswordInput = MutableStateFlow("")
    val roomPasswordInput: StateFlow<String> = _roomPasswordInput.asStateFlow()

    private val _showJoinDialog = MutableStateFlow(false)
    val showJoinDialog: StateFlow<Boolean> = _showJoinDialog.asStateFlow()

    private val _joinRoomError = MutableStateFlow<String?>(null)
    val joinRoomError: StateFlow<String?> = _joinRoomError.asStateFlow()

    init {
        loadProfile() // This will call loadUserInfo() after profile is loaded
        loadCurrentRoom()
    }

    private fun loadUserInfo() {
        // Load memo count for MY default room (not current room)
        viewModelScope.launch {
            val myRoomId = _myRoomId.value
            android.util.Log.d("ProfileViewModel", "loadUserInfo - myRoomId: $myRoomId")

            if (myRoomId != null) {
                // Get memos for MY default room specifically
                memoRepository.getMemos(isWishlist = null, roomId = myRoomId, page = 1, limit = 100).fold(
                    onSuccess = { result ->
                        _memoCount.value = result.total.toInt()
                        android.util.Log.d("ProfileViewModel", "loadUserInfo - MY room memo count: ${result.total}")
                    },
                    onFailure = {
                        android.util.Log.e("ProfileViewModel", "loadUserInfo - Failed to get MY room memos", it)
                        _memoCount.value = 0
                    }
                )
            } else {
                android.util.Log.w("ProfileViewModel", "loadUserInfo - myRoomId is null")
                _memoCount.value = 0
            }
        }
    }

    private fun loadCurrentRoom() {
        viewModelScope.launch {
            val currentRoomId = authLocalDataSource.getCurrentRoomId()
            val myRoomId = _myRoomId.value
            val currentUserId = authLocalDataSource.getUserId()?.toLongOrNull() ?: _currentUserId.value

            android.util.Log.d("ProfileViewModel", "loadCurrentRoom - currentRoomId: $currentRoomId, myRoomId: $myRoomId, currentUserId: $currentUserId")

            if (currentRoomId == null) {
                android.util.Log.w("ProfileViewModel", "loadCurrentRoom - No current room ID")
                _currentRoom.value = null
                return@launch
            }

            // Fetch room members from backend
            getRoomMembersUseCase(currentRoomId.toLong()).fold(
                onSuccess = { members ->
                    android.util.Log.d("ProfileViewModel", "loadCurrentRoom - Fetched ${members.size} members")

                    // Find my membership info
                    val myMembership = members.find { it.userId == currentUserId }
                    if (myMembership != null) {
                        // Update my permission
                        authLocalDataSource.setCurrentRoomPermission(myMembership.permission.name)
                        android.util.Log.d("ProfileViewModel", "loadCurrentRoom - My permission: ${myMembership.permission.name}")
                    }

                    // Find room owner
                    val owner = members.find { it.permission == com.dailymemo.domain.models.RoomPermission.OWNER }
                    val isMyRoom = currentRoomId == myRoomId

                    // Convert RoomMember to Participant
                    val participants = members.map { member ->
                        Participant(
                            id = member.userId,
                            name = member.userName,
                            isOwner = member.permission == com.dailymemo.domain.models.RoomPermission.OWNER,
                            permission = member.permission,
                            joinedAt = java.time.Instant.ofEpochMilli(member.joinedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                        )
                    }

                    _currentRoom.value = Room(
                        id = currentRoomId.toString(),
                        name = if (isMyRoom) "내 일상 메모" else "${owner?.userName ?: ""}님의 방",
                        ownerId = owner?.userId ?: currentUserId,
                        ownerName = owner?.userName ?: _userName.value,
                        participants = participants,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )

                    android.util.Log.d("ProfileViewModel", "loadCurrentRoom - Room loaded: ${_currentRoom.value?.name}, participants: ${participants.size}")
                },
                onFailure = { error ->
                    android.util.Log.e("ProfileViewModel", "loadCurrentRoom - Failed to fetch room members: ${error.message}")

                    // Fallback: create minimal room info
                    val isMyRoom = currentRoomId == myRoomId
                    _currentRoom.value = Room(
                        id = currentRoomId.toString(),
                        name = if (isMyRoom) "내 일상 메모" else "방",
                        ownerId = if (isMyRoom) currentUserId else 0L,
                        ownerName = if (isMyRoom) _userName.value else "Unknown",
                        participants = listOf(
                            Participant(
                                id = currentUserId,
                                name = _userName.value,
                                isOwner = isMyRoom,
                                permission = if (isMyRoom) com.dailymemo.domain.models.RoomPermission.OWNER else com.dailymemo.domain.models.RoomPermission.READ_WRITE,
                                joinedAt = LocalDateTime.now()
                            )
                        ),
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                }
            )
        }
    }

    fun isOwner(): Boolean {
        return _currentRoom.value?.ownerId == _currentUserId.value
    }

    fun refreshRoomInfo() {
        android.util.Log.d("ProfileViewModel", "refreshRoomInfo - Reloading room info")
        loadCurrentRoom()
        loadProfile() // Also reload profile to get latest room password and info
    }

    fun onRoomIdInputChange(input: String) {
        _roomIdInput.value = input
        _joinRoomError.value = null
    }

    fun onRoomPasswordInputChange(input: String) {
        _roomPasswordInput.value = input
        _joinRoomError.value = null
    }

    fun showJoinDialog() {
        _showJoinDialog.value = true
        _joinRoomError.value = null
    }

    fun hideJoinDialog() {
        _showJoinDialog.value = false
        _roomIdInput.value = ""
        _roomPasswordInput.value = ""
        _joinRoomError.value = null
    }

    fun joinRoom(roomId: String, password: String) {
        if (roomId.isBlank()) {
            _joinRoomError.value = "방 ID를 입력해주세요"
            return
        }

        if (password.isBlank()) {
            _joinRoomError.value = "방 비밀번호를 입력해주세요"
            return
        }

        viewModelScope.launch {
            try {
                val roomIdLong = roomId.toLongOrNull()
                if (roomIdLong == null) {
                    _joinRoomError.value = "잘못된 방 ID 형식입니다"
                    return@launch
                }

                val result = joinRoomUseCase(roomIdLong, password)
                result.fold(
                    onSuccess = {
                        // 방 참여 성공 시 현재 방 ID 설정 (메모 필터링을 위해)
                        authLocalDataSource.setCurrentRoomId(roomIdLong.toInt())
                        hideJoinDialog()
                        // 방 정보 새로고침
                        loadCurrentRoom()
                    },
                    onFailure = { error ->
                        _joinRoomError.value = when (error) {
                            is com.dailymemo.domain.error.DomainError.RoomNotFound ->
                                "존재하지 않는 방입니다\n방 ID를 확인해주세요"
                            is com.dailymemo.domain.error.DomainError.InvalidRoomPassword ->
                                "비밀번호가 일치하지 않습니다\n방 주인에게 비밀번호를 확인해주세요"
                            is com.dailymemo.domain.error.DomainError.NoConnection ->
                                "인터넷 연결을 확인해주세요"
                            is com.dailymemo.domain.error.DomainError.Timeout ->
                                "서버 응답 시간 초과\n잠시 후 다시 시도해주세요"
                            is com.dailymemo.domain.error.DomainError.NetworkError ->
                                "네트워크 오류가 발생했습니다\n연결 상태를 확인해주세요"
                            else ->
                                "방 참여에 실패했습니다\n잠시 후 다시 시도해주세요"
                        }
                    }
                )
            } catch (e: Exception) {
                _joinRoomError.value = "예상치 못한 오류가 발생했습니다\n${e.message}"
            }
        }
    }

    fun leaveRoom() {
        if (isOwner()) {
            // 방장은 나갈 수 없음
            return
        }

        viewModelScope.launch {
            // Reset to my default room
            _myRoomId.value?.let { myRoomId ->
                authLocalDataSource.setCurrentRoomId(myRoomId)
                // TODO: 백엔드 연동 시 실제 방 나가기 API 호출
                loadCurrentRoom()
            }
        }
    }

    fun kickParticipant(participantId: Long) {
        if (!isOwner()) {
            // 방장만 추방 가능
            return
        }

        viewModelScope.launch {
            val currentRoom = _currentRoom.value ?: return@launch
            val roomId = currentRoom.id.toLongOrNull() ?: return@launch

            // Call kick user API
            kickUserUseCase(roomId, participantId).fold(
                onSuccess = {
                    android.util.Log.d("ProfileViewModel", "User $participantId kicked successfully")
                    // Reload room info to get fresh data from backend
                    loadCurrentRoom()
                },
                onFailure = { error ->
                    android.util.Log.e("ProfileViewModel", "Failed to kick user: ${error.message}")
                    // TODO: Show error message to user
                }
            )
        }
    }

    fun updateMemberPermission(userId: Long, permission: com.dailymemo.domain.models.RoomPermission) {
        viewModelScope.launch {
            val currentRoom = _currentRoom.value ?: return@launch
            val roomId = currentRoom.id.toLongOrNull() ?: return@launch

            // Call update permission API
            updateMemberPermissionUseCase(roomId, userId, permission).fold(
                onSuccess = {
                    android.util.Log.d("ProfileViewModel", "User $userId permission updated to $permission")
                    // Reload room info to get fresh data from backend
                    loadCurrentRoom()
                },
                onFailure = { error ->
                    android.util.Log.e("ProfileViewModel", "Failed to update permission: ${error.message}")
                    // TODO: Show error message to user
                }
            )
        }
    }

    fun updateRoomPublic(isPublic: Boolean) {
        viewModelScope.launch {
            val currentRoom = _currentRoom.value ?: return@launch
            val roomId = currentRoom.id.toLongOrNull() ?: return@launch

            updateRoomSettingsUseCase(roomId, isPublic = isPublic).fold(
                onSuccess = {
                    android.util.Log.d("ProfileViewModel", "Room public setting updated to $isPublic")
                    // Reload room info to get fresh data from backend
                    loadCurrentRoom()
                },
                onFailure = { error ->
                    android.util.Log.e("ProfileViewModel", "Failed to update room settings: ${error.message}")
                    // TODO: Show error message to user
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Execute logout use case (clears all tokens, room_id, user info)
            logoutUseCase()

            // Reset all ViewModel states to initial values
            _uiState.value = ProfileUiState.Loading
            _nickname.value = ""
            _currentPassword.value = ""
            _newPassword.value = ""
            _confirmPassword.value = ""
            _selectedImageUri.value = null
            _profileImageUrl.value = null
            _roomPassword.value = ""
            _receivedLikesCount.value = 0
            _isLoading.value = false
            _errorMessage.value = null
            _successMessage.value = null
            _memosWithLocation.value = emptyList()
            _likedRooms.value = emptyList()
            _userName.value = "사용자"
            _userEmail.value = "user@example.com"
            _memoCount.value = 0
            _currentRoom.value = null
            _currentUserId.value = 1L
            _roomIdInput.value = ""
            _roomPasswordInput.value = ""
            _showJoinDialog.value = false
            _joinRoomError.value = null
        }
    }
}
