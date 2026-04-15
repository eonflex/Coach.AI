package com.coachai.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.data.model.DailySummaryResponse
import com.coachai.data.model.WorkoutLogResponse
import com.coachai.data.repository.FoodRepository
import com.coachai.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dailySummary: DailySummaryResponse? = null,
    val todayWorkouts: List<WorkoutLogResponse> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val summary = runCatching { foodRepository.getDailySummary() }.getOrNull()
                // Fetch today's workouts — backend returns paged; grab first page
                val workouts = runCatching {
                    workoutRepository.getWorkoutLogs(page = 1).items
                }.getOrDefault(emptyList())
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    dailySummary = summary,
                    todayWorkouts = workouts
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }
}
