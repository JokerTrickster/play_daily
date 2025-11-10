package com.dailymemo.presentation.memo

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.domain.models.Location
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.RoomPermission
import com.dailymemo.domain.usecases.GetMemoUseCase
import com.dailymemo.domain.usecases.UpdateMemoUseCase
import com.dailymemo.domain.usecases.location.GetCurrentLocationUseCase
import com.dailymemo.domain.usecases.place.SearchPlacesUseCase
import com.dailymemo.domain.usecases.room.GetRoomMembersUseCase
import com.dailymemo.domain.repositories.CategoryRepository
import com.dailymemo.domain.repositories.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMemoViewModel @Inject constructor(
    private val getMemoUseCase: GetMemoUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
    private val memoRepository: MemoRepository,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val categoryRepository: CategoryRepository,
    private val getRoomMembersUseCase: GetRoomMembersUseCase,
    private val authLocalDataSource: AuthLocalDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoId: Long = savedStateHandle.get<String>("memoId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow<EditMemoUiState>(EditMemoUiState.Loading)
    val uiState: StateFlow<EditMemoUiState> = _uiState.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    private val _existingImageUrl = MutableStateFlow<String?>(null)
    val existingImageUrl: StateFlow<String?> = _existingImageUrl.asStateFlow()

    private val _rating = MutableStateFlow(1f)
    val rating: StateFlow<Float> = _rating.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _locationName = MutableStateFlow("")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _isWishlist = MutableStateFlow(false)
    val isWishlist: StateFlow<Boolean> = _isWishlist.asStateFlow()

    private val _businessName = MutableStateFlow("")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _businessPhone = MutableStateFlow("")
    val businessPhone: StateFlow<String> = _businessPhone.asStateFlow()

    private val _businessAddress = MutableStateFlow("")
    val businessAddress: StateFlow<String> = _businessAddress.asStateFlow()

    private val _naverPlaceUrl = MutableStateFlow("")
    val naverPlaceUrl: StateFlow<String> = _naverPlaceUrl.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.dailymemo.domain.models.Place>>(emptyList())
    val searchResults: StateFlow<List<com.dailymemo.domain.models.Place>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _showSearchDialog = MutableStateFlow(false)
    val showSearchDialog: StateFlow<Boolean> = _showSearchDialog.asStateFlow()

    private val _categories = MutableStateFlow<List<com.dailymemo.domain.models.MemoCategory>>(emptyList())
    val categories: StateFlow<List<com.dailymemo.domain.models.MemoCategory>> = _categories.asStateFlow()

    private val _selectedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCategoryIds: StateFlow<Set<Int>> = _selectedCategoryIds.asStateFlow()

    private val _hasEditPermission = MutableStateFlow(false)
    val hasEditPermission: StateFlow<Boolean> = _hasEditPermission.asStateFlow()

    private var originalMemo: Memo? = null

    init {
        loadMemo()
        loadCategories()
    }

    private fun loadMemo() {
        viewModelScope.launch {
            _uiState.value = EditMemoUiState.Loading

            getMemoUseCase(memoId).fold(
                onSuccess = { memo ->
                    originalMemo = memo
                    // Populate fields with existing memo data
                    _title.value = memo.title
                    _content.value = memo.content
                    _existingImageUrl.value = memo.imageUrl
                    _rating.value = memo.rating
                    _isPinned.value = memo.isPinned
                    _locationName.value = memo.locationName ?: ""
                    _isWishlist.value = memo.isWishlist
                    _businessName.value = memo.businessName ?: ""
                    _businessPhone.value = memo.businessPhone ?: ""
                    _businessAddress.value = memo.businessAddress ?: ""
                    _naverPlaceUrl.value = memo.naverPlaceUrl ?: ""
                    _selectedCategoryIds.value = memo.categories.map { it.id }.toSet()

                    if (memo.latitude != null && memo.longitude != null) {
                        _currentLocation.value = Location(memo.latitude, memo.longitude)
                    }

                    // Check edit permission
                    checkEditPermission()
                },
                onFailure = { error ->
                    _uiState.value = EditMemoUiState.Error(error.message ?: "메모를 불러올 수 없습니다")
                }
            )
        }
    }

    private fun checkEditPermission() {
        viewModelScope.launch {
            val roomId = authLocalDataSource.getCurrentRoomId()
            val currentUserId = authLocalDataSource.getUserId()?.toLongOrNull()

            if (roomId == null || currentUserId == null) {
                _hasEditPermission.value = false
                _uiState.value = EditMemoUiState.Error("권한 정보를 확인할 수 없습니다")
                return@launch
            }

            getRoomMembersUseCase(roomId.toLong()).fold(
                onSuccess = { members ->
                    val currentMember = members.firstOrNull { it.userId == currentUserId }
                    val canEdit = currentMember?.permission in listOf(
                        RoomPermission.OWNER,
                        RoomPermission.READ_WRITE
                    )

                    _hasEditPermission.value = canEdit
                    _uiState.value = if (canEdit) {
                        EditMemoUiState.Editing
                    } else {
                        EditMemoUiState.Error("편집 권한이 없습니다. 방장 또는 편집 권한이 있는 사용자만 메모를 수정할 수 있습니다.")
                    }
                },
                onFailure = {
                    _hasEditPermission.value = false
                    _uiState.value = EditMemoUiState.Error("권한 확인에 실패했습니다")
                }
            )
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().fold(
                onSuccess = { categories ->
                    _categories.value = categories
                },
                onFailure = { error ->
                    android.util.Log.e("EditMemoVM", "Failed to load categories: ${error.message}")
                }
            )
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
    }

    fun onImageSelected(uri: Uri?) {
        _imageUri.value = uri
    }

    fun onRatingChange(newRating: Float) {
        _rating.value = newRating
    }

    fun togglePin() {
        _isPinned.value = !_isPinned.value
    }

    fun onLocationNameChange(newName: String) {
        _locationName.value = newName
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

    fun onNaverPlaceUrlChange(newUrl: String) {
        _naverPlaceUrl.value = newUrl
    }

    fun onCategorySelectionChange(newSelection: Set<Int>) {
        _selectedCategoryIds.value = newSelection
    }

    fun setPlaceLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = Location(latitude, longitude)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun searchPlaces() {
        viewModelScope.launch {
            _isSearching.value = true
            searchPlacesUseCase(_searchQuery.value).fold(
                onSuccess = { places ->
                    _searchResults.value = places
                    _isSearching.value = false
                },
                onFailure = {
                    _searchResults.value = emptyList()
                    _isSearching.value = false
                }
            )
        }
    }

    fun showSearchDialog() {
        _showSearchDialog.value = true
    }

    fun hideSearchDialog() {
        _showSearchDialog.value = false
    }

    fun updateMemo() {
        if (!_hasEditPermission.value) {
            _uiState.value = EditMemoUiState.Error("편집 권한이 없습니다")
            return
        }

        viewModelScope.launch {
            _uiState.value = EditMemoUiState.Saving

            // Handle image upload if new image is selected
            val imageUrl = if (_imageUri.value != null) {
                // Upload new image and get URL
                memoRepository.uploadImage(_imageUri.value!!).fold(
                    onSuccess = { url -> url },
                    onFailure = { error ->
                        _uiState.value = EditMemoUiState.Error(error.message ?: "이미지 업로드에 실패했습니다")
                        return@launch
                    }
                )
            } else {
                // Use existing image URL if no new image selected
                _existingImageUrl.value
            }

            val location = _currentLocation.value
            updateMemoUseCase(
                id = memoId,
                title = _title.value,
                content = _content.value,
                imageUrl = imageUrl,
                imageUrls = if (imageUrl != null) listOf(imageUrl) else emptyList(),
                rating = _rating.value,
                isPinned = _isPinned.value,
                latitude = location?.latitude,
                longitude = location?.longitude,
                locationName = _locationName.value.ifEmpty { null },
                isWishlist = _isWishlist.value,
                businessName = _businessName.value.ifEmpty { null },
                businessPhone = _businessPhone.value.ifEmpty { null },
                businessAddress = _businessAddress.value.ifEmpty { null },
                categoryIds = _selectedCategoryIds.value.toList()
            ).fold(
                onSuccess = {
                    _uiState.value = EditMemoUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = EditMemoUiState.Error(error.message ?: "메모 수정에 실패했습니다")
                }
            )
        }
    }

    fun hasChanges(): Boolean {
        val memo = originalMemo ?: return false
        return _title.value != memo.title ||
                _content.value != memo.content ||
                _rating.value != memo.rating ||
                _isPinned.value != memo.isPinned ||
                _locationName.value != (memo.locationName ?: "") ||
                _isWishlist.value != memo.isWishlist ||
                _businessName.value != (memo.businessName ?: "") ||
                _businessPhone.value != (memo.businessPhone ?: "") ||
                _businessAddress.value != (memo.businessAddress ?: "") ||
                _naverPlaceUrl.value != (memo.naverPlaceUrl ?: "") ||
                _selectedCategoryIds.value != memo.categories.map { it.id }.toSet() ||
                _imageUri.value != null
    }
}

sealed class EditMemoUiState {
    object Loading : EditMemoUiState()
    object Editing : EditMemoUiState()
    object Saving : EditMemoUiState()
    object Success : EditMemoUiState()
    data class Error(val message: String) : EditMemoUiState()
}
