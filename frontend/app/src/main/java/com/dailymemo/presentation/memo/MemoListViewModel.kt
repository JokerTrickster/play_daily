package com.dailymemo.presentation.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.usecases.DeleteMemoUseCase
import com.dailymemo.domain.usecases.GetMemosUseCase
import com.dailymemo.utils.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoListViewModel @Inject constructor(
    private val getMemosUseCase: GetMemosUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val categoryRepository: com.dailymemo.domain.repositories.CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemoListUiState>(MemoListUiState.Loading)
    val uiState: StateFlow<MemoListUiState> = _uiState.asStateFlow()

    private val _currentTab = MutableStateFlow(MemoTab.VISITED)
    val currentTab: StateFlow<MemoTab> = _currentTab.asStateFlow()

    // 페이지네이션 상태
    private var currentPage = 1
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // 전체 메모 목록 (필터링 전)
    private val _allMemos = MutableStateFlow<List<Memo>>(emptyList())

    // 검색 및 필터 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<com.dailymemo.domain.models.PlaceCategory?>(null)
    val selectedCategory: StateFlow<com.dailymemo.domain.models.PlaceCategory?> = _selectedCategory.asStateFlow()

    private val _minRating = MutableStateFlow(0f)
    val minRating: StateFlow<Float> = _minRating.asStateFlow()

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    // NEW: Category filter state
    private val _categories = MutableStateFlow<List<com.dailymemo.domain.models.MemoCategory>>(emptyList())
    val categories: StateFlow<List<com.dailymemo.domain.models.MemoCategory>> = _categories.asStateFlow()

    private val _filterCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    val filterCategoryIds: StateFlow<Set<Int>> = _filterCategoryIds.asStateFlow()

    init {
        loadCategories()
        loadMemos()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().fold(
                onSuccess = { categories ->
                    _categories.value = categories
                },
                onFailure = { error ->
                    android.util.Log.e("MemoListViewModel", "Failed to load categories: ${error.message}")
                }
            )
        }
    }

    fun switchTab(tab: MemoTab) {
        _currentTab.value = tab
        loadMemos()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun onCategoryFilterChange(category: com.dailymemo.domain.models.PlaceCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
        applyFilters()
    }

    fun onRatingFilterChange(rating: Float) {
        _minRating.value = rating
        applyFilters()
    }

    fun toggleFilters() {
        _showFilters.value = !_showFilters.value
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _minRating.value = 0f
        _filterCategoryIds.value = emptySet()
        loadMemos() // Reload without category filter
    }

    fun applyCategoryFilter(categoryIds: Set<Int>) {
        _filterCategoryIds.value = categoryIds
        // Use client-side filtering to avoid full reload
        applyFilters()
    }

    fun clearCategoryFilter() {
        _filterCategoryIds.value = emptySet()
        // Use client-side filtering to avoid full reload
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = _allMemos.value.filter { memo ->
            // 검색어 필터
            val matchesSearch = if (_searchQuery.value.isBlank()) {
                true
            } else {
                memo.title.contains(_searchQuery.value, ignoreCase = true) ||
                memo.content.contains(_searchQuery.value, ignoreCase = true) ||
                memo.locationName?.contains(_searchQuery.value, ignoreCase = true) == true
            }

            // 카테고리 필터 (deprecated PlaceCategory)
            val matchesCategory = _selectedCategory.value?.let { selectedCat ->
                memo.category == selectedCat
            } ?: true

            // 새로운 카테고리 필터 (MemoCategory IDs)
            val matchesCategoryIds = if (_filterCategoryIds.value.isNotEmpty()) {
                memo.categories.any { it.id in _filterCategoryIds.value }
            } else {
                true
            }

            // 평점 필터
            val matchesRating = memo.rating >= _minRating.value

            matchesSearch && matchesCategory && matchesCategoryIds && matchesRating
        }

        _uiState.value = MemoListUiState.Success(filtered)
    }

    fun loadMemos() {
        viewModelScope.launch {
            // Only show loading if we don't have data yet (prevents flickering on ON_RESUME)
            if (_allMemos.value.isEmpty()) {
                _uiState.value = MemoListUiState.Loading
            }
            currentPage = 1
            _hasMore.value = true

            val isWishlist = (_currentTab.value == MemoTab.WISHLIST)
            // Don't send categoryIds to server - we'll filter client-side for better UX
            getMemosUseCase(isWishlist = isWishlist, categoryIds = null, page = currentPage, limit = 10).fold(
                onSuccess = { result ->
                    _allMemos.value = result.memos
                    _hasMore.value = result.hasMore
                    applyFilters()
                },
                onFailure = { error ->
                    _uiState.value = MemoListUiState.Error(ErrorHandler.Memo.loadError(error))
                }
            )
        }
    }

    fun loadMoreMemos() {
        if (!_hasMore.value || _isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++

            val isWishlist = (_currentTab.value == MemoTab.WISHLIST)
            // Don't send categoryIds to server - we'll filter client-side for better UX
            getMemosUseCase(isWishlist = isWishlist, categoryIds = null, page = currentPage, limit = 10).fold(
                onSuccess = { result ->
                    _allMemos.value = _allMemos.value + result.memos
                    _hasMore.value = result.hasMore
                    applyFilters()
                    _isLoadingMore.value = false
                },
                onFailure = { error ->
                    currentPage-- // Rollback page on error
                    _isLoadingMore.value = false
                }
            )
        }
    }

    fun deleteMemo(id: Long) {
        viewModelScope.launch {
            deleteMemoUseCase(id).fold(
                onSuccess = {
                    loadMemos() // Reload after delete
                },
                onFailure = { error ->
                    _uiState.value = MemoListUiState.Error(ErrorHandler.Memo.deleteError(error))
                }
            )
        }
    }
}

sealed class MemoListUiState {
    object Loading : MemoListUiState()
    data class Success(val memos: List<Memo>) : MemoListUiState()
    data class Error(val message: String) : MemoListUiState()
}
