package com.coachai.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.data.model.*
import com.coachai.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val workouts: List<WorkoutLogResponse> = emptyList(),
    val showLogDialog: Boolean = false
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState(isLoading = true))
    val uiState: StateFlow<WorkoutUiState> = _uiState

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = repository.getWorkoutLogs()
                _uiState.value = _uiState.value.copy(isLoading = false, workouts = result.items)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logWorkout(workoutType: String, notes: String?, cardioMinutes: Int?, exercises: List<CreateExerciseRequest>) {
        viewModelScope.launch {
            try {
                repository.createWorkoutLog(CreateWorkoutLogRequest(workoutType, notes, cardioMinutes, null, exercises))
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteWorkoutLog(id)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setShowLogDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showLogDialog = show) }
}
