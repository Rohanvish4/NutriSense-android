package com.nutrisense.ai.data.remote.dto

import com.nutrisense.ai.domain.model.FoodItem
import com.nutrisense.ai.domain.model.MealAnalysis
import kotlinx.serialization.Serializable

/**
 * Remote Data Transfer Object representing aggregate macronutrient distribution.
 */
@Serializable
data class MacronutrientsDto(
    val proteinGrams: Double = 0.0,
    val carbGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val fiberGrams: Double = 0.0
)

/**
 * Remote Data Transfer Object representing an individual ingredient or food item detected by Gemini.
 */
@Serializable
data class FoodItemDto(
    val name: String,
    val estimatedWeightGrams: Double = 0.0,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val confidence: Float = 1.0f,
    val potentialAllergens: List<String> = emptyList()
)

/**
 * Remote Data Transfer Object representing the complete JSON payload returned by Gemini 1.5 Flash.
 */
@Serializable
data class MealAnalysisDto(
    val mealName: String,
    val totalCalories: Double = 0.0,
    val macronutrients: MacronutrientsDto = MacronutrientsDto(),
    val items: List<FoodItemDto> = emptyList(),
    val dietaryFlags: List<String> = emptyList(),
    val confidenceScore: Float = 1.0f
)

/**
 * Extension mapper function converting [FoodItemDto] to pure domain model [FoodItem].
 */
fun FoodItemDto.toDomain(): FoodItem {
    return FoodItem(
        name = name,
        portionGrams = estimatedWeightGrams,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        confidence = confidence,
        allergens = potentialAllergens
    )
}

/**
 * Extension mapper function converting [MealAnalysisDto] to pure domain model [MealAnalysis].
 */
fun MealAnalysisDto.toDomain(): MealAnalysis {
    return MealAnalysis(
        mealName = mealName,
        totalCalories = totalCalories,
        totalProtein = macronutrients.proteinGrams,
        totalCarbs = macronutrients.carbGrams,
        totalFats = macronutrients.fatGrams,
        totalFiber = macronutrients.fiberGrams,
        items = items.map { it.toDomain() },
        dietaryFlags = dietaryFlags
    )
}
