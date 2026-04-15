package com.coachai.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.data.model.*
import com.coachai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val history: List<ChatHistoryItem> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true))
    val uiState: StateFlow<ChatUiState> = _uiState

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val history = repository.getHistory()
                _uiState.value = _uiState.value.copy(isLoading = false, history = history)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            try {
                val userMsg = ChatHistoryItem(0, "user", message, "")
                val updatedHistory = _uiState.value.history + userMsg
                _uiState.value = _uiState.value.copy(history = updatedHistory)

                val response = repository.sendMessage(ChatRequest(message))
                val assistantMsg = ChatHistoryItem(response.id, "assistant", response.reply, response.createdAt)
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    history = updatedHistory + assistantMsg
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false, error = e.message)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                repository.clearHistory()
                _uiState.value = _uiState.value.copy(history = emptyList())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
