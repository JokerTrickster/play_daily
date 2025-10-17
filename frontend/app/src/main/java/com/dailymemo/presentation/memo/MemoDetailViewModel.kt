package com.dailymemo.presentation.memo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.usecases.DeleteMemoUseCase
import com.dailymemo.domain.usecases.GetMemoByIdUseCase
import com.dailymemo.domain.usecases.UpdateMemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoDetailViewModel @Inject constructor(
    private val getMemoByIdUseCase: GetMemoByIdUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoId: Long = savedStateHandle.get<String>("memoId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow<MemoDetailUiState>(MemoDetailUiState.Loading)
    val uiState: StateFlow<MemoDetailUiState> = _uiState.asStateFlow()

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

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    init {
        loadMemo()
    }

    private fun loadMemo() {
        viewModelScope.launch {
            _uiState.value = MemoDetailUiState.Loading

            getMemoByIdUseCase(memoId).fold(
                onSuccess = { memo ->
                    _title.value = memo.title
                    _content.value = memo.content
                    _imageUrl.value = memo.imageUrl ?: ""
                    _rating.value = memo.rating
                    _isPinned.value = memo.isPinned
                    _uiState.value = MemoDetailUiState.Loaded
                },
                onFailure = { error ->
                    _uiState.value = MemoDetailUiState.Error(
                        error.message ?: "메모를 불러오는데 실패했습니다"
                    )
                }
            )
        }
    }

    fun toggleEditMode() {
        _isEditing.value = !_isEditing.value
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
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

    fun updateMemo() {
        if (_title.value.isBlank() || _content.value.isBlank()) {
            _uiState.value = MemoDetailUiState.Error("제목과 내용을 입력해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = MemoDetailUiState.Updating

            updateMemoUseCase(
                id = memoId,
                title = _title.value.trim(),
                content = _content.value.trim(),
                imageUrl = if (_imageUrl.value.isNotBlank()) _imageUrl.value.trim() else null,
                rating = _rating.value,
                isPinned = _isPinned.value
            ).fold(
                onSuccess = {
                    _isEditing.value = false
                    _uiState.value = MemoDetailUiState.Updated
                },
                onFailure = { error ->
                    _uiState.value = MemoDetailUiState.Error(
                        error.message ?: "메모 수정에 실패했습니다"
                    )
                }
            )
        }
    }

    fun deleteMemo() {
        viewModelScope.launch {
            _uiState.value = MemoDetailUiState.Deleting

            deleteMemoUseCase(memoId).fold(
                onSuccess = {
                    _uiState.value = MemoDetailUiState.Deleted
                },
                onFailure = { error ->
                    _uiState.value = MemoDetailUiState.Error(
                        error.message ?: "메모 삭제에 실패했습니다"
                    )
                }
            )
        }
    }
}

sealed class MemoDetailUiState {
    object Loading : MemoDetailUiState()
    object Loaded : MemoDetailUiState()
    object Updating : MemoDetailUiState()
    object Updated : MemoDetailUiState()
    object Deleting : MemoDetailUiState()
    object Deleted : MemoDetailUiState()
    data class Error(val message: String) : MemoDetailUiState()
}
