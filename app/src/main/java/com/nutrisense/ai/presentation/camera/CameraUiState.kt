package com.nutrisense.ai.presentation.camera

import android.graphics.Bitmap
import com.nutrisense.ai.domain.model.DetectionBox
import com.nutrisense.ai.domain.model.FoodItem
import com.nutrisense.ai.domain.model.MealAnalysis

/**
 * Unidirectional UI State hierarchy for the Camera Scanning feature.
 */
sealed interface CameraUiState {
    data object Idle : CameraUiState
    data class Scanning(val detectedBoxes: List<DetectionBox> = emptyList()) : CameraUiState
    data object Capturing : CameraUiState
    data class Analyzing(val message: String = "Analyzing meal with Gemini AI...") : CameraUiState
    data class Success(
        val mealAnalysis: MealAnalysis,
        val originalItems: List<FoodItem>,
        val thumbnail: Bitmap,
        val imageHash: Long? = null,
        val isSaving: Boolean = false
    ) : CameraUiState
    data class Error(val errorMessage: String) : CameraUiState
}
