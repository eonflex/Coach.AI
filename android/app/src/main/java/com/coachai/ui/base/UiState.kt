package com.coachai.ui.base

/**
 * App-wide UI state envelope.  Every screen ViewModel exposes StateFlow<UiState<T>>.
 *
 * Idle    – initial / reset state, nothing shown
 * Loading – in-flight request; show spinner
 * Success – data available; show content
 * Error   – terminal failure; show error + retry button
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

/** Convenience helpers */
val <T> UiState<T>.isLoading get() = this is UiState.Loading
val <T> UiState<T>.isSuccess get() = this is UiState.Success
val <T> UiState<T>.dataOrNull get() = (this as? UiState.Success)?.data
val <T> UiState<T>.errorOrNull get() = (this as? UiState.Error)?.message
