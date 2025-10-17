package com.dailymemo.presentation.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.usecases.CreateMemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMemoViewModel @Inject constructor(
    private val createMemoUseCase: CreateMemoUseCase
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
                isPinned = _isPinned.value
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
