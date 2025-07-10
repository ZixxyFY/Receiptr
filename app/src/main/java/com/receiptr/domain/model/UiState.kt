package com.receiptr.domain.model

/**
 * Represents the UI state for any screen content
 */
sealed class UiState<out T> {
    /**
     * Initial state when no data has been loaded yet
     */
    object Idle : UiState<Nothing>()
    
    /**
     * Loading state while data is being fetched
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Success state with loaded data
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with error message
     */
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
    
    /**
     * Empty state when no data is available
     */
    object Empty : UiState<Nothing>()
}

/**
 * Extension functions for easier state checking
 */
val <T> UiState<T>.isLoading: Boolean
    get() = this is UiState.Loading

val <T> UiState<T>.isSuccess: Boolean
    get() = this is UiState.Success

val <T> UiState<T>.isError: Boolean
    get() = this is UiState.Error

val <T> UiState<T>.isEmpty: Boolean
    get() = this is UiState.Empty

val <T> UiState<T>.isIdle: Boolean
    get() = this is UiState.Idle

/**
 * Get data from success state or null
 */
val <T> UiState<T>.data: T?
    get() = (this as? UiState.Success)?.data

/**
 * Get error message from error state or null
 */
val <T> UiState<T>.errorMessage: String?
    get() = (this as? UiState.Error)?.message
