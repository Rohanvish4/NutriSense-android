package com.nutrisense.ai.presentation.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nutrisense.ai.domain.model.FoodItem
import com.nutrisense.ai.presentation.camera.analyzer.FoodObjectAnalyzer
import com.nutrisense.ai.presentation.camera.components.BoundingBoxOverlay
import com.nutrisense.ai.presentation.camera.util.CropUtils
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onResultConfirmed: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        if (hasCameraPermission) {
            CameraContent(
                uiState = uiState,
                viewModel = viewModel,
                onClose = onClose,
                onResultConfirmed = onResultConfirmed
            )
        } else {
            PermissionFallbackScreen {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Composable
private fun CameraContent(
    uiState: CameraUiState,
    viewModel: CameraViewModel,
    onClose: () -> Unit,
    onResultConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Live CameraX Viewfinder
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, FoodObjectAnalyzer { boxes ->
                                viewModel.onObjectsDetected(boxes)
                            })
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Real-Time ML Kit Bounding Box Overlay
        if (uiState is CameraUiState.Scanning) {
            BoundingBoxOverlay(
                detectedBoxes = uiState.detectedBoxes,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Top Scrim & Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Camera",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        camera?.cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Toggle Flashlight",
                        tint = Color.White
                    )
                }
            }
        }

        // 4. Bottom Sheet Control Sheet (Scanning / Analyzing / Error)
        if (uiState !is CameraUiState.Success) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .navigationBarsPadding()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFFF9F9F9))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFBCCAC0), RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (uiState) {
                        is CameraUiState.Analyzing -> {
                            Text(
                                text = "Analyzing meal with Gemini AI...",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1A1C1C)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator(color = Color(0xFF006948))
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        is CameraUiState.Error -> {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.resetState() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Try Again")
                            }
                        }

                        else -> {
                            Text(
                                text = "Point camera at your meal",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1A1C1C)
                            )
                            Text(
                                text = "Keep the food within the frame.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF3D4A42)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Primary Circular Capture Button (#006948)
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .border(4.dp, Color(0xFF006948), CircleShape)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF006948))
                                    .clickable {
                                        takePhotoAndAnalyze(
                                            imageCapture = imageCapture,
                                            context = context,
                                            viewModel = viewModel,
                                            uiState = uiState
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Capture Food",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // 5. Interactive Success Overlay with Keyboard-Aware Padding & Smooth Scrolling
        if (uiState is CameraUiState.Success) {
            val successState = uiState as CameraUiState.Success
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    val dialogScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .heightIn(max = 520.dp)
                            .verticalScroll(dialogScrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drag Indicator
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .background(Color(0xFFBCCAC0), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Header Row: Thumbnail + Editable Meal Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = successState.thumbnail.asImageBitmap(),
                                contentDescription = "Meal Thumbnail",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = successState.mealAnalysis.mealName,
                                    onValueChange = { viewModel.updateMealName(it) },
                                    label = { Text("Meal Title", color = Color(0xFF3D4A42)) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color(0xFF1A1C1C),
                                        fontSize = 14.sp,
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
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Calorie & Macro Recalculated Summary Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F3F3), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL CALORIES",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3D4A42),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${successState.mealAnalysis.totalCalories.toInt()} kcal",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF006948),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                MacroBadge("P", "${successState.mealAnalysis.totalProtein.toInt()}g", Color(0xFF0051D5))
                                MacroBadge("C", "${successState.mealAnalysis.totalCarbs.toInt()}g", Color(0xFF8D4B00))
                                MacroBadge("F", "${successState.mealAnalysis.totalFats.toInt()}g", Color(0xFFBA1A1A))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Section Header: Edit Quantities
                        Text(
                            text = "Edit Item Quantities & Macros",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3D4A42),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Itemized Food Items List with Quantity & Macro Inputs
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            successState.mealAnalysis.items.forEachIndexed { index, item ->
                                FoodItemQuantityRow(
                                    item = item,
                                    onPortionChanged = { newGrams ->
                                        viewModel.updateItemPortion(index, newGrams)
                                    },
                                    onMacroChanged = { protein, carbs, fat ->
                                        viewModel.updateItemMacros(index, protein, carbs, fat)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Options Row: RETAKE vs ADD TO LOG
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Option 1: RETAKE PHOTO
                            OutlinedButton(
                                onClick = { viewModel.retakePhoto() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(23.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF3D4A42)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retake",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retake", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            // Option 2: ADD TO LOG
                            Button(
                                onClick = {
                                    viewModel.confirmAndSaveMeal {
                                        onResultConfirmed()
                                    }
                                },
                                enabled = !successState.isSaving,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(23.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
                            ) {
                                if (successState.isSaving) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = "Add to Log",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to Log", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemQuantityRow(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Item Name & Calculated Item Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1C),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${item.calories.toInt()} kcal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006948)
                )
            }

            // Row 1: Portion Size Stepper & Centered Numeric Input
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
                            Text("g", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3D4A42))
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

            // Row 2: Direct Macro Gram Inputs (Protein, Carbs, Fat) with High Contrast Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Protein Grams Input (Blue)
                MacroGramInput(
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

                // Carbs Grams Input (Amber)
                MacroGramInput(
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

                // Fat Grams Input (Red)
                MacroGramInput(
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
private fun MacroGramInput(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
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
private fun MacroBadge(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, color = color, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = value, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C), fontSize = 12.sp)
    }
}

private fun takePhotoAndAnalyze(
    imageCapture: ImageCapture,
    context: android.content.Context,
    viewModel: CameraViewModel,
    uiState: CameraUiState
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)

    imageCapture.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val rotationDegrees = image.imageInfo.rotationDegrees.toFloat()
            val rawBitmap = image.toBitmap()
            val rotatedBitmap = CropUtils.rotateBitmap(rawBitmap, rotationDegrees)
            image.close()

            val primaryBox = if (uiState is CameraUiState.Scanning) {
                uiState.detectedBoxes.firstOrNull()
            } else null

            viewModel.captureAndAnalyze(rotatedBitmap, primaryBox)
        }

        override fun onError(exception: ImageCaptureException) {
            exception.printStackTrace()
        }
    })
}

@Composable
private fun PermissionFallbackScreen(onGrantPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = Color(0xFF006948),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera Access Required",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "NutriSense AI uses your camera to identify food items and calculate nutrition metrics in real time.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGrantPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006948))
        ) {
            Text("Grant Permission")
        }
    }
}
