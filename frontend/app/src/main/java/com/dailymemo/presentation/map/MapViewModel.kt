package com.dailymemo.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.domain.models.Location
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.Place
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.usecases.GetMemosUseCase
import com.dailymemo.domain.usecases.place.SearchPlacesUseCase
import com.dailymemo.domain.usecases.SearchPlacesByCategoryUseCase
import com.dailymemo.domain.usecases.location.GetCurrentLocationUseCase
import com.dailymemo.domain.usecases.location.GetLocationUpdatesUseCase
import com.dailymemo.domain.usecases.room.JoinRoomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getLocationUpdatesUseCase: GetLocationUpdatesUseCase,
    private val getMemosUseCase: GetMemosUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val searchPlacesByCategoryUseCase: SearchPlacesByCategoryUseCase,
    private val joinRoomUseCase: JoinRoomUseCase,
    private val authLocalDataSource: AuthLocalDataSource,
    private val memoRepository: com.dailymemo.domain.repositories.MemoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Place>>(emptyList())
    val searchResults: StateFlow<List<Place>> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    val selectedCategory: StateFlow<PlaceCategory?> = _selectedCategory.asStateFlow()

    private val _showSearchResults = MutableStateFlow(false)
    val showSearchResults: StateFlow<Boolean> = _showSearchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Wishlist filter state
    private val _wishlistFilter = MutableStateFlow<WishlistFilter>(WishlistFilter.ALL)
    val wishlistFilter: StateFlow<WishlistFilter> = _wishlistFilter.asStateFlow()

    // Sort and distance filter states
    private val _sortByDistance = MutableStateFlow(false)
    val sortByDistance: StateFlow<Boolean> = _sortByDistance.asStateFlow()

    private val _distanceFilter = MutableStateFlow<DistanceFilter>(DistanceFilter.ALL)
    val distanceFilter: StateFlow<DistanceFilter> = _distanceFilter.asStateFlow()

    // Computed memos with distance sorting and filtering
    private val _filteredMemos = MutableStateFlow<List<MemoWithDistance>>(emptyList())
    val filteredMemos: StateFlow<List<MemoWithDistance>> = _filteredMemos.asStateFlow()

    // Popup card states (Task 005)
    private val _selectedMemoId = MutableStateFlow<Long?>(null)
    val selectedMemoId: StateFlow<Long?> = _selectedMemoId.asStateFlow()

    private val _showPopupCard = MutableStateFlow(false)
    val showPopupCard: StateFlow<Boolean> = _showPopupCard.asStateFlow()

    // JoinRoom states
    private val _showJoinRoomDialog = MutableStateFlow(false)
    val showJoinRoomDialog: StateFlow<Boolean> = _showJoinRoomDialog.asStateFlow()

    private val _joinRoomState = MutableStateFlow<JoinRoomState>(JoinRoomState.Idle)
    val joinRoomState: StateFlow<JoinRoomState> = _joinRoomState.asStateFlow()

    // Selected place from search results (for marker display)
    private val _selectedSearchPlace = MutableStateFlow<Place?>(null)
    val selectedSearchPlace: StateFlow<Place?> = _selectedSearchPlace.asStateFlow()

    // Room permission state
    private val _canCreateMemo = MutableStateFlow(true)
    val canCreateMemo: StateFlow<Boolean> = _canCreateMemo.asStateFlow()

    init {
        loadMemos()
        startLocationUpdates()
        loadCurrentRoomPermission()

        // Observe memos, location, and filters to update filtered memos
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                _memos,
                _currentLocation,
                _sortByDistance,
                _distanceFilter
            ) { memos, location, sortByDistance, distanceFilter ->
                updateFilteredMemos(memos, location, sortByDistance, distanceFilter)
                Unit // Return Unit since updateFilteredMemos returns nothing
            }.collect { /* nothing to do */ }
        }
    }

    private fun loadCurrentRoomPermission() {
        viewModelScope.launch {
            val permission = authLocalDataSource.getCurrentRoomPermission()
            _canCreateMemo.value = permission != "READ_ONLY"
            android.util.Log.d("MapViewModel", "loadCurrentRoomPermission - permission: $permission, canCreateMemo: ${_canCreateMemo.value}")
        }
    }

    fun refreshPermission() {
        loadCurrentRoomPermission()
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            getCurrentLocationUseCase().fold(
                onSuccess = { location ->
                    _currentLocation.value = location
                    _uiState.value = MapUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Failed to get current location"
                    )
                }
            )
        }
    }

    private var locationJob: kotlinx.coroutines.Job? = null

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            getLocationUpdatesUseCase()
                .catch { error ->
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Failed to get location updates"
                    )
                }
                .collect { location ->
                    _currentLocation.value = location
                }
        }
    }

    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    override fun onCleared() {
        stopLocationUpdates()
        super.onCleared()
    }

    private fun loadMemos() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading

            // Reload permission when loading memos (in case room was switched)
            loadCurrentRoomPermission()

            val isWishlist = when (_wishlistFilter.value) {
                WishlistFilter.ALL -> null
                WishlistFilter.WISHLIST_ONLY -> true
                WishlistFilter.VISITED_ONLY -> false
            }
            // 현재 방의 메모를 조회하기 위해 room_id를 명시적으로 전달
            val currentRoomId = authLocalDataSource.getCurrentRoomId()
            android.util.Log.d("MapViewModel", "loadMemos - currentRoomId: $currentRoomId, isWishlist: $isWishlist")

            memoRepository.getMemos(isWishlist = isWishlist, roomId = currentRoomId, page = 1, limit = 100).fold(
                onSuccess = { result ->
                    _memos.value = result.memos
                    _uiState.value = MapUiState.Success
                    android.util.Log.d("MapViewModel", "loadMemos - success: ${result.memos.size} memos")
                },
                onFailure = { error ->
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Failed to load memos"
                    )
                    android.util.Log.e("MapViewModel", "loadMemos - error: ${error.message}", error)
                }
            )
        }
    }

    fun refreshMemos() {
        loadMemos()
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    // 검색어만 업데이트 (자동 검색 없음)
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // 검색어가 비어있으면 결과 초기화
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _showSearchResults.value = false
        } else {
            // 사용자가 타이핑 중일 때는 목록을 다시 보여줌 (이전 검색 결과가 있다면)
            if (_searchResults.value.isNotEmpty()) {
                _showSearchResults.value = true
            }
        }
    }

    // 디바운싱 로직은 더 이상 사용하지 않음 (수동 검색으로 변경)
    fun onSearchQueryChange(query: String) {
        updateSearchQuery(query)
    }

    fun searchPlaces(query: String) {
        android.util.Log.d("MapViewModel", "searchPlaces called with query: $query")
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _showSearchResults.value = false
            _isSearching.value = false
            android.util.Log.d("MapViewModel", "Query is blank, clearing results")
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            val location = _currentLocation.value
            android.util.Log.d("MapViewModel", "Current location: ${location?.latitude}, ${location?.longitude}")
            android.util.Log.d("MapViewModel", "Searching within 20km radius")

            searchPlacesUseCase(
                query = query,
                longitude = location?.longitude,
                latitude = location?.latitude,
                radius = 20000  // 20km 반경으로 검색
            ).fold(
                onSuccess = { places ->
                    android.util.Log.d("MapViewModel", "Search success: found ${places.size} places")

                    // 검색어 포함 여부와 거리를 기준으로 정렬
                    val sortedPlaces = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                        places.map { place ->
                            // 검색어가 장소명에 포함되어 있는지 확인 (대소문자 무시)
                            val containsQuery = place.name.contains(query, ignoreCase = true)

                            // 거리 계산
                            val distance = if (location != null) {
                                calculateDistance(
                                    location.latitude, location.longitude,
                                    place.latitude, place.longitude
                                )
                            } else {
                                Double.MAX_VALUE
                            }

                            // 우선순위 점수: 검색어 포함 시 -1000000 (음수로 우선순위 높임)
                            val priority = if (containsQuery) -1000000.0 else 0.0

                            Triple(place, priority + distance, containsQuery)
                        }
                        .sortedBy { it.second }  // 우선순위 + 거리로 정렬
                        .also { sorted ->
                            android.util.Log.d("MapViewModel",
                                "Sorted: ${sorted.filter { it.third }.size} matches with query, " +
                                "${sorted.size - sorted.filter { it.third }.size} others")
                        }
                        .map { it.first }
                    }

                    android.util.Log.d("MapViewModel", "Sorted ${sortedPlaces.size} places (query matches first, then by distance)")
                    _searchResults.value = sortedPlaces
                    _showSearchResults.value = true
                    _isSearching.value = false
                },
                onFailure = { error ->
                    android.util.Log.e("MapViewModel", "Search failed: ${error.message}", error)
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Failed to search places"
                    )
                    _isSearching.value = false
                }
            )
        }
    }

    // 두 지점 간 거리 계산 (Haversine formula)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // 지구 반지름 (미터)

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c // 거리 (미터)
    }

    fun searchByCategory(category: PlaceCategory) {
        _selectedCategory.value = category
        val location = _currentLocation.value ?: return

        viewModelScope.launch {
            searchPlacesByCategoryUseCase(
                category = category,
                longitude = location.longitude,
                latitude = location.latitude,
                radius = 20000  // 최대값 20km (카카오 API 제한)
            ).fold(
                onSuccess = { places ->
                    _searchResults.value = places
                    _showSearchResults.value = true
                },
                onFailure = { error ->
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Failed to search places by category"
                    )
                }
            )
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _showSearchResults.value = false
        _isSearching.value = false
        _selectedCategory.value = null
    }

    fun hideSearchResults() {
        _showSearchResults.value = false
    }

    fun selectSearchPlace(place: Place) {
        _selectedSearchPlace.value = place
        android.util.Log.d("MapViewModel", "Selected search place: ${place.name} at ${place.latitude}, ${place.longitude}")
    }

    fun clearSelectedSearchPlace() {
        _selectedSearchPlace.value = null
    }

    fun searchNearbyPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val category = _selectedCategory.value

            if (category == null || category == PlaceCategory.ALL) {
                // 전체 카테고리: 카페, 음식점, 편의점을 검색
                val results = listOf(
                    PlaceCategory.CAFE,
                    PlaceCategory.RESTAURANT,
                    PlaceCategory.CONVENIENCE
                ).mapNotNull { cat ->
                    searchPlacesByCategoryUseCase(
                        category = cat,
                        longitude = longitude,
                        latitude = latitude,
                        radius = 20000 // 최대값 20km (카카오 API 제한)
                    ).getOrNull()
                }.flatten().distinctBy { it.id }

                _searchResults.value = results
            } else {
                // 선택된 카테고리만 검색
                val results = searchPlacesByCategoryUseCase(
                    category = category,
                    longitude = longitude,
                    latitude = latitude,
                    radius = 20000 // 최대값 20km (카카오 API 제한)
                ).getOrNull() ?: emptyList()

                _searchResults.value = results
            }
        }
    }

    fun selectCategory(category: PlaceCategory) {
        _selectedCategory.value = category
        // Re-search with new category
        _currentLocation.value?.let { loc ->
            searchNearbyPlaces(loc.latitude, loc.longitude)
        }
    }

    // Marker and popup functions (Task 003, 005)
    fun onMarkerClick(memoId: Long) {
        _selectedMemoId.value = memoId
    }

    fun onInfoWindowClick() {
        _showPopupCard.value = true
    }

    fun showMemoPopup(memoId: Long) {
        _selectedMemoId.value = memoId
        _showPopupCard.value = true
    }

    fun dismissPopupCard() {
        _showPopupCard.value = false
        _selectedMemoId.value = null
    }

    // JoinRoom functions
    fun showJoinRoomDialog() {
        _showJoinRoomDialog.value = true
        _joinRoomState.value = JoinRoomState.Idle
    }

    fun dismissJoinRoomDialog() {
        _showJoinRoomDialog.value = false
        _joinRoomState.value = JoinRoomState.Idle
    }

    fun joinRoom(roomId: Long, roomPassword: String) {
        viewModelScope.launch {
            _joinRoomState.value = JoinRoomState.Loading
            joinRoomUseCase(roomId, roomPassword).fold(
                onSuccess = {
                    // Update current room ID
                    authLocalDataSource.setCurrentRoomId(roomId.toInt())
                    _joinRoomState.value = JoinRoomState.Success
                    _showJoinRoomDialog.value = false
                    // Refresh memos to show the new room's memos
                    refreshMemos()
                },
                onFailure = { error ->
                    // Provide user-friendly error messages based on error type
                    val errorWithMessage = when (error) {
                        is com.dailymemo.domain.error.DomainError.RoomNotFound ->
                            RuntimeException("존재하지 않는 방입니다\n방 ID를 확인해주세요")
                        is com.dailymemo.domain.error.DomainError.InvalidRoomPassword ->
                            RuntimeException("비밀번호가 일치하지 않습니다\n방 주인에게 비밀번호를 확인해주세요")
                        is com.dailymemo.domain.error.DomainError.NoConnection ->
                            RuntimeException("인터넷 연결을 확인해주세요")
                        is com.dailymemo.domain.error.DomainError.Timeout ->
                            RuntimeException("서버 응답 시간 초과\n잠시 후 다시 시도해주세요")
                        is com.dailymemo.domain.error.DomainError.NetworkError ->
                            RuntimeException("네트워크 오류가 발생했습니다\n연결 상태를 확인해주세요")
                        else ->
                            RuntimeException("방 참여에 실패했습니다\n잠시 후 다시 시도해주세요")
                    }
                    _joinRoomState.value = JoinRoomState.Error(errorWithMessage)
                }
            )
        }
    }

    fun resetJoinRoomState() {
        _joinRoomState.value = JoinRoomState.Idle
    }

    // Wishlist filter functions
    fun setWishlistFilter(filter: WishlistFilter) {
        _wishlistFilter.value = filter
        loadMemos()
    }

    // Distance sort and filter functions
    fun toggleSortByDistance() {
        _sortByDistance.value = !_sortByDistance.value
    }

    fun setDistanceFilter(filter: DistanceFilter) {
        _distanceFilter.value = filter
    }

    private fun updateFilteredMemos(
        memos: List<Memo>,
        location: Location?,
        sortByDistance: Boolean,
        distanceFilter: DistanceFilter
    ) {
        if (location == null) {
            // No location available, just map to MemoWithDistance with null distance
            _filteredMemos.value = memos.map { MemoWithDistance(it, null) }
            return
        }

        // Calculate distance for each memo
        val memosWithDistance = memos.mapNotNull { memo ->
            val memoLat = memo.latitude
            val memoLon = memo.longitude

            if (memoLat != null && memoLon != null) {
                val distance = calculateDistance(
                    location.latitude, location.longitude,
                    memoLat, memoLon
                )
                MemoWithDistance(memo, distance)
            } else {
                // Memo without location
                MemoWithDistance(memo, null)
            }
        }

        // Apply distance filter
        val filtered = when (distanceFilter) {
            DistanceFilter.ALL -> memosWithDistance
            DistanceFilter.WITHIN_5KM -> memosWithDistance.filter { it.distance?.let { d -> d <= 5000 } ?: false }
            DistanceFilter.WITHIN_10KM -> memosWithDistance.filter { it.distance?.let { d -> d <= 10000 } ?: false }
            DistanceFilter.WITHIN_20KM -> memosWithDistance.filter { it.distance?.let { d -> d <= 20000 } ?: false }
        }

        // Apply sorting
        val sorted = if (sortByDistance) {
            filtered.sortedBy { it.distance ?: Double.MAX_VALUE }
        } else {
            filtered // Keep original order (by creation date)
        }

        _filteredMemos.value = sorted
    }

    fun formatDistance(distance: Double?): String {
        if (distance == null) return ""

        return when {
            distance < 1000 -> "${distance.toInt()}m"
            else -> "%.1fkm".format(distance / 1000)
        }
    }
}

// Data class to hold memo with its distance
data class MemoWithDistance(
    val memo: Memo,
    val distance: Double? // in meters, null if location not available
)

sealed class MapUiState {
    data object Loading : MapUiState()
    data object Success : MapUiState()
    data class Error(val message: String) : MapUiState()
}

sealed class JoinRoomState {
    data object Idle : JoinRoomState()
    data object Loading : JoinRoomState()
    data object Success : JoinRoomState()
    data class Error(val error: Throwable) : JoinRoomState()
}

enum class WishlistFilter {
    ALL,           // 전체 (wishlist + visited)
    WISHLIST_ONLY, // 가고싶은 곳만
    VISITED_ONLY   // 방문한 곳만
}

enum class DistanceFilter {
    ALL,           // 모든 거리
    WITHIN_5KM,    // 5km 이내
    WITHIN_10KM,   // 10km 이내
    WITHIN_20KM    // 20km 이내
}
