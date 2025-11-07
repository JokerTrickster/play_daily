package com.dailymemo.presentation.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.error.DomainError
import com.dailymemo.domain.repositories.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinRoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val authLocalDataSource: com.dailymemo.data.datasources.local.AuthLocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinRoomUiState())
    val uiState: StateFlow<JoinRoomUiState> = _uiState.asStateFlow()

    private val _roomCode = MutableStateFlow("")
    val roomCode: StateFlow<String> = _roomCode.asStateFlow()

    private var pendingRoomId: Long? = null

    fun updateRoomCode(code: String) {
        // Only allow alphanumeric characters and limit to 8 chars
        val filtered = code.filter { it.isLetterOrDigit() }.uppercase().take(8)
        _roomCode.value = filtered
        // Clear error when user types
        if (_uiState.value.error != null) {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }

    fun joinByCode() {
        val code = _roomCode.value.trim()
        if (code.length < 8) {
            _uiState.value = _uiState.value.copy(error = "방 코드는 8자여야 합니다")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // Extract room ID from room code (assuming code format: first part is ID)
        // For now, we'll try to parse the code as a number directly
        // The backend should handle room code to ID mapping
        val roomId = code.toLongOrNull()
        if (roomId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "잘못된 방 코드 형식입니다"
            )
            return
        }

        pendingRoomId = roomId

        viewModelScope.launch {
            // Try joining without password first (in case it's public)
            roomRepository.joinRoom(roomId = roomId, roomPassword = "").fold(
                onSuccess = {
                    handleJoinSuccess(roomId)
                },
                onFailure = { error ->
                    handleJoinError(error)
                }
            )
        }
    }

    fun showPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showPasswordModal = true,
            isLoading = false
        )
    }

    fun submitPassword(password: String) {
        val roomId = pendingRoomId ?: return

        _uiState.value = _uiState.value.copy(isPasswordLoading = true, passwordError = null)

        viewModelScope.launch {
            roomRepository.joinRoom(roomId = roomId, roomPassword = password).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        showPasswordModal = false,
                        isPasswordLoading = false
                    )
                    handleJoinSuccess(roomId)
                },
                onFailure = { error ->
                    when (error) {
                        is DomainError.InvalidRoomPassword -> {
                            _uiState.value = _uiState.value.copy(
                                isPasswordLoading = false,
                                passwordError = "비밀번호가 일치하지 않습니다"
                            )
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(
                                showPasswordModal = false,
                                isPasswordLoading = false
                            )
                            handleJoinError(error)
                        }
                    }
                }
            )
        }
    }

    private fun handleJoinSuccess(roomId: Long) {
        // Set the new room as current
        authLocalDataSource.setCurrentRoomId(roomId.toInt())
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            joinedRoomId = roomId
        )
    }

    private fun handleJoinError(error: Throwable) {
        val errorMessage = when (error) {
            is DomainError.RoomNotFound ->
                "존재하지 않는 방입니다\n방 코드를 확인해주세요"
            is DomainError.InvalidRoomPassword -> {
                // Show password modal
                showPasswordDialog()
                return
            }
            is DomainError.NoConnection ->
                "인터넷 연결을 확인해주세요"
            is DomainError.Timeout ->
                "서버 응답 시간 초과\n잠시 후 다시 시도해주세요"
            is DomainError.NetworkError ->
                "네트워크 오류가 발생했습니다"
            else ->
                "방 참여에 실패했습니다\n${error.message ?: ""}"
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = errorMessage
        )
    }

    fun cancelPasswordInput() {
        _uiState.value = _uiState.value.copy(
            showPasswordModal = false,
            isPasswordLoading = false,
            passwordError = null,
            isLoading = false
        )
        pendingRoomId = null
    }

    fun clearJoinedRoom() {
        _uiState.value = _uiState.value.copy(joinedRoomId = null)
        _roomCode.value = ""
        pendingRoomId = null
    }
}

data class JoinRoomUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPasswordModal: Boolean = false,
    val isPasswordLoading: Boolean = false,
    val passwordError: String? = null,
    val joinedRoomId: Long? = null
)
