package com.nutrisense.ai.presentation.results

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nutrisense.ai.domain.model.FoodItem
import com.nutrisense.ai.domain.model.MealAnalysis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    mealId: String,
    viewModel: ResultsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mealId) {
        viewModel.loadMeal(mealId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if ((uiState as? ResultsUiState.Success)?.isEditing == true) "Edit Meal Log" else "Meal Breakdown",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1C)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1A1C1C)
                        )
                    }
                },
                actions = {
                    if (uiState is ResultsUiState.Success) {
                        val state = uiState as ResultsUiState.Success
                        // Edit Toggle Button
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Meal",
                                tint = if (state.isEditing) Color(0xFF006948) else Color(0xFF3D4A42)
                            )
                        }

                        // Delete Button
                        IconButton(
                            onClick = {
                                viewModel.deleteMeal(mealId, onDeleted = onNavigateBack)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Meal",
                                tint = Color(0xFFBA1A1A)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F9F9))
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { innerPadding ->
        when (val state = uiState) {
            is ResultsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF006948))
                }
            }

            is ResultsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }

            is ResultsUiState.Success -> {
                ResultsContent(
                    state = state,
                    viewModel = viewModel,
                    innerPadding = innerPadding,
                    onDone = onNavigateBack
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultsContent(
    state: ResultsUiState.Success,
    viewModel: ResultsViewModel,
    innerPadding: PaddingValues,
    onDone: () -> Unit
) {
    val meal = state.mealAnalysis

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Summary Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.isEditing) {
                        OutlinedTextField(
                            value = meal.mealName,
                            onValueChange = { viewModel.updateMealName(it) },
                            label = { Text("Meal Name", color = Color(0xFF3D4A42)) },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF1A1C1C),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1A1C1C),
                                unfocusedTextColor = Color(0xFF1A1C1C),
                                focusedBorderColor = Color(0xFF006948),
                                unfocusedBorderColor = Color(0xFFBCCAC0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    } else {
                        Text(
                            text = meal.mealName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1C),
                            textAlign = TextAlign.Center
                        )
                    }

                    val timeFormatted = SimpleDateFormat("h:mm a • EEE, MMM d", Locale.getDefault()).format(Date(meal.timestamp))
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3D4A42),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${meal.totalCalories.toInt()}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006948)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kcal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3D4A42),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    if (meal.dietaryFlags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            meal.dietaryFlags.forEach { flag ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDCFCE7), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = flag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF14532D)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Macronutrients Distribution Row
        item {
            Text(
                text = "Macronutrients Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroDetailCard(
                    modifier = Modifier.weight(1f),
                    label = "Protein",
                    grams = meal.totalProtein,
                    color = Color(0xFF0051D5)
                )
                MacroDetailCard(
                    modifier = Modifier.weight(1f),
                    label = "Carbs",
                    grams = meal.totalCarbs,
                    color = Color(0xFF8D4B00)
                )
                MacroDetailCard(
                    modifier = Modifier.weight(1f),
                    label = "Fat",
                    grams = meal.totalFats,
                    color = Color(0xFFBA1A1A)
                )
            }
        }

        // 3. Itemized Ingredients Section
        item {
            Text(
                text = if (state.isEditing) "Edit Food Items & Macros" else "Detected Ingredients (${meal.items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1C)
            )
        }

        itemsIndexed(meal.items) { index, item ->
            if (state.isEditing) {
                EditableFoodItemRow(
                    item = item,
                    onPortionChanged = { newGrams ->
                        viewModel.updateItemPortion(index, newGrams)
                    },
                    onMacroChanged = { protein, carbs, fat ->
                        viewModel.updateItemMacros(index, protein, carbs, fat)
                    }
                )
            } else {
                FoodIngredientCard(item = item)
            }
        }

        // 4. Bottom Action Buttons
        item {
            Spacer(modifier = Modifier.height(8.dp))
            if (state.isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.toggleEditMode() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3D4A42))
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            viewModel.saveMealChanges {
                                onDone()
                            }
                        },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Done", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EditableFoodItemRow(
    item: FoodItem,
    onPortionChanged: (Double) -> Unit,
    onMacroChanged: (protein: Double?, carbs: Double?, fat: Double?) -> Unit
) {
    var portionText by remember(item.portionGrams) {
        mutableStateOf(item.portionGrams.toInt().toString())
    }
    var proteinText by remember(item.protein) {
        mutableStateOf(item.protein.toInt().toString())
    }
    var carbsText by remember(item.carbs) {
        mutableStateOf(item.carbs.toInt().toString())
    }
    var fatText by remember(item.fat) {
        mutableStateOf(item.fat.toInt().toString())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1C),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${item.calories.toInt()} kcal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006948)
                )
            }

            // Portion Stepper Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Portion (g)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3D4A42)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val currentGrams = portionText.toDoubleOrNull() ?: item.portionGrams
                            val newGrams = (currentGrams - 10).coerceAtLeast(0.0)
                            portionText = newGrams.toInt().toString()
                            onPortionChanged(newGrams)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFE2E2E2), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease portion",
                            tint = Color(0xFF1A1C1C),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFF006948), RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            BasicTextField(
                                value = portionText,
                                onValueChange = { input ->
                                    portionText = input
                                    val parsed = input.toDoubleOrNull()
                                    if (parsed != null) {
                                        onPortionChanged(parsed)
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    color = Color(0xFF1A1C1C),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(Color(0xFF006948)),
                                modifier = Modifier.weight(1f)
                            )
                            Text("g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D4A42))
                        }
                    }

                    IconButton(
                        onClick = {
                            val currentGrams = portionText.toDoubleOrNull() ?: item.portionGrams
                            val newGrams = currentGrams + 10
                            portionText = newGrams.toInt().toString()
                            onPortionChanged(newGrams)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFE2E2E2), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase portion",
                            tint = Color(0xFF1A1C1C),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Macro Gram Inputs (P, C, F)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ResultMacroInput(
                    modifier = Modifier.weight(1f),
                    label = "Protein",
                    value = proteinText,
                    onValueChange = { input ->
                        proteinText = input
                        val parsed = input.toDoubleOrNull()
                        onMacroChanged(parsed, null, null)
                    },
                    color = Color(0xFF0051D5)
                )

                ResultMacroInput(
                    modifier = Modifier.weight(1f),
                    label = "Carbs",
                    value = carbsText,
                    onValueChange = { input ->
                        carbsText = input
                        val parsed = input.toDoubleOrNull()
                        onMacroChanged(null, parsed, null)
                    },
                    color = Color(0xFF8D4B00)
                )

                ResultMacroInput(
                    modifier = Modifier.weight(1f),
                    label = "Fat",
                    value = fatText,
                    onValueChange = { input ->
                        fatText = input
                        val parsed = input.toDoubleOrNull()
                        onMacroChanged(null, null, parsed)
                    },
                    color = Color(0xFFBA1A1A)
                )
            }
        }
    }
}

@Composable
private fun ResultMacroInput(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label (g)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.5.dp, color.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    color = Color(0xFF1A1C1C),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(color),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MacroDetailCard(
    modifier: Modifier = Modifier,
    label: String,
    grams: Double,
    color: Color
) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3D4A42)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${grams.toInt()}g",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodIngredientCard(item: FoodItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBCCAC0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1C),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${item.calories.toInt()} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006948)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portion: ${item.portionGrams.toInt()}g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF3D4A42)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "${item.protein.toInt()}P", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0051D5))
                    Text(text = "•", fontSize = 11.sp, color = Color(0xFFBCCAC0))
                    Text(text = "${item.carbs.toInt()}C", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8D4B00))
                    Text(text = "•", fontSize = 11.sp, color = Color(0xFFBCCAC0))
                    Text(text = "${item.fat.toInt()}F", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                }
            }

            if (item.allergens.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.allergens.forEach { allergen ->
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFFFDAD6), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFBA1A1A),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = allergen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF93000A)
                            )
                        }
                    }
                }
            }
        }
    }
}
