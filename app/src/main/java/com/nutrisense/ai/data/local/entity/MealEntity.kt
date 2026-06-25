package com.nutrisense.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Database Entity representing a stored meal analysis record.
 *
 * @property id Unique string UUID primary key.
 * @property mealName Descriptive title of the meal.
 * @property totalCalories Aggregate total kilocalories.
 * @property totalProtein Aggregate total protein in grams.
 * @property totalCarbs Aggregate total carbohydrates in grams.
 * @property totalFats Aggregate total fats in grams.
 * @property totalFiber Aggregate total dietary fiber in grams.
 * @property dietaryFlags List of dietary tags stored via [com.nutrisense.ai.data.local.Converters].
 * @property timestamp Unix epoch timestamp in milliseconds when the meal was logged.
 * @property imagePath Local storage URI/filepath of the meal image.
 * @property imageHash 64-bit dHash perceptual image hash used for semantic cache lookup.
 */
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val mealName: String,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFats: Double,
    val totalFiber: Double = 0.0,
    val dietaryFlags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val imageHash: Long? = null
)
