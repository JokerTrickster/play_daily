package com.dailymemo.presentation.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.PublicRoom
import com.dailymemo.domain.repositories.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverRoomsViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val authLocalDataSource: com.dailymemo.data.datasources.local.AuthLocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _rooms = MutableStateFlow<List<PublicRoom>>(emptyList())
    val rooms: StateFlow<List<PublicRoom>> = _rooms.asStateFlow()

    private var currentPage = 1
    private var hasMorePages = true
    private val pageSize = 10

    init {
        loadRooms()
    }

    fun loadRooms() {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        currentPage = 1

        viewModelScope.launch {
            roomRepository.getPublicRooms(page = 1, limit = pageSize).fold(
                onSuccess = { result ->
                    _rooms.value = result.rooms
                    hasMorePages = result.hasMore
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "방 목록을 불러올 수 없습니다"
                    )
                }
            )
        }
    }

    fun loadNextPage() {
        if (!hasMorePages || _uiState.value.isLoadingMore) return

        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        currentPage++

        viewModelScope.launch {
            roomRepository.getPublicRooms(page = currentPage, limit = pageSize).fold(
                onSuccess = { result ->
                    _rooms.value = _rooms.value + result.rooms
                    hasMorePages = result.hasMore
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                },
                onFailure = { error ->
                    currentPage-- // Revert page increment
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                }
            )
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        currentPage = 1

        viewModelScope.launch {
            roomRepository.getPublicRooms(page = 1, limit = pageSize).fold(
                onSuccess = { result ->
                    _rooms.value = result.rooms
                    hasMorePages = result.hasMore
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
            )
        }
    }

    fun joinRoom(roomId: Long, roomPassword: String) {
        viewModelScope.launch {
            roomRepository.joinRoom(roomId, roomPassword).fold(
                onSuccess = {
                    // Set the new room as current
                    authLocalDataSource.setCurrentRoomId(roomId.toInt())
                    _uiState.value = _uiState.value.copy(joinedRoomId = roomId)
                },
                onFailure = { error ->
                    android.util.Log.e("DiscoverRoomsViewModel", "Failed to join room: ${error.message}")
                    // TODO: Show error to user
                }
            )
        }
    }

    fun clearJoinedRoom() {
        _uiState.value = _uiState.value.copy(joinedRoomId = null)
    }
}

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val joinedRoomId: Long? = null
)
