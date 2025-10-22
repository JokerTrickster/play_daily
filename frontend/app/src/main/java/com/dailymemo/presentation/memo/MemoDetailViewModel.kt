package com.dailymemo.presentation.memo

import android.net.Uri
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
    private val createCommentUseCase: com.dailymemo.domain.usecases.CreateCommentUseCase,
    private val deleteCommentUseCase: com.dailymemo.domain.usecases.DeleteCommentUseCase,
    private val memoRepository: com.dailymemo.domain.repositories.MemoRepository,
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

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris.asStateFlow()

    private val _existingImageUrls = MutableStateFlow<List<String>>(emptyList())
    val existingImageUrls: StateFlow<List<String>> = _existingImageUrls.asStateFlow()

    private val _rating = MutableStateFlow(0f)
    val rating: StateFlow<Float> = _rating.asStateFlow()

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

    private val _locationName = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    private val _category = MutableStateFlow<com.dailymemo.domain.models.PlaceCategory?>(null)
    val category: StateFlow<com.dailymemo.domain.models.PlaceCategory?> = _category.asStateFlow()

    private val _isWishlist = MutableStateFlow(false)
    val isWishlist: StateFlow<Boolean> = _isWishlist.asStateFlow()

    private val _latitude = MutableStateFlow<Double?>(null)
    private val _longitude = MutableStateFlow<Double?>(null)

    init {
        loadMemo()
    }

    fun onCommentInputChange(newInput: String) {
        _commentInput.value = newInput
    }

    fun onCommentRatingChange(newRating: Int) {
        _commentRating.value = newRating
    }

    fun postComment() {
        if (_commentInput.value.isBlank()) return

        viewModelScope.launch {
            createCommentUseCase(memoId, _commentInput.value.trim()).fold(
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

    private fun loadMemo() {
        viewModelScope.launch {
            _uiState.value = MemoDetailUiState.Loading

            getMemoByIdUseCase(memoId).fold(
                onSuccess = { memo ->
                    _title.value = memo.title
                    _content.value = memo.content
                    _imageUrl.value = memo.imageUrl ?: ""
                    // Initialize existing images
                    _existingImageUrls.value = if (!memo.imageUrl.isNullOrBlank()) {
                        listOf(memo.imageUrl)
                    } else {
                        emptyList()
                    }
                    _rating.value = memo.rating
                    _isPinned.value = memo.isPinned
                    _naverPlaceUrl.value = memo.naverPlaceUrl
                    _businessName.value = memo.businessName
                    _businessPhone.value = memo.businessPhone
                    _businessAddress.value = memo.businessAddress
                    _locationName.value = memo.locationName
                    _category.value = memo.category
                    _isWishlist.value = memo.isWishlist
                    _latitude.value = memo.latitude
                    _longitude.value = memo.longitude

                    // 댓글 로드 (API에서 함께 반환됨)
                    _comments.value = memo.comments

                    // 새로 추가한 이미지 URIs 초기화
                    _imageUris.value = emptyList()

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

    fun addImageUri(uri: Uri?) {
        uri?.let { newUri ->
            val currentCount = _existingImageUrls.value.size + _imageUris.value.size
            if (currentCount < 2) {
                _imageUris.value = _imageUris.value + newUri
            }
        }
    }

    fun removeImageUri(uri: Uri) {
        _imageUris.value = _imageUris.value.filter { it != uri }
    }

    fun removeExistingImage(url: String) {
        _existingImageUrls.value = _existingImageUrls.value.filter { it != url }
    }

    fun canAddMoreImages(): Boolean {
        return (_existingImageUrls.value.size + _imageUris.value.size) < 2
    }

    fun onRatingChange(newRating: Float) {
        _rating.value = newRating.coerceIn(0f, 5f)
    }

    fun togglePin() {
        _isPinned.value = !_isPinned.value
    }

    fun onLocationNameChange(newLocationName: String?) {
        _locationName.value = newLocationName
    }

    fun onBusinessNameChange(newBusinessName: String?) {
        _businessName.value = newBusinessName
    }

    fun onBusinessPhoneChange(newBusinessPhone: String?) {
        _businessPhone.value = newBusinessPhone
    }

    fun onBusinessAddressChange(newBusinessAddress: String?) {
        _businessAddress.value = newBusinessAddress
    }

    fun onCategoryChange(newCategory: com.dailymemo.domain.models.PlaceCategory?) {
        _category.value = newCategory
    }

    fun updateMemo() {
        if (_title.value.isBlank() || _content.value.isBlank()) {
            _uiState.value = MemoDetailUiState.Error("제목과 내용을 입력해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = MemoDetailUiState.Updating

            // Upload new images and get URLs
            val newImageUrls = mutableListOf<String>()
            for (uri in _imageUris.value) {
                memoRepository.uploadImage(uri).fold(
                    onSuccess = { url -> newImageUrls.add(url) },
                    onFailure = { error ->
                        _uiState.value = MemoDetailUiState.Error("이미지 업로드 실패: ${error.message}")
                        return@launch
                    }
                )
            }

            // Combine existing and new image URLs
            val allImageUrls = _existingImageUrls.value + newImageUrls
            val firstImageUrl = allImageUrls.firstOrNull()

            updateMemoUseCase(
                id = memoId,
                title = _title.value.trim(),
                content = _content.value.trim(),
                imageUrl = firstImageUrl,
                imageUrls = allImageUrls,
                rating = _rating.value,
                isPinned = _isPinned.value,
                latitude = _latitude.value,
                longitude = _longitude.value,
                locationName = _locationName.value?.trim(),
                category = _category.value,
                isWishlist = _isWishlist.value,
                businessName = _businessName.value?.trim(),
                businessPhone = _businessPhone.value?.trim(),
                businessAddress = _businessAddress.value?.trim()
            ).fold(
                onSuccess = {
                    _isEditing.value = false
                    // 업데이트 후 메모를 다시 로드하여 최신 상태 반영
                    loadMemo()
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
