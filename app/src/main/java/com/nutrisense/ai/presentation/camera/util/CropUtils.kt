package com.nutrisense.ai.presentation.camera.util

import android.graphics.Bitmap
import android.graphics.Matrix
import com.nutrisense.ai.domain.model.DetectionBox

/**
 * Utility for bitmap cropping and processing.
 */
object CropUtils {

    private const val MAX_DIMENSION_PX = 1024

    /**
     * Crops the target region of interest from the source bitmap defined by normalized [detectionBox].
     * If no box is provided or the box covers < 5% of the total image area, returns the full bitmap downsampled.
     *
     * @param source Raw input camera bitmap.
     * @param detectionBox Normalized bounding box coordinates [0.0..1.0].
     * @return Cropped and downsampled bitmap (max dimension <= 1024 px).
     */
    fun cropRegionOfInterest(source: Bitmap, detectionBox: DetectionBox?): Bitmap {
        if (detectionBox == null || detectionBox.width <= 0f || detectionBox.height <= 0f) {
            return downsampleIfNeeded(source)
        }

        val left = (detectionBox.left * source.width).toInt().coerceIn(0, source.width - 1)
        val top = (detectionBox.top * source.height).toInt().coerceIn(0, source.height - 1)
        val right = (detectionBox.right * source.width).toInt().coerceIn(left + 1, source.width)
        val bottom = (detectionBox.bottom * source.height).toInt().coerceIn(top + 1, source.height)

        val cropWidth = right - left
        val cropHeight = bottom - top

        val totalArea = source.width * source.height
        val cropArea = cropWidth * cropHeight

        // If cropped area is < 5% of total image area, use full source bitmap to maintain context
        if (totalArea > 0 && (cropArea.toFloat() / totalArea.toFloat()) < 0.05f) {
            return downsampleIfNeeded(source)
        }

        val cropped = Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
        return downsampleIfNeeded(cropped)
    }

    /**
     * Rotates a bitmap by specified rotation degrees (e.g. 90, 180, 270 degrees).
     */
    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Float): Bitmap {
        if (rotationDegrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Downsamples bitmap proportionally if its maximum dimension exceeds [MAX_DIMENSION_PX] (1024 px).
     */
    fun downsampleIfNeeded(bitmap: Bitmap, maxDim: Int = MAX_DIMENSION_PX): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDim && height <= maxDim) {
            return bitmap
        }

        val aspectRatio = width.toFloat() / height.toFloat()
        val (newWidth, newHeight) = if (width >= height) {
            maxDim to (maxDim / aspectRatio).toInt()
        } else {
            (maxDim * aspectRatio).toInt() to maxDim
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth.coerceAtLeast(1), newHeight.coerceAtLeast(1), true)
    }
}
