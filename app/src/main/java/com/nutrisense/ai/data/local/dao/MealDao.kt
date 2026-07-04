package com.nutrisense.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nutrisense.ai.data.local.entity.FoodItemEntity
import com.nutrisense.ai.data.local.entity.MealEntity
import com.nutrisense.ai.data.local.entity.MealWithItems
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for executing Room database queries on meals and food items.
 */
@Dao
interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemEntity>)

    /**
     * Atomically inserts a meal entity along with its list of food item child entities inside a single transaction.
     */
    @Transaction
    suspend fun insertMealWithItems(meal: MealEntity, items: List<FoodItemEntity>) {
        insertMeal(meal)
        insertFoodItems(items)
    }

    /**
     * Emits a real-time reactive flow of all logged meals sorted chronologically by timestamp descending.
     */
    @Transaction
    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMealsFlow(): Flow<List<MealWithItems>>

    /**
     * Emits a real-time reactive flow of meals recorded within a specific timestamp range.
     */
    @Transaction
    @Query("SELECT * FROM meals WHERE timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp DESC")
    fun getMealsForDayFlow(startMs: Long, endMs: Long): Flow<List<MealWithItems>>

    /**
     * Fetches a single meal analysis record by unique ID.
     */
    @Transaction
    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getMealById(id: String): MealWithItems?

    /**
     * Emits a reactive flow of a single meal analysis record by unique ID.
     */
    @Transaction
    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    fun getMealByIdFlow(id: String): Flow<MealWithItems?>

    /**
     * Fetches all cached meals that have a valid perceptual image hash recorded.
     */
    @Transaction
    @Query("SELECT * FROM meals WHERE imageHash IS NOT NULL")
    suspend fun getAllMealsWithHash(): List<MealWithItems>

    /**
     * Deletes a meal by ID (cascade deletes child food items automatically).
     */
    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: String)
}
