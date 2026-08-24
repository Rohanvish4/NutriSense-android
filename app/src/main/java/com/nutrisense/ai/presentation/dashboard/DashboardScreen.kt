package com.nutrisense.ai.presentation.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nutrisense.ai.domain.model.MealAnalysis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToCamera: () -> Unit,
    onNavigateToLogMeal: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onMealClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDateEpochMs
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.selectDate(millis)
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NutriSense AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1C)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = Color(0xFF1A1C1C)
                        )
                    }
                    IconButton(onClick = onNavigateToLogMeal) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Manual Log",
                            tint = Color(0xFF006948)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F9F9))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCamera,
                containerColor = Color(0xFF006948),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan Food",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Date Selector Bar
            item {
                DateSelectorBar(
                    dateTitle = uiState.dateTitle,
                    onPreviousDate = { viewModel.changeDate(-1) },
                    onNextDate = { viewModel.changeDate(1) },
                    onPickDate = { showDatePicker = true }
                )
            }

            // 1. Hero Circular Calorie Ring
            item {
                CalorieHeroRing(
                    consumed = uiState.consumedCalories,
                    target = uiState.targetCalories,
                    progress = uiState.calorieProgress
                )
            }

            // 2. Macro Target Row
            item {
                MacroTargetsRow(
                    proteinConsumed = uiState.consumedProtein,
                    proteinTarget = uiState.targetProtein,
                    proteinProgress = uiState.proteinProgress,
                    carbsConsumed = uiState.consumedCarbs,
                    carbsTarget = uiState.targetCarbs,
                    carbsProgress = uiState.carbsProgress,
                    fatConsumed = uiState.consumedFat,
                    fatTarget = uiState.targetFat,
                    fatProgress = uiState.fatProgress
                )
            }

            // 3. Meal Timeline Section Header
            item {
                Text(
                    text = "Logged Meals (${uiState.meals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1C)
                )
            }

            // 4. Meal Items List
            if (uiState.meals.isEmpty()) {
                item {
                    EmptyLogCard(onScanClick = onNavigateToCamera)
                }
            } else {
                items(
                    items = uiState.meals,
                    key = { it.id }
                ) { meal ->
                    MealLogItem(
                        meal = meal,
                        onDelete = { viewModel.deleteMeal(meal.id) },
                        onClick = { onMealClick(meal.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun DateSelectorBar(
    dateTitle: String,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onPickDate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousDate,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day",
                    tint = Color(0xFF3D4A42),
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onPickDate)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    tint = Color(0xFF006948),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = dateTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1C)
                )
            }

            IconButton(
                onClick = onNextDate,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Day",
                    tint = Color(0xFF3D4A42),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CalorieHeroRing(
    consumed: Double,
    target: Double,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "calorieRingProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    drawArc(
                        color = Color(0xFFE2E2E2),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = Color(0xFF006948),
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${consumed.toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1C)
                    )
                    Text(
                        text = "/ ${target.toInt()} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3D4A42)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val remaining = (target - consumed).coerceAtLeast(0.0)
            Text(
                text = if (consumed > target) "Exceeded by ${(consumed - target).toInt()} kcal" else "${remaining.toInt()} kcal remaining",
                style = MaterialTheme.typography.labelLarge,
                color = if (consumed > target) MaterialTheme.colorScheme.error else Color(0xFF006948),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MacroTargetsRow(
    proteinConsumed: Double,
    proteinTarget: Double,
    proteinProgress: Float,
    carbsConsumed: Double,
    carbsTarget: Double,
    carbsProgress: Float,
    fatConsumed: Double,
    fatTarget: Double,
    fatProgress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MacroCard(
            modifier = Modifier.weight(1f),
            label = "PROTEIN",
            consumed = proteinConsumed,
            target = proteinTarget,
            progress = proteinProgress,
            fillColor = Color(0xFF0051D5),
            trackColor = Color(0xFFDBE1FF)
        )

        MacroCard(
            modifier = Modifier.weight(1f),
            label = "CARBS",
            consumed = carbsConsumed,
            target = carbsTarget,
            progress = carbsProgress,
            fillColor = Color(0xFF8D4B00),
            trackColor = Color(0xFFFFDCC3)
        )

        MacroCard(
            modifier = Modifier.weight(1f),
            label = "FAT",
            consumed = fatConsumed,
            target = fatTarget,
            progress = fatProgress,
            fillColor = Color(0xFFBA1A1A),
            trackColor = Color(0xFFFFDAD6)
        )
    }
}

@Composable
private fun MacroCard(
    modifier: Modifier = Modifier,
    label: String,
    consumed: Double,
    target: Double,
    progress: Float,
    fillColor: Color,
    trackColor: Color
) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3D4A42),
                letterSpacing = 0.5.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "${consumed.toInt()}g",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1C),
                    maxLines = 1
                )
                Text(
                    text = "of ${target.toInt()}g target",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3D4A42),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = fillColor,
                trackColor = trackColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealLogItem(
    meal: MealAnalysis,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var isDismissed by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isDismissed = true
                true
            } else false
        }
    )

    LaunchedEffect(isDismissed) {
        if (isDismissed) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFBA1A1A))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Meal",
                    tint = Color.White
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Row: Meal Title + Delete Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = meal.mealName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1C),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFBA1A1A).copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom Row: Timestamp & Calories + Macro Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(meal.timestamp))
                    Text(
                        text = "$timeString • ${meal.totalCalories.toInt()} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3D4A42),
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MacroPill("${meal.totalProtein.toInt()}P", Color(0xFFDBE1FF), Color(0xFF00174B))
                        MacroPill("${meal.totalCarbs.toInt()}C", Color(0xFFFFDCC3), Color(0xFF2F1500))
                        MacroPill("${meal.totalFats.toInt()}F", Color(0xFFFFDAD6), Color(0xFF93000A))
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroPill(text: String, containerColor: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun EmptyLogCard(onScanClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color(0xFF006948),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Meals Logged for Selected Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1C)
            )
            Text(
                text = "Point your camera at food to scan or log manually.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3D4A42),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Scan Meal")
            }
        }
    }
}
