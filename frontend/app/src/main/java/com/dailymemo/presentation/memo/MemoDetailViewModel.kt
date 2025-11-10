package com.dailymemo.presentation.memo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Comment
import com.dailymemo.domain.models.MemoCategory
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.usecases.CreateCommentUseCase
import com.dailymemo.domain.usecases.DeleteCommentUseCase
import com.dailymemo.domain.usecases.DeleteMemoUseCase
import com.dailymemo.domain.usecases.GetMemoByIdUseCase
import com.dailymemo.domain.usecases.ToggleMemoLikeUseCase
import com.dailymemo.domain.repositories.RoomRepository
import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.domain.models.RoomPermission
import com.dailymemo.utils.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoDetailViewModel @Inject constructor(
    private val getMemoByIdUseCase: GetMemoByIdUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val toggleMemoLikeUseCase: ToggleMemoLikeUseCase,
    private val roomRepository: RoomRepository,
    private val authLocalDataSource: AuthLocalDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoId: Long = savedStateHandle.get<String>("memoId")?.toLongOrNull() ?: 0L

    // UI State
    private val _uiState = MutableStateFlow<MemoDetailUiState>(MemoDetailUiState.Loading)
    val uiState: StateFlow<MemoDetailUiState> = _uiState.asStateFlow()

    // Memo Data - Read-only display state
    private val _memoData = MutableStateFlow<MemoDisplayData?>(null)
    val memoData: StateFlow<MemoDisplayData?> = _memoData.asStateFlow()

    // Comments
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _commentInput = MutableStateFlow("")
    val commentInput: StateFlow<String> = _commentInput.asStateFlow()

    private val _commentRating = MutableStateFlow(0)
    val commentRating: StateFlow<Int> = _commentRating.asStateFlow()

    // Like state
    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> = _likesCount.asStateFlow()

    // Permissions
    private val _canEditMemo = MutableStateFlow(true)
    val canEditMemo: StateFlow<Boolean> = _canEditMemo.asStateFlow()

    init {
        loadMemo()
    }

    fun refresh() {
        loadMemo()
    }

    private fun loadMemo() {
        viewModelScope.launch {
            _uiState.value = MemoDetailUiState.Loading

            getMemoByIdUseCase(memoId).fold(
                onSuccess = { memo ->
                    // Update memo display data
                    _memoData.value = MemoDisplayData(
                        memoId = memo.id,
                        title = memo.title,
                        content = memo.content,
                        imageUrls = listOfNotNull(memo.imageUrl).filter { it.isNotBlank() },
                        rating = memo.rating,
                        isPinned = memo.isPinned,
                        naverPlaceUrl = memo.naverPlaceUrl,
                        businessName = memo.businessName,
                        businessPhone = memo.businessPhone,
                        businessAddress = memo.businessAddress,
                        locationName = memo.locationName,
                        category = memo.category,
                        categories = memo.categories
                    )

                    // Update comments
                    _comments.value = memo.comments

                    // Update like state
                    _isLiked.value = memo.isLiked
                    _likesCount.value = memo.likesCount

                    // Check edit permissions
                    checkEditPermission(memo.userId)

                    _uiState.value = MemoDetailUiState.Loaded
                },
                onFailure = { error ->
                    _uiState.value = MemoDetailUiState.Error(ErrorHandler.Memo.loadError(error))
                }
            )
        }
    }

    // Comment operations
    fun onCommentInputChange(newInput: String) {
        _commentInput.value = newInput
    }

    fun onCommentRatingChange(newRating: Int) {
        _commentRating.value = newRating
    }

    fun postComment() {
        if (_commentInput.value.isBlank()) return

        viewModelScope.launch {
            createCommentUseCase(memoId, _commentInput.value.trim(), _commentRating.value).fold(
                onSuccess = { newComment ->
                    _comments.value = _comments.value + newComment
                    _commentInput.value = ""
                    _commentRating.value = 0
                },
                onFailure = { error ->
                    _uiState.value = MemoDetailUiState.Error("댓글 작성 실패: ${error.message}")
                }
            )
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            deleteCommentUseCase(commentId).fold(
                onSuccess = {
                    _comments.value = _comments.value.filter { it.id != commentId }
                },
                onFailure = { error ->
                    _uiState.value = MemoDetailUiState.Error("댓글 삭제 실패: ${error.message}")
                }
            )
        }
    }

    // Like operation
    fun toggleLike() {
        viewModelScope.launch {
            toggleMemoLikeUseCase(memoId)
                .onSuccess { isLiked ->
                    _isLiked.value = isLiked
                    _likesCount.value = if (isLiked) {
                        _likesCount.value + 1
                    } else {
                        (_likesCount.value - 1).coerceAtLeast(0)
                    }
                }
                .onFailure {
                    // Silent failure for like/unlike (network issues)
                }
        }
    }

    // Delete operation
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

    // Permission check
    private fun checkEditPermission(memoOwnerId: Long) {
        viewModelScope.launch {
            val currentUserId = authLocalDataSource.getUserId()?.toLongOrNull() ?: 1L

            // Owner can always edit
            if (currentUserId == memoOwnerId) {
                _canEditMemo.value = true
                return@launch
            }

            // Check room permissions for shared rooms
            roomRepository.getRoomMembers(memoOwnerId).fold(
                onSuccess = { members ->
                    val currentMember = members.find { it.userId == currentUserId }
                    _canEditMemo.value = when (currentMember?.permission) {
                        RoomPermission.OWNER,
                        RoomPermission.READ_WRITE -> true
                        else -> false
                    }
                },
                onFailure = {
                    _canEditMemo.value = false
                }
            )
        }
    }
}

// Simplified display data model
data class MemoDisplayData(
    val memoId: Long,
    val title: String,
    val content: String,
    val imageUrls: List<String>,
    val rating: Float,
    val isPinned: Boolean,
    val naverPlaceUrl: String?,
    val businessName: String?,
    val businessPhone: String?,
    val businessAddress: String?,
    val locationName: String?,
    val category: PlaceCategory?,
    val categories: List<MemoCategory>
)

sealed class MemoDetailUiState {
    object Loading : MemoDetailUiState()
    object Loaded : MemoDetailUiState()
    object Deleting : MemoDetailUiState()
    object Deleted : MemoDetailUiState()
    data class Error(val message: String) : MemoDetailUiState()
}
