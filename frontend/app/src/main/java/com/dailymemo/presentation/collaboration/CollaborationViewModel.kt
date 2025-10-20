package com.dailymemo.presentation.collaboration

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
class CollaborationViewModel @Inject constructor(
    // TODO: RoomRepository 추가 시 주입
) : ViewModel() {

    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()

    private val _currentUserId = MutableStateFlow(1L) // TODO: 실제 사용자 ID로 변경
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    private val _roomIdInput = MutableStateFlow("")
    val roomIdInput: StateFlow<String> = _roomIdInput.asStateFlow()

    private val _showJoinDialog = MutableStateFlow(false)
    val showJoinDialog: StateFlow<Boolean> = _showJoinDialog.asStateFlow()

    init {
        loadCurrentRoom()
    }

    private fun loadCurrentRoom() {
        viewModelScope.launch {
            // TODO: 백엔드 연동 시 실제 데이터 로드
            // 현재는 내 방을 기본으로 설정
            _currentRoom.value = Room(
                id = "room_1",
                name = "내 일상 메모",
                ownerId = 1L,
                ownerName = "나",
                participants = listOf(
                    Participant(
                        id = 1L,
                        name = "나",
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
                        id = 1L,
                        name = "나",
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
            // 내 방으로 돌아가기
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
}
