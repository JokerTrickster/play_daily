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
    private val searchPlacesByCategoryUseCase: SearchPlacesByCategoryUseCase
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

    // Popup card states (Task 005)
    private val _selectedMemoId = MutableStateFlow<Long?>(null)
    val selectedMemoId: StateFlow<Long?> = _selectedMemoId.asStateFlow()

    private val _showPopupCard = MutableStateFlow(false)
    val showPopupCard: StateFlow<Boolean> = _showPopupCard.asStateFlow()

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

    private fun startLocationUpdates() {
        viewModelScope.launch {
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

    private fun loadMemos() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            getMemosUseCase().fold(
                onSuccess = { memos ->
                    _memos.value = memos
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun searchPlaces(query: String) {
        android.util.Log.d("MapViewModel", "searchPlaces called with query: $query")
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _showSearchResults.value = false
            android.util.Log.d("MapViewModel", "Query is blank, clearing results")
            return
        }

        viewModelScope.launch {
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

                    // 현재 위치 기준으로 거리순 정렬
                    val sortedPlaces = if (location != null) {
                        places.sortedBy { place ->
                            calculateDistance(
                                location.latitude, location.longitude,
                                place.latitude, place.longitude
                            )
                        }
                    } else {
                        places
                    }

                    android.util.Log.d("MapViewModel", "Sorted ${sortedPlaces.size} places by distance")
                    _searchResults.value = sortedPlaces
                    _showSearchResults.value = true
                },
                onFailure = { error ->
                    android.util.Log.e("MapViewModel", "Search failed: ${error.message}", error)
                    _uiState.value = MapUiState.Error(
                        error.message ?: "Failed to search places"
                    )
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
        _selectedCategory.value = null
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

    fun dismissPopupCard() {
        _showPopupCard.value = false
    }
}

sealed class MapUiState {
    data object Loading : MapUiState()
    data object Success : MapUiState()
    data class Error(val message: String) : MapUiState()
}
