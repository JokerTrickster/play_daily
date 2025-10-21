package com.dailymemo.presentation.memo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Comment
import com.dailymemo.domain.usecases.DeleteMemoUseCase
import com.dailymemo.domain.usecases.GetMemoByIdUseCase
import com.dailymemo.domain.usecases.UpdateMemoUseCase
import com.dailymemo.utils.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
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

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _commentInput = MutableStateFlow("")
    val commentInput: StateFlow<String> = _commentInput.asStateFlow()

    private val _commentRating = MutableStateFlow(0)
    val commentRating: StateFlow<Int> = _commentRating.asStateFlow()

    private val _naverPlaceUrl = MutableStateFlow<String?>(null)
    val naverPlaceUrl: StateFlow<String?> = _naverPlaceUrl.asStateFlow()

    private val _businessName = MutableStateFlow<String?>(null)
    val businessName: StateFlow<String?> = _businessName.asStateFlow()

    private val _businessPhone = MutableStateFlow<String?>(null)
    val businessPhone: StateFlow<String?> = _businessPhone.asStateFlow()

    private val _businessAddress = MutableStateFlow<String?>(null)
    val businessAddress: StateFlow<String?> = _businessAddress.asStateFlow()

    init {
        loadMemo()
        loadComments()
    }

    private fun loadComments() {
        // TODO: 백엔드 연동 시 실제 댓글 로드
        _comments.value = listOf(
            Comment(
                id = 1,
                memoId = memoId,
                userId = 1,
                userName = "테스트 사용자",
                content = "좋은 장소네요!",
                createdAt = LocalDateTime.now().minusHours(2),
                updatedAt = LocalDateTime.now().minusHours(2)
            ),
            Comment(
                id = 2,
                memoId = memoId,
                userId = 2,
                userName = "김철수",
                content = "다음에 꼭 가봐야겠어요",
                createdAt = LocalDateTime.now().minusMinutes(30),
                updatedAt = LocalDateTime.now().minusMinutes(30)
            )
        )
    }

    fun onCommentInputChange(newInput: String) {
        _commentInput.value = newInput
    }

    fun onCommentRatingChange(newRating: Int) {
        _commentRating.value = newRating
    }

    fun postComment() {
        if (_commentInput.value.isBlank()) return

        // TODO: 백엔드 연동 시 실제 댓글 작성
        val newComment = Comment(
            id = System.currentTimeMillis(),
            memoId = memoId,
            userId = 1,
            userName = "현재 사용자",
            content = _commentInput.value.trim(),
            rating = _commentRating.value,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        _comments.value = _comments.value + newComment
        _commentInput.value = ""
        _commentRating.value = 0
    }

    fun deleteComment(commentId: Long) {
        // TODO: 백엔드 연동 시 실제 댓글 삭제
        _comments.value = _comments.value.filter { it.id != commentId }
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
                    _naverPlaceUrl.value = memo.naverPlaceUrl
                    _businessName.value = memo.businessName
                    _businessPhone.value = memo.businessPhone
                    _businessAddress.value = memo.businessAddress
                    _uiState.value = MemoDetailUiState.Loaded
                },
                onFailure = { error ->
                    _uiState.value = MemoDetailUiState.Error(ErrorHandler.Memo.loadError(error))
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
                    _uiState.value = MemoDetailUiState.Error(ErrorHandler.Memo.updateError(error))
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
                    _uiState.value = MemoDetailUiState.Error(ErrorHandler.Memo.deleteError(error))
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
