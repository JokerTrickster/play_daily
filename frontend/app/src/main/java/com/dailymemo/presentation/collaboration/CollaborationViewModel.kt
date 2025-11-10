package com.dailymemo.presentation.collaboration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.domain.models.RoomMember
import com.dailymemo.domain.models.RoomPermission
import com.dailymemo.domain.usecases.room.GetRoomMembersUseCase
import com.dailymemo.domain.usecases.room.KickUserUseCase
import com.dailymemo.domain.usecases.room.UpdateMemberPermissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollaborationViewModel @Inject constructor(
    private val getRoomMembersUseCase: GetRoomMembersUseCase,
    private val updateMemberPermissionUseCase: UpdateMemberPermissionUseCase,
    private val kickUserUseCase: KickUserUseCase,
    private val authLocalDataSource: AuthLocalDataSource
) : ViewModel() {

    private val _members = MutableStateFlow<List<RoomMember>>(emptyList())
    val members: StateFlow<List<RoomMember>> = _members.asStateFlow()

    private val _currentUserId = MutableStateFlow(0L)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _selectedMember = MutableStateFlow<RoomMember?>(null)
    val selectedMember: StateFlow<RoomMember?> = _selectedMember.asStateFlow()

    private val _showKickDialog = MutableStateFlow(false)
    val showKickDialog: StateFlow<Boolean> = _showKickDialog.asStateFlow()

    private val _kickTargetMember = MutableStateFlow<RoomMember?>(null)
    val kickTargetMember: StateFlow<RoomMember?> = _kickTargetMember.asStateFlow()

    init {
        loadCurrentUser()
        loadMembers()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = authLocalDataSource.getUserId()?.toLongOrNull() ?: 0L
            _currentUserId.value = userId
        }
    }

    fun loadMembers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Get current room ID
            val roomId = authLocalDataSource.getCurrentRoomId()
            if (roomId == null) {
                _error.value = "현재 방 정보를 찾을 수 없습니다"
                _isLoading.value = false
                return@launch
            }

            getRoomMembersUseCase(roomId.toLong()).fold(
                onSuccess = { membersList ->
                    _members.value = membersList
                    _isLoading.value = false
                    Log.d("CollaborationViewModel", "Loaded ${membersList.size} members for room $roomId")
                },
                onFailure = { error ->
                    _error.value = error.message ?: "참여자 목록을 불러올 수 없습니다"
                    _isLoading.value = false
                    Log.e("CollaborationViewModel", "Failed to load members", error)
                }
            )
        }
    }

    fun showPermissionDialog(member: RoomMember) {
        _selectedMember.value = member
        _showPermissionDialog.value = true
    }

    fun hidePermissionDialog() {
        _showPermissionDialog.value = false
        _selectedMember.value = null
    }

    fun showKickDialog(member: RoomMember) {
        _kickTargetMember.value = member
        _showKickDialog.value = true
    }

    fun hideKickDialog() {
        _showKickDialog.value = false
        _kickTargetMember.value = null
    }

    fun updateMemberPermission(userId: Long, newPermission: RoomPermission) {
        viewModelScope.launch {
            _isLoading.value = true

            val roomId = authLocalDataSource.getCurrentRoomId()
            if (roomId == null) {
                _error.value = "현재 방 정보를 찾을 수 없습니다"
                _isLoading.value = false
                return@launch
            }

            updateMemberPermissionUseCase(
                roomId = roomId.toLong(),
                userId = userId,
                permission = newPermission
            ).fold(
                onSuccess = {
                    // Reload members to reflect changes
                    loadMembers()
                    Log.d("CollaborationViewModel", "Updated permission for user $userId to $newPermission")
                },
                onFailure = { error ->
                    _error.value = "권한 변경에 실패했습니다: ${error.message}"
                    _isLoading.value = false
                    Log.e("CollaborationViewModel", "Failed to update permission", error)
                }
            )
        }
    }

    fun kickMember(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            val roomId = authLocalDataSource.getCurrentRoomId()
            if (roomId == null) {
                _error.value = "현재 방 정보를 찾을 수 없습니다"
                _isLoading.value = false
                return@launch
            }

            kickUserUseCase(
                roomId = roomId.toLong(),
                targetUserId = userId
            ).fold(
                onSuccess = {
                    // Reload members to reflect changes
                    loadMembers()
                    Log.d("CollaborationViewModel", "Kicked user $userId from room $roomId")
                },
                onFailure = { error ->
                    _error.value = "추방에 실패했습니다: ${error.message}"
                    _isLoading.value = false
                    Log.e("CollaborationViewModel", "Failed to kick member", error)
                }
            )
        }
    }
}
