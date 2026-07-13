package com.nutrisense.ai.domain.repository

import android.graphics.Bitmap
import com.nutrisense.ai.domain.model.MealAnalysis
import kotlinx.coroutines.flow.Flow

/**
 * Domain Repository contract defining offline-first nutrition intelligence and meal tracking APIs.
 */
interface NutritionRepository {

    /**
     * Analyzes a food image bitmap. Performs an offline cache-first perceptual hash lookup
     * before falling back to Gemini 1.5 Flash multimodal cloud prediction.
     *
     * @param bitmap Input cropped or full image bitmap.
     * @param forceCloud If true, bypasses the perceptual hash cache and forces a cloud API call.
     * @return [Result] wrapping [MealAnalysis] on success or exception on failure.
     */
    suspend fun analyzeFood(bitmap: Bitmap, forceCloud: Boolean = false): Result<MealAnalysis>

    /**
     * Saves a meal analysis domain model into local Room persistence.
     *
     * @param meal The domain model to save.
     * @param imageHash Optional 64-bit dHash perceptual hash to store for future offline cache matches.
     */
    suspend fun saveMeal(meal: MealAnalysis, imageHash: Long? = null): Result<Unit>

    /**
     * Deletes a meal analysis record by ID.
     */
    suspend fun deleteMeal(mealId: String): Result<Unit>

    /**
     * Emits a reactive stream of a single meal analysis record by ID.
     */
    fun getMealById(mealId: String): Flow<MealAnalysis?>

    /**
     * Emits a reactive stream of all logged meals sorted chronologically.
     */
    fun getAllMeals(): Flow<List<MealAnalysis>>

    /**
     * Emits a reactive stream of meals logged for a specific day timestamp range.
     */
    fun getMealsForDay(startOfDayEpochMs: Long, endOfDayEpochMs: Long): Flow<List<MealAnalysis>>
}
