package com.dailymemo.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Location
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.Place
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.usecases.GetMemosUseCase
import com.dailymemo.domain.usecases.SearchPlacesUseCase
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
    private val joinRoomUseCase: JoinRoomUseCase
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

    init {
        loadMemos()
        startLocationUpdates()
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
            val isWishlist = when (_wishlistFilter.value) {
                WishlistFilter.ALL -> null
                WishlistFilter.WISHLIST_ONLY -> true
                WishlistFilter.VISITED_ONLY -> false
            }
            getMemosUseCase(isWishlist = isWishlist, page = 1, limit = 100).fold(
                onSuccess = { result ->
                    _memos.value = result.memos
                    _uiState.value = MapUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Failed to load memos"
                    )
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

                    // 현재 위치 기준으로 거리순 정렬 (백그라운드 스레드에서 실행)
                    val sortedPlaces = if (location != null) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                            places.map { place ->
                                place to calculateDistance(
                                    location.latitude, location.longitude,
                                    place.latitude, place.longitude
                                )
                            }.sortedBy { it.second }
                             .map { it.first }
                        }
                    } else {
                        places
                    }

                    android.util.Log.d("MapViewModel", "Sorted ${sortedPlaces.size} places by distance")
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
                    _joinRoomState.value = JoinRoomState.Success
                    _showJoinRoomDialog.value = false
                    // Refresh memos to show the new room's memos
                    refreshMemos()
                },
                onFailure = { error ->
                    _joinRoomState.value = JoinRoomState.Error(error)
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
}

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
