package com.nutrisense.ai.presentation.results

import com.nutrisense.ai.domain.model.MealAnalysis

/**
 * UI State for the Meal Analysis Details & Nutrition Breakdown screen.
 */
sealed interface ResultsUiState {
    data object Loading : ResultsUiState
    data class Success(
        val mealAnalysis: MealAnalysis,
        val isEditing: Boolean = false,
        val isSaving: Boolean = false
    ) : ResultsUiState
    data class Error(val message: String) : ResultsUiState
}
