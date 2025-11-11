package com.dailymemo.domain.error

/**
 * Sealed class hierarchy for domain-level errors
 * Provides structured error handling with specific error types
 */
sealed class DomainError(
    val messageKey: String,
    override val cause: Throwable? = null
) : Exception(messageKey, cause) {

    // Network Errors
    data class NetworkError(val exception: Throwable) : DomainError("error_network", exception)
    data object NoConnection : DomainError("error_no_connection")
    data object Timeout : DomainError("error_timeout")

    // Authentication Errors
    data object Unauthorized : DomainError("error_unauthorized")
    data object InvalidCredentials : DomainError("error_invalid_credentials")
    data object SessionExpired : DomainError("error_session_expired")

    // Room Errors
    data object RoomNotFound : DomainError("error_room_not_found")
    data object InvalidRoomPassword : DomainError("error_invalid_room_password")
    data object RoomJoinFailed : DomainError("error_room_join_failed")
    data object Forbidden : DomainError("error_forbidden")
    data object BadRequest : DomainError("error_bad_request")
    data object RateLimitExceeded : DomainError("error_rate_limit_exceeded")

    // Memo Errors
    data object MemoNotFound : DomainError("error_memo_not_found")
    data object MemoLoadFailed : DomainError("error_memo_load_failed")
    data object MemoCreateFailed : DomainError("error_memo_create_failed")
    data object MemoUpdateFailed : DomainError("error_memo_update_failed")
    data object MemoDeleteFailed : DomainError("error_memo_delete_failed")

    // Comment Errors
    data object CommentCreateFailed : DomainError("error_comment_create_failed")
    data object CommentDeleteFailed : DomainError("error_comment_delete_failed")

    // Like Errors
    data object LikeToggleFailed : DomainError("error_like_toggle_failed")

    // Location Errors
    data object LocationPermissionDenied : DomainError("error_location_permission_denied")
    data object LocationUnavailable : DomainError("error_location_unavailable")

    // Search Errors
    data object SearchFailed : DomainError("error_search_failed")

    // Profile Errors
    data object ProfileLoadFailed : DomainError("error_profile_load_failed")
    data object ProfileUpdateFailed : DomainError("error_profile_update_failed")

    // File/Image Errors
    data object ImageUploadFailed : DomainError("error_image_upload_failed")
    data object ImageReadFailed : DomainError("error_image_read_failed")

    // Server Errors
    data class ServerError(val code: Int) : DomainError("error_server")
    data object UnknownError : DomainError("error_unknown")

    companion object {
        fun fromException(exception: Throwable, httpCode: Int? = null): DomainError {
            return when {
                httpCode == 401 -> Unauthorized
                httpCode == 404 -> RoomNotFound
                httpCode == 500 -> ServerError(500)
                exception is java.net.UnknownHostException -> NoConnection
                exception is java.net.SocketTimeoutException -> Timeout
                exception is java.io.IOException -> NetworkError(exception)
                else -> UnknownError
            }
        }

        fun fromHttpCode(code: Int, context: ErrorContext = ErrorContext.GENERIC): DomainError {
            return when (code) {
                401 -> Unauthorized
                404 -> when (context) {
                    ErrorContext.ROOM -> RoomNotFound
                    ErrorContext.MEMO -> MemoNotFound
                    else -> UnknownError
                }
                in 500..599 -> ServerError(code)
                else -> UnknownError
            }
        }
    }

    enum class ErrorContext {
        GENERIC, ROOM, MEMO, COMMENT, PROFILE, LOCATION, SEARCH
    }
}
