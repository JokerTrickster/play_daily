package com.dailymemo.presentation.profile

import com.dailymemo.domain.models.Profile

/**
 * 프로필 화면 UI 상태
 */
sealed class ProfileUiState {
    /**
     * 로딩 중
     */
    object Loading : ProfileUiState()

    /**
     * 프로필 로드 성공
     * @property profile 프로필 정보
     */
    data class Success(val profile: Profile) : ProfileUiState()

    /**
     * 에러 발생
     * @property message 에러 메시지
     */
    data class Error(val message: String) : ProfileUiState()
}
