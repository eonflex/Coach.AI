package com.coachai.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.data.model.*
import com.coachai.data.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val weights: List<WeightLogResponse> = emptyList(),
    val latestWeight: WeightLogResponse? = null,
    val showLogDialog: Boolean = false
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val repository: WeightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState(isLoading = true))
    val uiState: StateFlow<WeightUiState> = _uiState

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = repository.getWeightLogs()
                val latest = repository.getLatestWeight()
                _uiState.value = _uiState.value.copy(isLoading = false, weights = result.items, latestWeight = latest)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logWeight(weightKg: Double, notes: String?) {
        viewModelScope.launch {
            try {
                repository.logWeight(LogWeightRequest(weightKg, notes))
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteWeightLog(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteWeightLog(id)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setShowLogDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showLogDialog = show) }
}
