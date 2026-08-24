package com.nutrisense.ai.presentation.dashboard

import com.nutrisense.ai.domain.model.MealAnalysis

/**
 * UI State data model for the Daily Nutrition Dashboard screen.
 */
data class DashboardUiState(
    val selectedDateEpochMs: Long = System.currentTimeMillis(),
    val dateTitle: String = "Today, Aug 22",
    val consumedCalories: Double = 0.0,
    val targetCalories: Double = 2400.0,
    val consumedProtein: Double = 0.0,
    val targetProtein: Double = 150.0,
    val consumedCarbs: Double = 0.0,
    val targetCarbs: Double = 300.0,
    val consumedFat: Double = 0.0,
    val targetFat: Double = 80.0,
    val meals: List<MealAnalysis> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val calorieProgress: Float
        get() = if (targetCalories > 0) (consumedCalories / targetCalories).toFloat().coerceIn(0f, 1f) else 0f

    val proteinProgress: Float
        get() = if (targetProtein > 0) (consumedProtein / targetProtein).toFloat().coerceIn(0f, 1f) else 0f

    val carbsProgress: Float
        get() = if (targetCarbs > 0) (consumedCarbs / targetCarbs).toFloat().coerceIn(0f, 1f) else 0f

    val fatProgress: Float
        get() = if (targetFat > 0) (consumedFat / targetFat).toFloat().coerceIn(0f, 1f) else 0f
}
