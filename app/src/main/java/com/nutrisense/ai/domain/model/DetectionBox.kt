package com.nutrisense.ai.domain.model

/**
 * Domain model representing normalized bounding box coordinates for Compose Canvas overlays.
 *
 * Coordinates are normalized to [0.0, 1.0] relative to the camera preview / image frame width and height,
 * allowing resolution-agnostic UI scaling across different screen aspect ratios.
 *
 * @property left Left boundary in normalized coordinates [0.0..1.0].
 * @property top Top boundary in normalized coordinates [0.0..1.0].
 * @property right Right boundary in normalized coordinates [0.0..1.0].
 * @property bottom Bottom boundary in normalized coordinates [0.0..1.0].
 * @property label Optional label detected by on-device ML Kit (e.g., "Food", "Dish").
 * @property confidence ML detection confidence score (0.0 to 1.0).
 * @property trackingId Optional object tracking ID emitted by ML Kit STREAM_MODE.
 */
data class DetectionBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val label: String = "Food Item",
    val confidence: Float = 0f,
    val trackingId: Int? = null
) {
    /**
     * Calculates width of bounding box in normalized space.
     */
    val width: Float get() = (right - left).coerceAtLeast(0f)

    /**
     * Calculates height of bounding box in normalized space.
     */
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
}
