package com.nutrisense.ai.domain.model

import kotlinx.serialization.Serializable

/**
 * Pure immutable domain model representing an individual food item/ingredient inside a meal.
 *
 * @property name Clean display name of the detected food item (e.g., "Grilled Chicken Breast").
 * @property portionGrams Estimated weight in grams.
 * @property calories Estimated total kilocalories for this portion.
 * @property protein Protein content in grams.
 * @property carbs Carbohydrate content in grams.
 * @property fat Fat content in grams.
 * @property fiber Dietary fiber content in grams.
 * @property confidence Machine learning detection confidence score (0.0 to 1.0).
 * @property allergens List of potential allergen warnings associated with this food item.
 */
@Serializable
data class FoodItem(
    val name: String,
    val portionGrams: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val confidence: Float = 1.0f,
    val allergens: List<String> = emptyList()
)
