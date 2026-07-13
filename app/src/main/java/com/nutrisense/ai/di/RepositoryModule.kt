package com.nutrisense.ai.di

import com.nutrisense.ai.data.repository.NutritionRepositoryImpl
import com.nutrisense.ai.domain.repository.NutritionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module binding [NutritionRepositoryImpl] to its domain interface [NutritionRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(
        impl: NutritionRepositoryImpl
    ): NutritionRepository
}
