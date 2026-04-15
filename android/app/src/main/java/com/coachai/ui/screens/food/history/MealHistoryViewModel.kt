package com.coachai.ui.screens.food.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.data.model.DailySummaryResponse
import com.coachai.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MealHistoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: String = LocalDate.now().toString(),
    val summary: DailySummaryResponse? = null
)

/**
 * ViewModel for [MealHistoryScreen].
 *
 * Fetches daily food summary for [selectedDate] via [FoodRepository.getDailySummary].
 * Backend endpoint: GET /api/food/summary?date=yyyy-MM-dd
 */
@HiltViewModel
class MealHistoryViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealHistoryUiState(isLoading = true))
    val uiState: StateFlow<MealHistoryUiState> = _uiState

    init {
        reload()
    }

    fun reload() {
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val summary = repository.getDailySummary(date)
                _uiState.value = _uiState.value.copy(isLoading = false, summary = summary)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load meal history"
                )
            }
        }
    }

    fun previousDay() {
        val prev = LocalDate.parse(_uiState.value.selectedDate).minusDays(1).toString()
        _uiState.value = _uiState.value.copy(selectedDate = prev, summary = null)
        reload()
    }

    fun nextDay() {
        val next = LocalDate.parse(_uiState.value.selectedDate).plusDays(1)
        if (next <= LocalDate.now()) {
            _uiState.value = _uiState.value.copy(selectedDate = next.toString(), summary = null)
            reload()
        }
    }
}
