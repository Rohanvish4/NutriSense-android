package com.nutrisense.ai.domain.model

import java.util.UUID

/**
 * Pure immutable domain model representing the complete nutritional analysis of a scanned meal.
 *
 * @property id Unique identifier for the meal analysis record.
 * @property mealName Descriptive title summarizing the entire meal (e.g., "Salmon Salad Bowl").
 * @property totalCalories Aggregate total calories across all detected items.
 * @property totalProtein Aggregate total protein in grams.
 * @property totalCarbs Aggregate total carbohydrates in grams.
 * @property totalFats Aggregate total fats in grams.
 * @property totalFiber Aggregate total dietary fiber in grams.
 * @property items Breakdown of individual food items detected in the meal.
 * @property dietaryFlags Key dietary indicators (e.g., "Keto", "High-Protein", "Gluten-Free").
 * @property timestamp Unix epoch timestamp in milliseconds when the meal was analyzed.
 * @property imagePath Optional local URI/path to the captured cropped/full food image.
 */
data class MealAnalysis(
    val id: String = UUID.randomUUID().toString(),
    val mealName: String,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFats: Double,
    val totalFiber: Double = 0.0,
    val items: List<FoodItem> = emptyList(),
    val dietaryFlags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null
)
