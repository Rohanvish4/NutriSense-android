package com.nutrisense.ai.data.local

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.scale

/**
 * Utility class implementing a 64-bit difference hashing (dHash) algorithm for fast, offline
 * perceptual image similarity comparisons and semantic caching.
 */
object ImageHashUtil {

    /**
     * Computes a 64-bit dHash perceptual fingerprint from an input image [Bitmap].
     *
     * 1. Resizes the image to 9x8 pixels.
     * 2. Converts pixels to luminance (grayscale).
     * 3. Compares adjacent horizontal pixels to produce a 64-bit binary sequence.
     *
     * @param bitmap Input image bitmap.
     * @return 64-bit [Long] hash value.
     */
    fun computeDHash(bitmap: Bitmap): Long {
        val scaled = bitmap.scale(9, 8, filter = true)
        var hash = 0L

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val leftPixel = scaled.getPixel(x, y)
                val rightPixel = scaled.getPixel(x + 1, y)

                val leftGray = (Color.red(leftPixel) * 0.299 + Color.green(leftPixel) * 0.587 + Color.blue(leftPixel) * 0.114)
                val rightGray = (Color.red(rightPixel) * 0.299 + Color.green(rightPixel) * 0.587 + Color.blue(rightPixel) * 0.114)

                if (leftGray > rightGray) {
                    val bitIndex = y * 8 + x
                    hash = hash or (1L shl bitIndex)
                }
            }
        }

        if (scaled != bitmap && !scaled.isRecycled) {
            scaled.recycle()
        }

        return hash
    }

    /**
     * Calculates the Hamming distance (number of differing bits) between two 64-bit perceptual hashes.
     */
    fun calculateHammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
    }

    /**
     * Determines whether two perceptual hashes indicate visual similarity within a specified bit tolerance.
     *
     * @param maxThreshold Maximum differing bits (default <= 10 out of 64 bits).
     */
    fun isVisuallySimilar(hash1: Long, hash2: Long, maxThreshold: Int = 10): Boolean {
        return calculateHammingDistance(hash1, hash2) <= maxThreshold
    }
}
