package com.nutrisense.ai.presentation.log

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMealScreen(
    viewModel: LogMealViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onNavigateToScan: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.timestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateSelectedDate(millis)
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
                ) {
                    Text("OK")
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

    // TimePicker Dialog Trigger
    val openTimePicker = {
        val cal = Calendar.getInstance().apply { timeInMillis = uiState.timestamp }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                viewModel.updateSelectedTime(selectedHour, selectedMinute)
            },
            hour,
            minute,
            false
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Log Meal Manually",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1C)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF3D4A42)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F9F9))
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveMeal(onSuccess = onClose)
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Meal Title & Date/Time Pickers
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.mealName,
                    onValueChange = { viewModel.updateMealName(it) },
                    label = { Text("Meal Title", color = Color(0xFF3D4A42)) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(0xFF1A1C1C),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1C),
                        unfocusedTextColor = Color(0xFF1A1C1C),
                        focusedBorderColor = Color(0xFF006948),
                        unfocusedBorderColor = Color(0xFFBCCAC0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedLabelColor = Color(0xFF006948),
                        unfocusedLabelColor = Color(0xFF3D4A42)
                    )
                )

                // Date & Time Picker Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Date Picker Selector
                    PickerCardButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        value = uiState.dateFormatted,
                        onClick = { showDatePicker = true }
                    )

                    // Time Picker Selector
                    PickerCardButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccessTime,
                        label = "Time",
                        value = uiState.timeFormatted,
                        onClick = openTimePicker
                    )
                }
            }

            // Section 2: Portion & Manual Macros Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Portion & Nutrition Macros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1C)
                    )

                    // Portion Size Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Portion Weight (g)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3D4A42)
                        )

                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFF006948), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                BasicTextField(
                                    value = uiState.portionGrams,
                                    onValueChange = { viewModel.updatePortion(it) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        color = Color(0xFF1A1C1C),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF006948)),
                                    modifier = Modifier.weight(1f)
                                )
                                Text("g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D4A42))
                            }
                        }
                    }

                    // Macronutrients (Protein, Carbs, Fat) Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroInputField(
                            modifier = Modifier.weight(1f),
                            label = "Protein",
                            value = uiState.proteinGrams,
                            onValueChange = { viewModel.updateProtein(it) },
                            accentColor = Color(0xFF0051D5)
                        )

                        MacroInputField(
                            modifier = Modifier.weight(1f),
                            label = "Carbs",
                            value = uiState.carbsGrams,
                            onValueChange = { viewModel.updateCarbs(it) },
                            accentColor = Color(0xFF8D4B00)
                        )

                        MacroInputField(
                            modifier = Modifier.weight(1f),
                            label = "Fat",
                            value = uiState.fatGrams,
                            onValueChange = { viewModel.updateFat(it) },
                            accentColor = Color(0xFFBA1A1A)
                        )
                    }
                }
            }

            // Section 3: Dynamic Calculated Calorie Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF006948))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "TOTAL CALCULATED ENERGY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${uiState.calculatedCalories.toInt()}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kcal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "P: ${uiState.proteinCalories.toInt()} kcal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "C: ${uiState.carbsCalories.toInt()} kcal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "F: ${uiState.fatCalories.toInt()} kcal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerCardButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF006948),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3D4A42)
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1C)
                )
            }
        }
    }
}

@Composable
private fun MacroInputField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: Color
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label (g)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.5.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    color = Color(0xFF1A1C1C),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(accentColor),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
