package com.nutrisense.ai.presentation.camera.analyzer

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.nutrisense.ai.domain.model.DetectionBox

/**
 * Analyzes camera frames for food objects using ML Kit.
 */
class FoodObjectAnalyzer(
    private val onObjectsDetected: (List<DetectionBox>) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .enableMultipleObjects()
        .build()

    private val detector = ObjectDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        // Swap width and height if frame is rotated 90 or 270 degrees
        val isRotated = rotationDegrees == 90 || rotationDegrees == 270
        val frameWidth = if (isRotated) imageProxy.height.toFloat() else imageProxy.width.toFloat()
        val frameHeight = if (isRotated) imageProxy.width.toFloat() else imageProxy.height.toFloat()

        detector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                val boxes = detectedObjects.map { obj ->
                    val rect = obj.boundingBox

                    val left = (rect.left / frameWidth).coerceIn(0f, 1f)
                    val top = (rect.top / frameHeight).coerceIn(0f, 1f)
                    val right = (rect.right / frameWidth).coerceIn(left, 1f)
                    val bottom = (rect.bottom / frameHeight).coerceIn(top, 1f)

                    val label = obj.labels.firstOrNull()?.text ?: "Food Item"
                    val confidence = obj.labels.firstOrNull()?.confidence ?: 0.0f
                    val trackingId = obj.trackingId

                    DetectionBox(
                        left = left,
                        top = top,
                        right = right,
                        bottom = bottom,
                        label = label,
                        confidence = confidence,
                        trackingId = trackingId
                    )
                }
                onObjectsDetected(boxes)
            }
            .addOnFailureListener {
                onObjectsDetected(emptyList())
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
