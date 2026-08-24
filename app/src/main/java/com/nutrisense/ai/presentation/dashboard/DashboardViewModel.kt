package com.nutrisense.ai.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisense.ai.domain.model.MealAnalysis
import com.nutrisense.ai.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the dashboard screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: NutritionRepository
) : ViewModel() {

    private val _selectedDateCalendar = MutableStateFlow(Calendar.getInstance())

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllMeals(),
        _selectedDateCalendar
    ) { allMeals, calendar ->
        val dateTitle = formatDateTitle(calendar)

        // Filter meals that match the selected day
        val dayMeals = filterMealsForDay(allMeals, calendar)

        val totalCalories = dayMeals.sumOf { it.totalCalories }
        val totalProtein = dayMeals.sumOf { it.totalProtein }
        val totalCarbs = dayMeals.sumOf { it.totalCarbs }
        val totalFat = dayMeals.sumOf { it.totalFats }

        DashboardUiState(
            selectedDateEpochMs = calendar.timeInMillis,
            dateTitle = dateTitle,
            consumedCalories = totalCalories,
            consumedProtein = totalProtein,
            consumedCarbs = totalCarbs,
            consumedFat = totalFat,
            meals = dayMeals
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun deleteMeal(mealId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeal(mealId)
        }
    }

    fun changeDate(offsetDays: Int) {
        val newCal = Calendar.getInstance().apply {
            timeInMillis = _selectedDateCalendar.value.timeInMillis
            add(Calendar.DAY_OF_YEAR, offsetDays)
        }
        _selectedDateCalendar.value = newCal
    }

    fun selectDate(millis: Long) {
        val newCal = Calendar.getInstance().apply {
            timeInMillis = millis
        }
        _selectedDateCalendar.value = newCal
    }

    private fun filterMealsForDay(meals: List<MealAnalysis>, calendar: Calendar): List<MealAnalysis> {
        val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        val mealCal = Calendar.getInstance()
        return meals.filter { meal ->
            mealCal.timeInMillis = meal.timestamp
            mealCal.get(Calendar.DAY_OF_YEAR) == targetDay && mealCal.get(Calendar.YEAR) == targetYear
        }
    }

    private fun formatDateTitle(calendar: Calendar): String {
        val today = Calendar.getInstance()
        return if (calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        ) {
            val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
            "Today, ${formatter.format(calendar.time)}"
        } else {
            val formatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            formatter.format(calendar.time)
        }
    }
}
