package com.coachai.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.data.api.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel that owns a [UiState] and provides helpers for executing
 * network operations safely.
 *
 * Feature ViewModels extend this class:
 *
 *   @HiltViewModel
 *   class MyViewModel @Inject constructor(...) : BaseViewModel<MyData>() {
 *       fun load() = launchWithState { safeApiCall { repository.getData() } }
 *   }
 */
abstract class BaseViewModel<T> : ViewModel() {

    private val _state = MutableStateFlow<UiState<T>>(UiState.Idle)
    val state: StateFlow<UiState<T>> = _state

    protected fun setState(value: UiState<T>) {
        _state.value = value
    }

    /**
     * Launches a coroutine that sets Loading, then maps a [NetworkResult] to the
     * appropriate [UiState] and updates [state].
     */
    protected fun launchWithState(block: suspend () -> NetworkResult<T>) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = when (val result = block()) {
                is NetworkResult.Success -> UiState.Success(result.data)
                is NetworkResult.Error   -> UiState.Error(result.message)
                is NetworkResult.Loading -> UiState.Loading
            }
        }
    }
}
