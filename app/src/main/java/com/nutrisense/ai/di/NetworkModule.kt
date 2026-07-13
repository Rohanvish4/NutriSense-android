package com.nutrisense.ai.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.nutrisense.ai.BuildConfig
import com.nutrisense.ai.data.remote.GeminiRemoteDataSource
import com.nutrisense.ai.data.remote.schema.GeminiNutritionSchema
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Hilt Module providing singleton dependencies for remote multimodal network operations.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a configured Kotlinx [Json] instance for resilient JSON deserialization.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Provides the Google [GenerativeModel] singleton configured for `gemini-1.5-flash`
     * with low temperature (0.1f) and strict JSON schema enforcement.
     */
    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val config = generationConfig {
            temperature = 0.1f
            responseMimeType = "application/json"
            responseSchema = GeminiNutritionSchema.schema
        }

        return GenerativeModel(
            modelName = "gemini-3.6-flash",
            apiKey = apiKey,
            generationConfig = config
        )
    }

    /**
     * Provides the [GeminiRemoteDataSource] singleton instance.
     */
    @Provides
    @Singleton
    fun provideGeminiRemoteDataSource(
        generativeModel: GenerativeModel,
        json: Json
    ): GeminiRemoteDataSource {
        return GeminiRemoteDataSource(generativeModel, json)
    }
}
