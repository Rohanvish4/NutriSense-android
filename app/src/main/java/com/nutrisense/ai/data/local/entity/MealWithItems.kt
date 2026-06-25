package com.nutrisense.ai.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.nutrisense.ai.domain.model.MealAnalysis

/**
 * Room Relation data class pairing a parent [MealEntity] with its child [FoodItemEntity] list.
 */
data class MealWithItems(
    @Embedded val meal: MealEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val items: List<FoodItemEntity>
) {
    /**
     * Maps the Room relational entity to pure immutable domain model [MealAnalysis].
     */
    fun toDomain(): MealAnalysis {
        return MealAnalysis(
            id = meal.id,
            mealName = meal.mealName,
            totalCalories = meal.totalCalories,
            totalProtein = meal.totalProtein,
            totalCarbs = meal.totalCarbs,
            totalFats = meal.totalFats,
            totalFiber = meal.totalFiber,
            items = items.map { it.toDomain() },
            dietaryFlags = meal.dietaryFlags,
            timestamp = meal.timestamp,
            imagePath = meal.imagePath
        )
    }
}
