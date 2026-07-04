package com.nutrisense.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nutrisense.ai.data.local.dao.MealDao
import com.nutrisense.ai.data.local.entity.FoodItemEntity
import com.nutrisense.ai.data.local.entity.MealEntity

/**
 * Room Database definition for NutriSense AI app.
 */
@Database(
    entities = [MealEntity::class, FoodItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
