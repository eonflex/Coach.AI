package com.coachai.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

/**
 * Handles login form submission.
 *
 * For v1, Basic Auth credentials are saved directly to PreferencesManager and
 * the app navigates to main. There is no token-based login endpoint yet.
 *
 * TODO: When backend adds POST /api/auth/login, replace saveSettings call with
 *       an API call and persist the returned bearer token instead.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Username and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val serverUrl = preferencesManager.serverUrl.first()
                preferencesManager.saveSettings(serverUrl, username, password)
                _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save credentials"
                )
            }
        }
    }

    /** Navigate past login using whatever credentials are already persisted. */
    fun skipLogin() {
        _uiState.value = _uiState.value.copy(loginSuccess = true)
    }
}
