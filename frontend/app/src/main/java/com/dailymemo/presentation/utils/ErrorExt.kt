package com.dailymemo.presentation.utils

import android.content.Context
import com.dailymemo.R
import com.dailymemo.domain.error.DomainError

/**
 * Extension function to convert DomainError to user-friendly localized message
 */
fun DomainError.toUserMessage(context: Context): String {
    return context.getString(
        when (this) {
            is DomainError.NetworkError -> R.string.error_network
            is DomainError.NoConnection -> R.string.error_no_connection
            is DomainError.Timeout -> R.string.error_timeout
            is DomainError.Unauthorized -> R.string.error_unauthorized
            is DomainError.InvalidCredentials -> R.string.error_invalid_credentials
            is DomainError.SessionExpired -> R.string.error_session_expired
            is DomainError.RoomNotFound -> R.string.error_room_not_found
            is DomainError.InvalidRoomPassword -> R.string.error_invalid_room_password
            is DomainError.RoomJoinFailed -> R.string.error_room_join_failed
            is DomainError.Forbidden -> R.string.error_forbidden
            is DomainError.BadRequest -> R.string.error_bad_request
            is DomainError.RateLimitExceeded -> R.string.error_rate_limit_exceeded
            is DomainError.MemoNotFound -> R.string.error_memo_not_found
            is DomainError.MemoLoadFailed -> R.string.error_memo_load_failed
            is DomainError.MemoCreateFailed -> R.string.error_memo_create_failed
            is DomainError.MemoUpdateFailed -> R.string.error_memo_update_failed
            is DomainError.MemoDeleteFailed -> R.string.error_memo_delete_failed
            is DomainError.CommentCreateFailed -> R.string.error_comment_create_failed
            is DomainError.CommentDeleteFailed -> R.string.error_comment_delete_failed
            is DomainError.LikeToggleFailed -> R.string.error_like_toggle_failed
            is DomainError.LocationPermissionDenied -> R.string.error_location_permission_denied
            is DomainError.LocationUnavailable -> R.string.error_location_unavailable
            is DomainError.SearchFailed -> R.string.error_search_failed
            is DomainError.ProfileLoadFailed -> R.string.error_profile_load_failed
            is DomainError.ProfileUpdateFailed -> R.string.error_profile_update_failed
            is DomainError.ImageUploadFailed -> R.string.error_image_upload_failed
            is DomainError.ImageReadFailed -> R.string.error_image_read_failed
            is DomainError.ServerError -> R.string.error_server
            is DomainError.UnknownError -> R.string.error_unknown
        }
    )
}

/**
 * Extension function to convert any Throwable to user-friendly message
 * Falls back to DomainError.UnknownError if not a DomainError
 */
fun Throwable.toUserMessage(context: Context): String {
    return if (this is DomainError) {
        this.toUserMessage(context)
    } else {
        context.getString(R.string.error_unknown)
    }
}
