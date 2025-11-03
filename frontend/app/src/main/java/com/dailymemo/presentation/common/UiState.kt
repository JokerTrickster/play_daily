package com.dailymemo.presentation.common

/**
 * Generic UI state wrapper for async operations
 * Provides consistent loading, success, and error states across ViewModels
 */
sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val error: Throwable) : UiState<Nothing>()

    val isLoading: Boolean
        get() = this is Loading

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    val isIdle: Boolean
        get() = this is Idle
}

/**
 * Extension to get data or null
 */
fun <T> UiState<T>.dataOrNull(): T? {
    return when (this) {
        is UiState.Success -> data
        else -> null
    }
}

/**
 * Extension to get error or null
 */
fun <T> UiState<T>.errorOrNull(): Throwable? {
    return when (this) {
        is UiState.Error -> error
        else -> null
    }
}
