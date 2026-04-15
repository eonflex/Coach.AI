package com.coachai.ui.screens.workout.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.data.model.WorkoutLogResponse
import com.coachai.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutHistoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val workouts: List<WorkoutLogResponse> = emptyList(),
    val currentPage: Int = 0,
    val hasMore: Boolean = true
)

/**
 * ViewModel for [WorkoutHistoryScreen].
 *
 * Fetches paginated workout logs from [WorkoutRepository].
 * Backend: GET /api/workouts?page=N  (default page size = 20)
 */
@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutHistoryUiState(isLoading = true))
    val uiState: StateFlow<WorkoutHistoryUiState> = _uiState

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        val current = _uiState.value
        if (current.isLoading || !current.hasMore) return

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, error = null)
            try {
                val nextPage = current.currentPage + 1
                val paged = repository.getWorkoutLogs(page = nextPage)
                val all = current.workouts + paged.items
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    workouts = all,
                    currentPage = nextPage,
                    hasMore = all.size < paged.total
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load workouts"
                )
            }
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteWorkoutLog(id)
                _uiState.value = _uiState.value.copy(
                    workouts = _uiState.value.workouts.filter { it.id != id }
                )
            } catch (_: Exception) { /* surface error via snackbar in a follow-up */ }
        }
    }
}
