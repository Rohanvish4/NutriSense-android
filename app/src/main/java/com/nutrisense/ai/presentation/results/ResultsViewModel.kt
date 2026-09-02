package com.nutrisense.ai.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisense.ai.domain.model.MealAnalysis
import com.nutrisense.ai.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for displaying meal details and nutrition breakdown.
 */
@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    fun loadMeal(mealId: String) {
        _uiState.value = ResultsUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMealById(mealId).collect { meal ->
                if (meal != null) {
                    _uiState.value = ResultsUiState.Success(mealAnalysis = meal)
                } else {
                    _uiState.value = ResultsUiState.Error("Meal analysis not found.")
                }
            }
        }
    }

    fun toggleEditMode() {
        val state = _uiState.value as? ResultsUiState.Success ?: return
        _uiState.value = state.copy(isEditing = !state.isEditing)
    }

    fun updateMealName(newName: String) {
        val state = _uiState.value as? ResultsUiState.Success ?: return
        val updatedMeal = state.mealAnalysis.copy(mealName = newName)
        _uiState.value = state.copy(mealAnalysis = updatedMeal)
    }

    fun updateItemPortion(itemIndex: Int, newGrams: Double) {
        val state = _uiState.value as? ResultsUiState.Success ?: return
        val currentMeal = state.mealAnalysis
        val currentItems = currentMeal.items.toMutableList()

        if (itemIndex in currentItems.indices) {
            val item = currentItems[itemIndex]
            val origGrams = if (item.portionGrams > 0) item.portionGrams else 100.0
            val validNewGrams = newGrams.coerceAtLeast(0.0)
            val ratio = validNewGrams / origGrams

            val updatedItem = item.copy(
                portionGrams = validNewGrams,
                calories = item.calories * ratio,
                protein = item.protein * ratio,
                carbs = item.carbs * ratio,
                fat = item.fat * ratio
            )

            currentItems[itemIndex] = updatedItem

            val updatedMeal = currentMeal.copy(
                items = currentItems,
                totalCalories = currentItems.sumOf { it.calories },
                totalProtein = currentItems.sumOf { it.protein },
                totalCarbs = currentItems.sumOf { it.carbs },
                totalFats = currentItems.sumOf { it.fat }
            )

            _uiState.value = state.copy(mealAnalysis = updatedMeal)
        }
    }

    fun updateItemMacros(itemIndex: Int, protein: Double? = null, carbs: Double? = null, fat: Double? = null) {
        val state = _uiState.value as? ResultsUiState.Success ?: return
        val currentMeal = state.mealAnalysis
        val currentItems = currentMeal.items.toMutableList()

        if (itemIndex in currentItems.indices) {
            val item = currentItems[itemIndex]
            val newProtein = (protein ?: item.protein).coerceAtLeast(0.0)
            val newCarbs = (carbs ?: item.carbs).coerceAtLeast(0.0)
            val newFat = (fat ?: item.fat).coerceAtLeast(0.0)
            val newCalories = (newProtein * 4.0) + (newCarbs * 4.0) + (newFat * 9.0)

            val updatedItem = item.copy(
                protein = newProtein,
                carbs = newCarbs,
                fat = newFat,
                calories = newCalories
            )

            currentItems[itemIndex] = updatedItem

            val updatedMeal = currentMeal.copy(
                items = currentItems,
                totalCalories = currentItems.sumOf { it.calories },
                totalProtein = currentItems.sumOf { it.protein },
                totalCarbs = currentItems.sumOf { it.carbs },
                totalFats = currentItems.sumOf { it.fat }
            )

            _uiState.value = state.copy(mealAnalysis = updatedMeal)
        }
    }

    fun saveMealChanges(onSaved: () -> Unit) {
        val state = _uiState.value as? ResultsUiState.Success ?: return
        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMeal(state.mealAnalysis)
            withContext(Dispatchers.Main) {
                _uiState.value = state.copy(isSaving = false, isEditing = false)
                onSaved()
            }
        }
    }

    fun deleteMeal(mealId: String, onDeleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeal(mealId)
            withContext(Dispatchers.Main) {
                onDeleted()
            }
        }
    }
}
