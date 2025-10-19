package com.dailymemo.utils

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {
    /**
     * Converts technical error messages into user-friendly Korean messages
     */
    fun getUserFriendlyMessage(error: Throwable?, defaultMessage: String = "오류가 발생했습니다"): String {
        return when (error) {
            is UnknownHostException -> "인터넷 연결을 확인해주세요"
            is SocketTimeoutException -> "서버 응답 시간이 초과되었습니다\n잠시 후 다시 시도해주세요"
            is IOException -> "네트워크 연결이 불안정합니다\n인터넷 연결을 확인해주세요"
            else -> {
                val message = error?.message ?: return defaultMessage
                when {
                    message.contains("404", ignoreCase = true) -> "요청하신 메모를 찾을 수 없습니다"
                    message.contains("401", ignoreCase = true) ||
                    message.contains("403", ignoreCase = true) -> "인증에 실패했습니다\n다시 로그인해주세요"
                    message.contains("500", ignoreCase = true) -> "서버에 일시적인 오류가 발생했습니다\n잠시 후 다시 시도해주세요"
                    message.contains("timeout", ignoreCase = true) -> "서버 응답 시간이 초과되었습니다\n잠시 후 다시 시도해주세요"
                    message.contains("connection", ignoreCase = true) -> "서버에 연결할 수 없습니다\n네트워크 연결을 확인해주세요"
                    message.isNotBlank() && !message.contains("http", ignoreCase = true) -> message
                    else -> defaultMessage
                }
            }
        }
    }

    /**
     * Specific error messages for memo operations
     */
    object Memo {
        fun loadError(error: Throwable?): String =
            getUserFriendlyMessage(error, "메모를 불러오는데 실패했습니다\n다시 시도해주세요")

        fun createError(error: Throwable?): String =
            getUserFriendlyMessage(error, "메모 작성에 실패했습니다\n다시 시도해주세요")

        fun updateError(error: Throwable?): String =
            getUserFriendlyMessage(error, "메모 수정에 실패했습니다\n다시 시도해주세요")

        fun deleteError(error: Throwable?): String =
            getUserFriendlyMessage(error, "메모 삭제에 실패했습니다\n다시 시도해주세요")
    }

    /**
     * Specific error messages for location operations
     */
    object Location {
        fun getCurrentLocationError(error: Throwable?): String =
            when (error) {
                is SecurityException -> "위치 권한이 필요합니다\n설정에서 권한을 허용해주세요"
                else -> getUserFriendlyMessage(error, "현재 위치를 가져올 수 없습니다")
            }
    }

    /**
     * Specific error messages for authentication
     */
    object Auth {
        fun signInError(error: Throwable?): String =
            getUserFriendlyMessage(error, "로그인에 실패했습니다\n아이디와 비밀번호를 확인해주세요")

        fun signUpError(error: Throwable?): String =
            getUserFriendlyMessage(error, "회원가입에 실패했습니다\n다시 시도해주세요")
    }
}
