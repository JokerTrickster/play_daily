package com.dailymemo.presentation.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.usecases.GetMemosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getMemosUseCase: GetMemosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        loadMemos()
    }

    fun loadMemos() {
        viewModelScope.launch {
            _uiState.value = TimelineUiState.Loading
            getMemosUseCase().fold(
                onSuccess = { memos ->
                    val groupedMemos = groupMemosByDate(memos)
                    _uiState.value = TimelineUiState.Success(groupedMemos)
                },
                onFailure = { error ->
                    _uiState.value = TimelineUiState.Error(error.message ?: "메모 로드 실패")
                }
            )
        }
    }

    private fun groupMemosByDate(memos: List<Memo>): List<TimelineGroup> {
        return memos
            .sortedByDescending { it.createdAt }
            .groupBy { it.createdAt.toLocalDate() }
            .map { (date, memos) ->
                TimelineGroup(date = date, memos = memos)
            }
    }
}

sealed class TimelineUiState {
    object Loading : TimelineUiState()
    data class Success(val groups: List<TimelineGroup>) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
}

data class TimelineGroup(
    val date: LocalDate,
    val memos: List<Memo>
)
