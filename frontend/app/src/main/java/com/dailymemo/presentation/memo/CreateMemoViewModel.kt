package com.dailymemo.presentation.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Location
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.usecases.CreateMemoUseCase
import com.dailymemo.domain.usecases.location.GetCurrentLocationUseCase
import com.dailymemo.utils.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMemoViewModel @Inject constructor(
    private val createMemoUseCase: CreateMemoUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val searchPlacesUseCase: com.dailymemo.domain.usecases.place.SearchPlacesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateMemoUiState>(CreateMemoUiState.Initial)
    val uiState: StateFlow<CreateMemoUiState> = _uiState.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl.asStateFlow()

    private val _rating = MutableStateFlow(0)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _locationName = MutableStateFlow("")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _category = MutableStateFlow<PlaceCategory?>(null)
    val category: StateFlow<PlaceCategory?> = _category.asStateFlow()

    private val _isWishlist = MutableStateFlow(false)
    val isWishlist: StateFlow<Boolean> = _isWishlist.asStateFlow()

    private val _businessName = MutableStateFlow("")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _businessPhone = MutableStateFlow("")
    val businessPhone: StateFlow<String> = _businessPhone.asStateFlow()

    private val _businessAddress = MutableStateFlow("")
    val businessAddress: StateFlow<String> = _businessAddress.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.dailymemo.domain.models.Place>>(emptyList())
    val searchResults: StateFlow<List<com.dailymemo.domain.models.Place>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _showSearchDialog = MutableStateFlow(false)
    val showSearchDialog: StateFlow<Boolean> = _showSearchDialog.asStateFlow()

    init {
        // Automatically get current location when creating memo
        getCurrentLocation()
    }

    fun setWishlistMode(isWishlist: Boolean) {
        android.util.Log.d("CreateMemoVM", "setWishlistMode called with: $isWishlist")
        _isWishlist.value = isWishlist
        android.util.Log.d("CreateMemoVM", "_isWishlist.value is now: ${_isWishlist.value}")
    }

    fun onBusinessNameChange(newName: String) {
        _businessName.value = newName
    }

    fun onBusinessPhoneChange(newPhone: String) {
        _businessPhone.value = newPhone
    }

    fun onBusinessAddressChange(newAddress: String) {
        _businessAddress.value = newAddress
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            getCurrentLocationUseCase().fold(
                onSuccess = { location ->
                    _currentLocation.value = location
                    // You can add reverse geocoding here to get location name
                    _locationName.value = "현재 위치" // Placeholder
                },
                onFailure = {
                    // Silently fail - location is optional
                }
            )
        }
    }

    fun onLocationNameChange(newName: String) {
        _locationName.value = newName
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
        _uiState.value = CreateMemoUiState.Initial
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
        _uiState.value = CreateMemoUiState.Initial
    }

    fun onImageUrlChange(newUrl: String) {
        _imageUrl.value = newUrl
    }

    fun onRatingChange(newRating: Int) {
        _rating.value = newRating
    }

    fun togglePin() {
        _isPinned.value = !_isPinned.value
    }

    fun onCategoryChange(newCategory: PlaceCategory?) {
        _category.value = newCategory
    }

    fun setPlaceLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = Location(latitude, longitude)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun openSearchDialog() {
        _showSearchDialog.value = true
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun closeSearchDialog() {
        _showSearchDialog.value = false
    }

    fun searchPlaces() {
        if (_searchQuery.value.isBlank()) return

        viewModelScope.launch {
            _isSearching.value = true
            searchPlacesUseCase(
                query = _searchQuery.value,
                longitude = _currentLocation.value?.longitude,
                latitude = _currentLocation.value?.latitude
            ).fold(
                onSuccess = { places ->
                    _searchResults.value = places
                    _isSearching.value = false
                },
                onFailure = { error ->
                    _isSearching.value = false
                    _searchResults.value = emptyList()
                }
            )
        }
    }

    private val _naverPlaceUrl = MutableStateFlow("")
    val naverPlaceUrl: StateFlow<String> = _naverPlaceUrl.asStateFlow()

    fun selectPlace(place: com.dailymemo.domain.models.Place) {
        _locationName.value = place.name
        _currentLocation.value = Location(place.latitude, place.longitude)
        _businessName.value = place.name
        _businessPhone.value = place.phone ?: ""
        _businessAddress.value = place.address
        // Generate Naver Place URL
        _naverPlaceUrl.value = "https://m.place.naver.com/place/search?query=${java.net.URLEncoder.encode(place.name, "UTF-8")}"
        closeSearchDialog()
    }

    fun createMemo() {
        if (_title.value.isBlank() || _content.value.isBlank()) {
            _uiState.value = CreateMemoUiState.Error("제목과 내용을 입력해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateMemoUiState.Loading

            android.util.Log.d("CreateMemoVM", "createMemo called - isWishlist: ${_isWishlist.value}")

            createMemoUseCase(
                title = _title.value.trim(),
                content = _content.value.trim(),
                imageUrl = if (_imageUrl.value.isNotBlank()) _imageUrl.value.trim() else null,
                rating = _rating.value,
                isPinned = _isPinned.value,
                latitude = _currentLocation.value?.latitude,
                longitude = _currentLocation.value?.longitude,
                locationName = if (_locationName.value.isNotBlank()) _locationName.value.trim() else null,
                category = _category.value,
                isWishlist = _isWishlist.value,
                businessName = if (_businessName.value.isNotBlank()) _businessName.value.trim() else null,
                businessPhone = if (_businessPhone.value.isNotBlank()) _businessPhone.value.trim() else null,
                businessAddress = if (_businessAddress.value.isNotBlank()) _businessAddress.value.trim() else null,
                naverPlaceUrl = if (_naverPlaceUrl.value.isNotBlank()) _naverPlaceUrl.value.trim() else null
            ).fold(
                onSuccess = {
                    android.util.Log.d("CreateMemoVM", "Memo created successfully")
                    _uiState.value = CreateMemoUiState.Success
                },
                onFailure = { error ->
                    android.util.Log.e("CreateMemoVM", "Failed to create memo: ${error.message}")
                    _uiState.value = CreateMemoUiState.Error(ErrorHandler.Memo.createError(error))
                }
            )
        }
    }
}

sealed class CreateMemoUiState {
    object Initial : CreateMemoUiState()
    object Loading : CreateMemoUiState()
    object Success : CreateMemoUiState()
    data class Error(val message: String) : CreateMemoUiState()
}
