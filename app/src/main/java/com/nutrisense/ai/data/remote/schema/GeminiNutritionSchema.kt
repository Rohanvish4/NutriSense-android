package com.nutrisense.ai.data.remote.schema

import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.Schema

/**
 * Defines the rigid Gemini 1.5 Flash JSON output schema tree.
 * Guarantees zero conversational text and strict JSON parsing compatibility.
 */
object GeminiNutritionSchema {
    val schema: Schema<*> = Schema(
        name = "mealAnalysis",
        type = FunctionType.OBJECT,
        description = "Meal analysis JSON payload schema",
        properties = mapOf(
            "mealName" to Schema(
                name = "mealName",
                type = FunctionType.STRING,
                description = "Summary title of the meal"
            ),
            "totalCalories" to Schema(
                name = "totalCalories",
                type = FunctionType.NUMBER,
                description = "Total estimated calories in kilocalories"
            ),
            "macronutrients" to Schema(
                name = "macronutrients",
                type = FunctionType.OBJECT,
                description = "Aggregate macronutrients distribution",
                properties = mapOf(
                    "proteinGrams" to Schema(name = "proteinGrams", type = FunctionType.NUMBER, description = "Total protein in grams"),
                    "carbGrams" to Schema(name = "carbGrams", type = FunctionType.NUMBER, description = "Total carbohydrates in grams"),
                    "fatGrams" to Schema(name = "fatGrams", type = FunctionType.NUMBER, description = "Total fats in grams"),
                    "fiberGrams" to Schema(name = "fiberGrams", type = FunctionType.NUMBER, description = "Total dietary fiber in grams")
                )
            ),
            "items" to Schema(
                name = "items",
                type = FunctionType.ARRAY,
                description = "Array of food items contained in the meal",
                items = Schema(
                    name = "foodItem",
                    type = FunctionType.OBJECT,
                    description = "Single food item element",
                    properties = mapOf(
                        "name" to Schema(name = "name", type = FunctionType.STRING, description = "Name of individual food item or ingredient"),
                        "estimatedWeightGrams" to Schema(name = "estimatedWeightGrams", type = FunctionType.NUMBER, description = "Estimated portion weight in grams"),
                        "calories" to Schema(name = "calories", type = FunctionType.NUMBER, description = "Calories for this item"),
                        "protein" to Schema(name = "protein", type = FunctionType.NUMBER, description = "Protein in grams for this item"),
                        "carbs" to Schema(name = "carbs", type = FunctionType.NUMBER, description = "Carbohydrates in grams for this item"),
                        "fat" to Schema(name = "fat", type = FunctionType.NUMBER, description = "Fat in grams for this item"),
                        "fiber" to Schema(name = "fiber", type = FunctionType.NUMBER, description = "Dietary fiber in grams for this item"),
                        "confidence" to Schema(name = "confidence", type = FunctionType.NUMBER, description = "Detection confidence score between 0.0 and 1.0"),
                        "potentialAllergens" to Schema(
                            name = "potentialAllergens",
                            type = FunctionType.ARRAY,
                            description = "List of potential allergens",
                            items = Schema(name = "allergen", type = FunctionType.STRING, description = "Allergen warning tag")
                        )
                    )
                )
            ),
            "dietaryFlags" to Schema(
                name = "dietaryFlags",
                type = FunctionType.ARRAY,
                description = "Dietary indicator tags",
                items = Schema(name = "flag", type = FunctionType.STRING, description = "Dietary indicator tag like Keto, High-Protein, Vegan")
            ),
            "confidenceScore" to Schema(name = "confidenceScore", type = FunctionType.NUMBER, description = "Overall analysis confidence score between 0.0 and 1.0")
        )
    )
}
