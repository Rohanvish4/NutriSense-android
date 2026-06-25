package com.nutrisense.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nutrisense.ai.domain.model.FoodItem
import java.util.UUID

/**
 * Room Database Entity representing an individual ingredient item linked to a parent meal.
 */
@Entity(
    tableName = "food_items",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mealId"])]
)
data class FoodItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val mealId: String,
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

/**
 * Maps [FoodItemEntity] to pure immutable domain model [FoodItem].
 */
fun FoodItemEntity.toDomain(): FoodItem {
    return FoodItem(
        name = name,
        portionGrams = portionGrams,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        confidence = confidence,
        allergens = allergens
    )
}
