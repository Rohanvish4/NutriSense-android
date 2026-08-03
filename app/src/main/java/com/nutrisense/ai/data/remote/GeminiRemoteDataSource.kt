package com.nutrisense.ai.data.remote

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.nutrisense.ai.data.remote.dto.MealAnalysisDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for food analysis using Gemini.
 */
@Singleton
class GeminiRemoteDataSource @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val json: Json
) {

    /**
     * Analyzes a food image bitmap.
     *
     * @param bitmap The image to analyze.
     * @return [Result] wrapping [MealAnalysisDto].
     */
    suspend fun analyzeFoodImage(bitmap: Bitmap): Result<MealAnalysisDto> = withContext(Dispatchers.IO) {
        try {
            val prompt = content {
                image(bitmap)
                text(
                    "Analyze the provided food image in detail. Identify all distinct food items and ingredients, " +
                    "estimate their portion weights in grams, calculate individual and aggregate calories and macronutrients " +
                    "(protein, carbohydrates, fats, dietary fiber), assign dietary indicators (e.g. Keto, High-Protein), " +
                    "and flag potential allergens. Output strictly structured JSON adhering to the target schema."
                )
            }

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text
                ?: throw IllegalStateException("Empty response from Gemini.")

            val dto = json.decodeFromString<MealAnalysisDto>(responseText)
            Result.success(dto)
        } catch (e: Throwable) {
            val rawMessage = e.message ?: "Unknown error during food analysis."
            val cleanMessage = when {
                rawMessage.contains("404") || rawMessage.contains("NOT_FOUND", ignoreCase = true) ->
                    "Model endpoint not found. Please verify API configuration."
                rawMessage.contains("403") || rawMessage.contains("PERMISSION_DENIED", ignoreCase = true) ->
                    "Permission denied. Check API key validity."
                rawMessage.contains("429") || rawMessage.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ->
                    "Rate limit exceeded. Please try again later."
                else -> rawMessage
            }
            Result.failure(Exception(cleanMessage, e))
        }
    }
}
