package com.dailymemo.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Participant
import com.dailymemo.domain.models.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    // TODO: UserRepository, RoomRepository 추가 시 주입
) : ViewModel() {

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
