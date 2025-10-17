package com.dailymemo.presentation.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.usecases.DeleteMemoUseCase
import com.dailymemo.domain.usecases.GetMemosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoListViewModel @Inject constructor(
    private val getMemosUseCase: GetMemosUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemoListUiState>(MemoListUiState.Loading)
    val uiState: StateFlow<MemoListUiState> = _uiState.asStateFlow()

    init {
        loadMemos()
    }

    fun loadMemos() {
        viewModelScope.launch {
            _uiState.value = MemoListUiState.Loading
            getMemosUseCase().fold(
                onSuccess = { memos ->
                    _uiState.value = MemoListUiState.Success(memos)
                },
                onFailure = { error ->
                    _uiState.value = MemoListUiState.Error(error.message ?: "메모 로드 실패")
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
                    _uiState.value = MemoListUiState.Error(error.message ?: "메모 삭제 실패")
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
