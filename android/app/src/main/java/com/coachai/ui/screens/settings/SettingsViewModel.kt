package com.coachai.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String = PreferencesManager.DEFAULT_URL,
    val username: String = PreferencesManager.DEFAULT_USERNAME,
    val password: String = PreferencesManager.DEFAULT_PASSWORD,
    val isSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(prefs.serverUrl, prefs.username, prefs.password) { url, user, pass ->
                SettingsUiState(url, user, pass)
            }.collect { _uiState.value = it }
        }
    }

    fun save(url: String, username: String, password: String) {
        viewModelScope.launch {
            prefs.saveSettings(url, username, password)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
