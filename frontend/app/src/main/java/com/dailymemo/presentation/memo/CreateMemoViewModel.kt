package com.dailymemo.presentation.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Location
import com.dailymemo.domain.usecases.CreateMemoUseCase
import com.dailymemo.domain.usecases.location.GetCurrentLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMemoViewModel @Inject constructor(
    private val createMemoUseCase: CreateMemoUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
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

    init {
        // Automatically get current location when creating memo
        getCurrentLocation()
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

    fun createMemo() {
        if (_title.value.isBlank() || _content.value.isBlank()) {
            _uiState.value = CreateMemoUiState.Error("제목과 내용을 입력해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateMemoUiState.Loading

            createMemoUseCase(
                title = _title.value.trim(),
                content = _content.value.trim(),
                imageUrl = if (_imageUrl.value.isNotBlank()) _imageUrl.value.trim() else null,
                rating = _rating.value,
                isPinned = _isPinned.value,
                latitude = _currentLocation.value?.latitude,
                longitude = _currentLocation.value?.longitude,
                locationName = if (_locationName.value.isNotBlank()) _locationName.value.trim() else null
            ).fold(
                onSuccess = {
                    _uiState.value = CreateMemoUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = CreateMemoUiState.Error(
                        error.message ?: "메모 생성에 실패했습니다"
                    )
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
