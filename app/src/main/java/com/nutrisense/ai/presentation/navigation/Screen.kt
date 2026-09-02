package com.nutrisense.ai.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for NutriSense AI app using Kotlinx Serialization.
 */
sealed interface Route {

    @Serializable
    data object Dashboard : Route

    @Serializable
    data object Camera : Route

    @Serializable
    data class Results(val mealId: String) : Route

    @Serializable
    data object LogMeal : Route

    @Serializable
    data object History : Route
}
