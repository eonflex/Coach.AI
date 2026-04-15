package com.coachai.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coachai.data.model.*
import com.coachai.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val foodItems: List<FoodItemResponse> = emptyList(),
    val dailySummary: DailySummaryResponse? = null,
    val searchQuery: String = "",
    val showAddFoodDialog: Boolean = false,
    val showLogFoodDialog: Boolean = false,
    val selectedFoodItem: FoodItemResponse? = null
)

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState(isLoading = true))
    val uiState: StateFlow<FoodUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val items = repository.getFoodItems()
                val summary = runCatching { repository.getDailySummary() }.getOrNull()
                _uiState.value = _uiState.value.copy(isLoading = false, foodItems = items, dailySummary = summary)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    fun searchFoods(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            try {
                val items = repository.getFoodItems(query.takeIf { it.isNotBlank() })
                _uiState.value = _uiState.value.copy(foodItems = items)
            } catch (e: Exception) { /* ignore search errors */ }
        }
    }

    fun createFoodItem(name: String, brand: String?, servingGrams: Double, calories: Double,
                       protein: Double, carbs: Double, fat: Double, fiber: Double) {
        viewModelScope.launch {
            try {
                repository.createFoodItem(CreateFoodItemRequest(name, brand, servingGrams, calories, protein, carbs, fat, fiber))
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun logFood(foodItemId: Int, servings: Double, meal: String?) {
        viewModelScope.launch {
            try {
                repository.logFood(LogFoodRequest(foodItemId, servings, meal, null))
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteFoodLog(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteFoodLog(id)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setShowAddFoodDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddFoodDialog = show)
    }

    fun setShowLogFoodDialog(show: Boolean, item: FoodItemResponse? = null) {
        _uiState.value = _uiState.value.copy(showLogFoodDialog = show, selectedFoodItem = item)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
