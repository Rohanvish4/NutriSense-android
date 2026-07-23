package com.nutrisense.ai.presentation.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisense.ai.data.local.ImageHashUtil
import com.nutrisense.ai.domain.model.DetectionBox
import com.nutrisense.ai.domain.model.FoodItem
import com.nutrisense.ai.domain.model.MealAnalysis
import com.nutrisense.ai.domain.repository.NutritionRepository
import com.nutrisense.ai.presentation.camera.util.CropUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the camera scanning screen.
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Scanning())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /**
     * Called by CameraX frame analyzer whenever real-time ML Kit object detection results update.
     */
    fun onObjectsDetected(boxes: List<DetectionBox>) {
        val currentState = _uiState.value
        if (currentState is CameraUiState.Idle || currentState is CameraUiState.Scanning) {
            _uiState.value = CameraUiState.Scanning(detectedBoxes = boxes)
        }
    }

    /**
     * Captures a still camera frame, crops the primary detected food ROI, and dispatches to [NutritionRepository].
     */
    fun captureAndAnalyze(fullBitmap: Bitmap, focusedBox: DetectionBox? = null) {
        _uiState.value = CameraUiState.Analyzing("Analyzing meal...")

        viewModelScope.launch(Dispatchers.IO) {
            val croppedBitmap = CropUtils.cropRegionOfInterest(fullBitmap, focusedBox)
            val dHash = ImageHashUtil.computeDHash(croppedBitmap)
            val result = repository.analyzeFood(croppedBitmap)

            result.fold(
                onSuccess = { mealAnalysis ->
                    _uiState.value = CameraUiState.Success(
                        mealAnalysis = mealAnalysis,
                        originalItems = mealAnalysis.items,
                        thumbnail = croppedBitmap,
                        imageHash = dHash
                    )
                },
                onFailure = { throwable ->
                    _uiState.value = CameraUiState.Error(
                        errorMessage = throwable.localizedMessage ?: "Failed to analyze food image. Please try again."
                    )
                }
            )
        }
    }

    /**
     * Dynamically updates the portion quantity in grams for a specific food item index
     * and recalculates individual and total calories & macronutrients in real time.
     */
    fun updateItemPortion(itemIndex: Int, newGrams: Double) {
        val state = _uiState.value as? CameraUiState.Success ?: return
        val currentMeal = state.mealAnalysis
        val currentItems = currentMeal.items.toMutableList()

        if (itemIndex in currentItems.indices) {
            val origItem = state.originalItems.getOrNull(itemIndex) ?: currentItems[itemIndex]
            val origGrams = if (origItem.portionGrams > 0) origItem.portionGrams else 100.0
            val validNewGrams = newGrams.coerceAtLeast(0.0)
            val ratio = validNewGrams / origGrams

            val updatedItem = origItem.copy(
                portionGrams = validNewGrams,
                calories = origItem.calories * ratio,
                protein = origItem.protein * ratio,
                carbs = origItem.carbs * ratio,
                fat = origItem.fat * ratio,
                fiber = origItem.fiber * ratio
            )

            currentItems[itemIndex] = updatedItem

            val updatedMeal = currentMeal.copy(
                items = currentItems,
                totalCalories = currentItems.sumOf { it.calories },
                totalProtein = currentItems.sumOf { it.protein },
                totalCarbs = currentItems.sumOf { it.carbs },
                totalFats = currentItems.sumOf { it.fat },
                totalFiber = currentItems.sumOf { it.fiber }
            )

            _uiState.value = state.copy(mealAnalysis = updatedMeal)
        }
    }

    /**
     * Directly updates macronutrient quantities (protein, carbs, or fat in grams) for a specific food item
     * and automatically recalculates individual and total meal calories in real time.
     */
    fun updateItemMacros(itemIndex: Int, protein: Double? = null, carbs: Double? = null, fat: Double? = null) {
        val state = _uiState.value as? CameraUiState.Success ?: return
        val currentMeal = state.mealAnalysis
        val currentItems = currentMeal.items.toMutableList()

        if (itemIndex in currentItems.indices) {
            val item = currentItems[itemIndex]
            val newProtein = (protein ?: item.protein).coerceAtLeast(0.0)
            val newCarbs = (carbs ?: item.carbs).coerceAtLeast(0.0)
            val newFat = (fat ?: item.fat).coerceAtLeast(0.0)
            val newCalories = (newProtein * 4.0) + (newCarbs * 4.0) + (newFat * 9.0) + (item.fiber * 4.0)

            val updatedItem = item.copy(
                protein = newProtein,
                carbs = newCarbs,
                fat = newFat,
                calories = newCalories
            )

            currentItems[itemIndex] = updatedItem

            val updatedMeal = currentMeal.copy(
                items = currentItems,
                totalCalories = currentItems.sumOf { it.calories },
                totalProtein = currentItems.sumOf { it.protein },
                totalCarbs = currentItems.sumOf { it.carbs },
                totalFats = currentItems.sumOf { it.fat },
                totalFiber = currentItems.sumOf { it.fiber }
            )

            _uiState.value = state.copy(mealAnalysis = updatedMeal)
        }
    }

    /**
     * Updates the display name of the scanned meal.
     */
    fun updateMealName(newName: String) {
        val state = _uiState.value as? CameraUiState.Success ?: return
        val updatedMeal = state.mealAnalysis.copy(mealName = newName)
        _uiState.value = state.copy(mealAnalysis = updatedMeal)
    }

    /**
     * Confirms the finalized (edited) meal analysis and persists it into Room local database.
     */
    fun confirmAndSaveMeal(onSaved: () -> Unit) {
        val state = _uiState.value as? CameraUiState.Success ?: return
        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMeal(state.mealAnalysis, state.imageHash)
            _uiState.value = CameraUiState.Scanning()
            withContext(Dispatchers.Main) {
                onSaved()
            }
        }
    }

    /**
     * Resets the camera UI state back to real-time scanning mode without saving anything.
     */
    fun retakePhoto() {
        _uiState.value = CameraUiState.Scanning()
    }

    /**
     * Resets the camera UI state back to real-time scanning mode.
     */
    fun resetState() {
        _uiState.value = CameraUiState.Scanning()
    }
}
