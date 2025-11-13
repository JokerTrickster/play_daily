package com.dailymemo.presentation.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.MemoCategory
import com.dailymemo.domain.models.Profile
import com.dailymemo.domain.models.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomDiscoveryViewModel @Inject constructor(
    private val getPopularRoomsUseCase: com.dailymemo.domain.usecases.room.GetPopularRoomsUseCase,
    private val getRecentRoomsUseCase: com.dailymemo.domain.usecases.room.GetRecentRoomsUseCase,
    private val resetRoomPasswordUseCase: com.dailymemo.domain.usecases.room.ResetRoomPasswordUseCase,
    private val getRoomDetailUseCase: com.dailymemo.domain.usecases.room.GetRoomDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RoomDiscoveryUiState>(RoomDiscoveryUiState.Loading)
    val uiState: StateFlow<RoomDiscoveryUiState> = _uiState.asStateFlow()

    private val _currentTab = MutableStateFlow(RoomTab.POPULAR)
    val currentTab: StateFlow<RoomTab> = _currentTab.asStateFlow()

    // Pagination state
    private var currentPage = 1
    private val pageSize = 10  // Limited to 10 items per page for better UX
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // User search state
    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    private val _userSearchState = MutableStateFlow<UserSearchState>(UserSearchState.Idle)
    val userSearchState: StateFlow<UserSearchState> = _userSearchState.asStateFlow()

    private var searchJob: Job? = null

    // Pull-to-refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Room detail state
    private val _selectedRoomDetail = MutableStateFlow<RoomDetail?>(null)
    val selectedRoomDetail: StateFlow<RoomDetail?> = _selectedRoomDetail.asStateFlow()

    private val _showRoomDetailModal = MutableStateFlow(false)
    val showRoomDetailModal: StateFlow<Boolean> = _showRoomDetailModal.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    init {
        loadRooms()
    }

    fun switchTab(tab: RoomTab) {
        _currentTab.value = tab
        _searchQuery.value = "" // Clear search when switching tabs
        _userSearchQuery.value = "" // Clear user search
        _userSearchState.value = UserSearchState.Idle // Reset user search state
        searchJob?.cancel() // Cancel any pending search
        currentPage = 1
        _hasMore.value = true
        loadRooms()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (_currentTab.value == RoomTab.SEARCH) {
            currentPage = 1
            loadRooms()
        }
    }

    fun onUserSearchQueryChange(query: String) {
        _userSearchQuery.value = query

        // Cancel previous search job
        searchJob?.cancel()

        if (query.isBlank()) {
            _userSearchState.value = UserSearchState.Idle
            return
        }

        // Debounce search: wait 300ms before searching
        searchJob = viewModelScope.launch {
            delay(300)
            searchUser(query)
        }
    }

    private fun searchUser(userId: String) {
        viewModelScope.launch {
            _userSearchState.value = UserSearchState.Loading

            try {
                // TODO: Backend API for user search by account_id does not exist yet
                // For now, show error message that this feature is not available
                delay(300) // Small delay to show loading state

                _userSearchState.value = UserSearchState.Error(
                    "사용자 검색 기능은 준비 중입니다. 백엔드 API 구현이 필요합니다."
                )
            } catch (e: Exception) {
                _userSearchState.value = UserSearchState.Error(e.message ?: "검색 중 오류가 발생했습니다")
            }
        }
    }

    fun loadRooms() {
        viewModelScope.launch {
            if (currentPage == 1) {
                _uiState.value = RoomDiscoveryUiState.Loading
            }

            try {
                when (_currentTab.value) {
                    RoomTab.POPULAR -> {
                        getPopularRoomsUseCase(currentPage, pageSize).fold(
                            onSuccess = { (rooms, hasNext) ->
                                _uiState.value = RoomDiscoveryUiState.Success(rooms)
                                _hasMore.value = hasNext
                                _isRefreshing.value = false
                            },
                            onFailure = { error ->
                                _uiState.value = RoomDiscoveryUiState.Error(
                                    error.message ?: "방 목록을 불러오는데 실패했습니다"
                                )
                                _isRefreshing.value = false
                            }
                        )
                    }
                    RoomTab.NEW -> {
                        getRecentRoomsUseCase(currentPage, pageSize).fold(
                            onSuccess = { (rooms, hasNext) ->
                                _uiState.value = RoomDiscoveryUiState.Success(rooms)
                                _hasMore.value = hasNext
                                _isRefreshing.value = false
                            },
                            onFailure = { error ->
                                _uiState.value = RoomDiscoveryUiState.Error(
                                    error.message ?: "방 목록을 불러오는데 실패했습니다"
                                )
                                _isRefreshing.value = false
                            }
                        )
                    }
                    RoomTab.SEARCH -> {
                        // Search functionality - use popular for now until backend implements search
                        if (_searchQuery.value.isNotBlank()) {
                            getPopularRoomsUseCase(currentPage, pageSize).fold(
                                onSuccess = { (rooms, hasNext) ->
                                    // Client-side filtering by search query
                                    val filteredRooms = rooms.filter { room ->
                                        room.title.contains(_searchQuery.value, ignoreCase = true) ||
                                        room.description.contains(_searchQuery.value, ignoreCase = true)
                                    }
                                    _uiState.value = RoomDiscoveryUiState.Success(filteredRooms)
                                    _hasMore.value = hasNext
                                    _isRefreshing.value = false
                                },
                                onFailure = { error ->
                                    _uiState.value = RoomDiscoveryUiState.Error(
                                        error.message ?: "방 검색에 실패했습니다"
                                    )
                                    _isRefreshing.value = false
                                }
                            )
                        } else {
                            _uiState.value = RoomDiscoveryUiState.Success(emptyList())
                            _isRefreshing.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = RoomDiscoveryUiState.Error(
                    e.message ?: "알 수 없는 오류가 발생했습니다"
                )
                _isRefreshing.value = false
            }
        }
    }

    fun loadMoreRooms() {
        if (!_hasMore.value || _isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++

            try {
                val currentRooms = (_uiState.value as? RoomDiscoveryUiState.Success)?.rooms ?: emptyList()

                when (_currentTab.value) {
                    RoomTab.POPULAR -> {
                        getPopularRoomsUseCase(currentPage, pageSize).fold(
                            onSuccess = { (newRooms, hasNext) ->
                                _uiState.value = RoomDiscoveryUiState.Success(currentRooms + newRooms)
                                _hasMore.value = hasNext
                                _isLoadingMore.value = false
                            },
                            onFailure = {
                                // Keep current state on error, just stop loading
                                _hasMore.value = false
                                _isLoadingMore.value = false
                            }
                        )
                    }
                    RoomTab.NEW -> {
                        getRecentRoomsUseCase(currentPage, pageSize).fold(
                            onSuccess = { (newRooms, hasNext) ->
                                _uiState.value = RoomDiscoveryUiState.Success(currentRooms + newRooms)
                                _hasMore.value = hasNext
                                _isLoadingMore.value = false
                            },
                            onFailure = {
                                // Keep current state on error, just stop loading
                                _hasMore.value = false
                                _isLoadingMore.value = false
                            }
                        )
                    }
                    RoomTab.SEARCH -> {
                        // For search, no pagination support yet (client-side filtering)
                        _hasMore.value = false
                        _isLoadingMore.value = false
                    }
                }
            } catch (e: Exception) {
                _hasMore.value = false
                _isLoadingMore.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            currentPage = 1
            _hasMore.value = true
            loadRooms()
        }
    }

    /**
     * Reset room password - returns new password if successful
     */
    suspend fun resetPassword(roomId: Long): Result<String> {
        return resetRoomPasswordUseCase(roomId)
    }

    /**
     * Load room detail and show modal
     */
    fun onRoomClick(roomId: String) {
        viewModelScope.launch {
            _isLoadingDetail.value = true

            try {
                val roomIdLong = roomId.toLongOrNull() ?: return@launch

                getRoomDetailUseCase(roomIdLong).fold(
                    onSuccess = { roomDetail ->
                        _selectedRoomDetail.value = roomDetail
                        _showRoomDetailModal.value = true
                        _isLoadingDetail.value = false
                    },
                    onFailure = { error ->
                        // Log error but don't show modal if failed
                        _isLoadingDetail.value = false
                    }
                )
            } catch (e: Exception) {
                _isLoadingDetail.value = false
            }
        }
    }

    /**
     * Hide room detail modal
     */
    fun hideRoomDetailModal() {
        _showRoomDetailModal.value = false
        _selectedRoomDetail.value = null
    }

    /**
     * Handle join room action from detail modal
     */
    fun onJoinRoom(roomId: String) {
        // TODO: Implement join room logic
        // This should navigate to room or handle join flow
        hideRoomDetailModal()
    }
}

sealed class RoomDiscoveryUiState {
    object Loading : RoomDiscoveryUiState()
    data class Success(val rooms: List<RoomCardData>) : RoomDiscoveryUiState()
    data class Error(val message: String) : RoomDiscoveryUiState()
}

sealed class UserSearchState {
    object Idle : UserSearchState()
    object Loading : UserSearchState()
    data class Success(val user: Profile) : UserSearchState()
    object NotFound : UserSearchState()
    data class Error(val message: String) : UserSearchState()
}

// Temporary data class for room cards - will be replaced with proper Room model in task #63
data class RoomCardData(
    val id: String,
    val title: String,
    val description: String,
    val participantCount: Int,
    val categories: List<MemoCategory>,
    val thumbnailUrl: String?,
    val ownerName: String,
    val isPublic: Boolean
)
