package com.dailymemo.presentation.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.usecases.GetMemosUseCase
import com.dailymemo.utils.ErrorHandler
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

    private val _currentTab = MutableStateFlow(MemoTab.VISITED)
    val currentTab: StateFlow<MemoTab> = _currentTab.asStateFlow()

    private val _currentRoomId = MutableStateFlow<Long?>(null)
    val currentRoomId: StateFlow<Long?> = _currentRoomId.asStateFlow()

    private val _isOwnRoom = MutableStateFlow(true)
    val isOwnRoom: StateFlow<Boolean> = _isOwnRoom.asStateFlow()

    init {
        loadMemos()
    }

    fun loadMemos() {
        reloadCurrentRoomData()
    }

    fun switchTab(tab: MemoTab) {
        _currentTab.value = tab
        reloadCurrentRoomData()
    }

    fun switchRoom(roomId: Long?, isOwn: Boolean = false) {
        _currentRoomId.value = roomId
        _isOwnRoom.value = isOwn
        reloadCurrentRoomData()
    }

    private fun reloadCurrentRoomData() {
        viewModelScope.launch {
            _uiState.value = TimelineUiState.Loading

            val isWishlist = (_currentTab.value == MemoTab.WISHLIST)

            getMemosUseCase(isWishlist = isWishlist).fold(
                onSuccess = { memos ->
                    val groupedMemos = groupMemosByDate(memos)
                    _uiState.value = TimelineUiState.Success(groupedMemos)
                },
                onFailure = { error ->
                    _uiState.value = TimelineUiState.Error(ErrorHandler.Memo.loadError(error))
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
