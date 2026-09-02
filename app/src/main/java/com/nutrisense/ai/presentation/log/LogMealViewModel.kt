package com.nutrisense.ai.presentation.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisense.ai.domain.model.FoodItem
import com.nutrisense.ai.domain.model.MealAnalysis
import com.nutrisense.ai.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class LogMealUiState(
    val mealName: String = "Lunch",
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String = formatDate(System.currentTimeMillis()),
    val timeFormatted: String = formatTime(System.currentTimeMillis()),
    val portionGrams: String = "150",
    val proteinGrams: String = "35",
    val carbsGrams: String = "45",
    val fatGrams: String = "15",
    val searchQuery: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
) {
    val portionVal: Double get() = portionGrams.toDoubleOrNull() ?: 150.0
    val proteinVal: Double get() = proteinGrams.toDoubleOrNull() ?: 0.0
    val carbsVal: Double get() = carbsGrams.toDoubleOrNull() ?: 0.0
    val fatVal: Double get() = fatGrams.toDoubleOrNull() ?: 0.0

    val calculatedCalories: Double
        get() = (proteinVal * 4) + (carbsVal * 4) + (fatVal * 9)

    val proteinCalories: Double get() = proteinVal * 4
    val carbsCalories: Double get() = carbsVal * 4
    val fatCalories: Double get() = fatVal * 9

    companion object {
        fun formatDate(millis: Long): String {
            val today = Calendar.getInstance()
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            return if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            ) {
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                "Today, ${fmt.format(cal.time)}"
            } else {
                val fmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                fmt.format(cal.time)
            }
        }

        fun formatTime(millis: Long): String {
            val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
            return fmt.format(Date(millis))
        }
    }
}

@HiltViewModel
class LogMealViewModel @Inject constructor(
    private val repository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogMealUiState())
    val uiState: StateFlow<LogMealUiState> = _uiState.asStateFlow()

    fun updateMealName(name: String) = _uiState.update { it.copy(mealName = name) }
    fun updatePortion(portion: String) = _uiState.update { it.copy(portionGrams = portion) }
    fun updateProtein(protein: String) = _uiState.update { it.copy(proteinGrams = protein) }
    fun updateCarbs(carbs: String) = _uiState.update { it.copy(carbsGrams = carbs) }
    fun updateFat(fat: String) = _uiState.update { it.copy(fatGrams = fat) }
    fun updateSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun updateSelectedDate(dateMillis: Long) {
        val currentCal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.timestamp
        }
        val hour = currentCal.get(Calendar.HOUR_OF_DAY)
        val minute = currentCal.get(Calendar.MINUTE)

        val selectedCal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val newTime = selectedCal.timeInMillis
        _uiState.update {
            it.copy(
                timestamp = newTime,
                dateFormatted = LogMealUiState.formatDate(newTime)
            )
        }
    }

    fun updateSelectedTime(hour: Int, minute: Int) {
        val currentCal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.timestamp
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val newTime = currentCal.timeInMillis
        _uiState.update {
            it.copy(
                timestamp = newTime,
                timeFormatted = LogMealUiState.formatTime(newTime)
            )
        }
    }

    fun saveMeal(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        val meal = MealAnalysis(
            id = UUID.randomUUID().toString(),
            mealName = state.mealName.ifBlank { "Manual Log" },
            totalCalories = state.calculatedCalories,
            totalProtein = state.proteinVal,
            totalCarbs = state.carbsVal,
            totalFats = state.fatVal,
            totalFiber = 0.0,
            items = listOf(
                FoodItem(
                    name = state.mealName.ifBlank { "Manual Entry" },
                    portionGrams = state.portionVal,
                    calories = state.calculatedCalories,
                    protein = state.proteinVal,
                    carbs = state.carbsVal,
                    fat = state.fatVal
                )
            ),
            dietaryFlags = emptyList(),
            timestamp = state.timestamp
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMeal(meal)
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
