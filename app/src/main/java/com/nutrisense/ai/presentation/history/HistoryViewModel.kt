package com.nutrisense.ai.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisense.ai.domain.model.MealAnalysis
import com.nutrisense.ai.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DailyMealGroup(
    val dateHeader: String,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val meals: List<MealAnalysis>
)

data class HistoryUiState(
    val groups: List<DailyMealGroup> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: NutritionRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.getAllMeals().map { meals ->
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val groupedMap = meals.groupBy { meal ->
            dateFormat.format(Date(meal.timestamp))
        }

        val groups = groupedMap.map { (dateHeader, mealList) ->
            DailyMealGroup(
                dateHeader = dateHeader,
                totalCalories = mealList.sumOf { it.totalCalories },
                totalProtein = mealList.sumOf { it.totalProtein },
                totalCarbs = mealList.sumOf { it.totalCarbs },
                totalFat = mealList.sumOf { it.totalFats },
                meals = mealList
            )
        }

        HistoryUiState(groups = groups, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun deleteMeal(mealId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeal(mealId)
        }
    }
}
