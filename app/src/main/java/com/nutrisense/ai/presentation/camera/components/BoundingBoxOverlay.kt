package com.nutrisense.ai.presentation.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisense.ai.domain.model.DetectionBox

/**
 * Overlay for drawing bounding boxes on the camera preview.
 */
@Composable
fun BoundingBoxOverlay(
    detectedBoxes: List<DetectionBox>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        detectedBoxes.forEach { box ->
            val left = box.left * canvasWidth
            val top = box.top * canvasHeight
            val width = box.width * canvasWidth
            val height = box.height * canvasHeight

            if (width > 0f && height > 0f) {
                // 1. Semi-transparent fill
                drawRoundRect(
                    color = Color(0x2210B981),
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )

                // 2. Dashed white outline
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 8.dp.toPx()), 0f)
                    )
                )

                // 3. Active emerald accent border
                drawRoundRect(
                    color = Color(0xFF10B981),
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // 4. Attached Label Badge Chip
                val confidencePercent = (box.confidence * 100).toInt()
                val labelText = if (box.label.isNotBlank()) {
                    "${box.label.uppercase()}, C.Score: ($confidencePercent%)"
                } else {
                    "SUBJECT DETECTED ($confidencePercent%)"
                }
                val textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                val textLayoutResult = textMeasurer.measure(labelText, textStyle)
                val badgePaddingX = 8.dp.toPx()
                val badgePaddingY = 4.dp.toPx()
                val badgeWidth = textLayoutResult.size.width + (badgePaddingX * 2)
                val badgeHeight = textLayoutResult.size.height + (badgePaddingY * 2)

                val badgeTopLeft = Offset(
                    x = left.coerceAtLeast(0f),
                    y = (top - badgeHeight).coerceAtLeast(0f)
                )

                // Badge Chip Background (#1A1C1C)
                drawRoundRect(
                    color = Color(0xFF1A1C1C),
                    topLeft = badgeTopLeft,
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Badge Text
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    topLeft = Offset(
                        x = badgeTopLeft.x + badgePaddingX,
                        y = badgeTopLeft.y + badgePaddingY
                    ),
                    style = textStyle
                )
            }
        }
    }
}
