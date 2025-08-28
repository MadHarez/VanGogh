package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.annotation.FloatRange
import com.hy.vangogh.imageprocess.core.AdjustmentProcessor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * A processor for high-performance image brightness adjustment.
 * Supports multiple algorithms and optimizations.
 */
class BrightnessProcessor : AdjustmentProcessor {

    companion object {
        const val MIN_BRIGHTNESS = -1f
        const val MAX_BRIGHTNESS = 1f
        const val DEFAULT_BRIGHTNESS = 0f

        // Brightness adjustment modes
        enum class BrightnessMode {
            LINEAR,      // Linear adjustment using ColorMatrix
            GAMMA,       // Gamma correction for more natural effect
            ADAPTIVE     // Adaptive adjustment based on image content
        }
    }

    private var mode = BrightnessMode.LINEAR

    /**
     * Processes image brightness based on the selected mode.
     * @param bitmap The original bitmap.
     * @param value The brightness value (-1.0 to 1.0, 0 is original).
     * @return The processed bitmap.
     */
    override fun process(bitmap: Bitmap, value: Float): Bitmap {
        val clampedValue = clampBrightness(value)
        return when (mode) {
            BrightnessMode.LINEAR -> Adjuster.linear(bitmap, clampedValue)
            BrightnessMode.GAMMA -> Adjuster.gamma(bitmap, clampedValue)
            BrightnessMode.ADAPTIVE -> Adjuster.adaptive(bitmap, clampedValue)
        }
    }

    /**
     * Sets the brightness adjustment mode.
     */
    fun setBrightnessMode(mode: BrightnessMode) {
        this.mode = mode
    }

    /**
     * Gets the current brightness adjustment mode.
     */
    fun getBrightnessMode(): BrightnessMode = mode

    /**
     * Returns the valid range for the brightness value.
     */
    fun getValueRange(): ClosedFloatingPointRange<Float> = MIN_BRIGHTNESS..MAX_BRIGHTNESS

    /**
     * Returns the default brightness value.
     */
    fun getDefaultValue(): Float = DEFAULT_BRIGHTNESS

    /**
     * Contains the specific brightness adjustment algorithms.
     */
    private object Adjuster {

        /**
         * Linear brightness adjustment using ColorMatrix.
         *
         * This is a highly optimized method for linear adjustments using GPU/hardware acceleration.
         */
        fun linear(bitmap: Bitmap, brightness: Float): Bitmap {
            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                val brightnessOffset = brightness * 255f
                colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                    set(floatArrayOf(
                        1f, 0f, 0f, 0f, brightnessOffset,
                        0f, 1f, 0f, 0f, brightnessOffset,
                        0f, 0f, 1f, 0f, brightnessOffset,
                        0f, 0f, 0f, 1f, 0f
                    ))
                })
            }
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            return result
        }

        /**
         * Gamma correction brightness adjustment.
         *
         * This is a non-linear adjustment that requires a pixel-by-pixel approach.
         * It uses a lookup table (LUT) for performance optimization.
         */
        fun gamma(bitmap: Bitmap, brightness: Float): Bitmap {
            val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val width = result.width
            val height = result.height

            val gamma = if (brightness >= 0) {
                1f / (1f + brightness)
            } else {
                1f - brightness
            }

            // Create a gamma lookup table
            val gammaTable = IntArray(256) { i ->
                (255 * (i / 255f).pow(gamma)).toInt().coerceIn(0, 255)
            }

            val pixels = IntArray(width * height)
            result.getPixels(pixels, 0, width, 0, 0, width, height)

            runBlocking(Dispatchers.Default) {
                val chunkSize = pixels.size / Runtime.getRuntime().availableProcessors()
                (0 until pixels.size step chunkSize).map { start ->
                    async {
                        val end = min(start + chunkSize, pixels.size)
                        for (i in start until end) {
                            val pixel = pixels[i]
                            val r = (pixel shr 16) and 0xFF
                            val g = (pixel shr 8) and 0xFF
                            val b = pixel and 0xFF
                            pixels[i] = (pixel and 0xFF000000.toInt()) or
                                    (gammaTable[r] shl 16) or
                                    (gammaTable[g] shl 8) or
                                    gammaTable[b]
                        }
                    }
                }.forEach { it.await() }
            }

            result.setPixels(pixels, 0, width, 0, 0, width, height)
            return result
        }

        /**
         * Adaptive brightness adjustment based on the image's average brightness.
         */
        fun adaptive(bitmap: Bitmap, brightness: Float): Bitmap {
            val avgBrightness = calculateAverageBrightness(bitmap)
            val adaptiveBrightness = when {
                avgBrightness < 0.3f -> brightness * 1.2f
                avgBrightness > 0.7f -> brightness * 0.8f
                else -> brightness
            }
            return linear(bitmap, adaptiveBrightness)
        }

        /**
         * Calculates the average perceived brightness of a bitmap.
         */
        private fun calculateAverageBrightness(bitmap: Bitmap): Float {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            var totalBrightness = 0f
            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                totalBrightness += (0.299f * r + 0.587f * g + 0.114f * b)
            }
            return (totalBrightness / pixels.size) / 255f
        }
    }

    /**
     * Clamps the brightness value to the valid range.
     */
    private fun clampBrightness(@FloatRange(from = -1.0, to = 1.0) brightness: Float): Float {
        return max(MIN_BRIGHTNESS, min(MAX_BRIGHTNESS, brightness))
    }
}