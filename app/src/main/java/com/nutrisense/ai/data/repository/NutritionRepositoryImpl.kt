package com.nutrisense.ai.data.repository

import android.graphics.Bitmap
import com.nutrisense.ai.data.local.ImageHashUtil
import com.nutrisense.ai.data.local.dao.MealDao
import com.nutrisense.ai.data.local.entity.FoodItemEntity
import com.nutrisense.ai.data.local.entity.MealEntity
import com.nutrisense.ai.data.remote.GeminiRemoteDataSource
import com.nutrisense.ai.data.remote.dto.toDomain
import com.nutrisense.ai.domain.model.MealAnalysis
import com.nutrisense.ai.domain.repository.NutritionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [NutritionRepository] delivering offline-first nutrition intelligence.
 *
 * Employs difference hashing (dHash) for sub-millisecond local visual cache matching, automatically
 * falling back to Gemini 1.5 Flash multimodal prediction on cache misses.
 */
@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val remoteDataSource: GeminiRemoteDataSource,
    private val mealDao: MealDao
) : NutritionRepository {

    override suspend fun analyzeFood(bitmap: Bitmap, forceCloud: Boolean): Result<MealAnalysis> = withContext(Dispatchers.IO) {
        runCatching {
            val currentHash = ImageHashUtil.computeDHash(bitmap)

            if (!forceCloud) {
                val cachedMeals = mealDao.getAllMealsWithHash()
                val matchingMeal = cachedMeals.find { mealWithItems ->
                    mealWithItems.meal.imageHash?.let { cachedHash ->
                        ImageHashUtil.isVisuallySimilar(currentHash, cachedHash, maxThreshold = 8)
                    } ?: false
                }
                if (matchingMeal != null) {
                    return@runCatching matchingMeal.toDomain()
                }
            }

            // Cache miss or forceCloud -> execute Gemini AI prediction
            val remoteDto = remoteDataSource.analyzeFoodImage(bitmap).getOrThrow()
            remoteDto.toDomain()
        }
    }

    override suspend fun saveMeal(meal: MealAnalysis, imageHash: Long?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            saveMealInternal(meal, imageHash)
        }
    }

    override suspend fun deleteMeal(mealId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            mealDao.deleteMeal(mealId)
        }
    }

    override fun getMealById(mealId: String): Flow<MealAnalysis?> {
        return mealDao.getMealByIdFlow(mealId).map { it?.toDomain() }
    }

    override fun getAllMeals(): Flow<List<MealAnalysis>> {
        return mealDao.getAllMealsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getMealsForDay(startOfDayEpochMs: Long, endOfDayEpochMs: Long): Flow<List<MealAnalysis>> {
        return mealDao.getMealsForDayFlow(startOfDayEpochMs, endOfDayEpochMs).map { list ->
            list.map { it.toDomain() }
        }
    }

    private suspend fun saveMealInternal(meal: MealAnalysis, imageHash: Long?) {
        val mealEntity = MealEntity(
            id = meal.id,
            mealName = meal.mealName,
            totalCalories = meal.totalCalories,
            totalProtein = meal.totalProtein,
            totalCarbs = meal.totalCarbs,
            totalFats = meal.totalFats,
            totalFiber = meal.totalFiber,
            dietaryFlags = meal.dietaryFlags,
            timestamp = meal.timestamp,
            imagePath = meal.imagePath,
            imageHash = imageHash
        )

        val foodItemEntities = meal.items.map { item ->
            FoodItemEntity(
                mealId = meal.id,
                name = item.name,
                portionGrams = item.portionGrams,
                calories = item.calories,
                protein = item.protein,
                carbs = item.carbs,
                fat = item.fat,
                fiber = item.fiber,
                confidence = item.confidence,
                allergens = item.allergens
            )
        }

        mealDao.insertMealWithItems(mealEntity, foodItemEntities)
    }
}
