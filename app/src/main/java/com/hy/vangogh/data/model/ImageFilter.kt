package com.hy.vangogh.data.model

import android.graphics.ColorMatrix
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a single image filter with adjustable parameters for image processing.
 *
 * @property name The name of the filter, intended to be a string resource key.
 * @property brightness A value from -1f to 1f, where 0f is the original brightness.
 * @property contrast A value from 0f to 2f, where 1f is the original contrast.
 * @property saturation A value from 0f to 2f, where 1f is the original saturation.
 * @property hue A value in degrees from -180f to 180f, where 0f is the original hue.
 * @property warmth A value from -1f to 1f, where 0f is no warmth adjustment.
 * @property vignette A value from 0f to 1f, where 0f is no vignette.
 * @property sharpen A value for sharpening, where 0f is no sharpening.
 * @property blur A value for blurring, where 0f is no blur.
 */
data class ImageFilter(
    val name: String,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val hue: Float = 0f,
    val warmth: Float = 0f,
    val vignette: Float = 0f,
    val sharpen: Float = 0f,
    val blur: Float = 0f
) {
    init {
        // Validate input ranges
        require(brightness in -1f..1f) { "Brightness must be in range [-1, 1]" }
        require(contrast in 0f..2f) { "Contrast must be in range [0, 2]" }
        require(saturation in 0f..2f) { "Saturation must be in range [0, 2]" }
        require(hue in -180f..180f) { "Hue must be in range [-180, 180]" }
        require(warmth in -1f..1f) { "Warmth must be in range [-1, 1]" }
        require(vignette in 0f..1f) { "Vignette must be in range [0, 1]" }
        require(sharpen >= 0f) { "Sharpen must be non-negative" }
        require(blur >= 0f) { "Blur must be non-negative" }
    }

    /**
     * Converts the filter's parameters (excluding vignette, sharpen, and blur) into a single ColorMatrix.
     * Combines saturation, hue, warmth, contrast, and brightness transformations in a single pass.
     *
     * @return A ColorMatrix representing the combined filter effects.
     */
    fun toColorMatrix(): ColorMatrix {
        val matrix = ColorMatrix()

        // Apply saturation
        if (saturation != 1f) {
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(saturation)
            matrix.postConcat(satMatrix)
        }

        // Apply hue rotation
        if (hue != 0f) {
            val hueMatrix = ColorMatrix()
            val cosVal = cos(Math.toRadians(hue.toDouble())).toFloat()
            val sinVal = sin(Math.toRadians(hue.toDouble())).toFloat()
            val lumR = 0.213f
            val lumG = 0.715f
            val lumB = 0.072f

            val hueValues = floatArrayOf(
                lumR + cosVal * (1f - lumR) + sinVal * (-lumR), // R row
                lumG + cosVal * (-lumG) + sinVal * (-lumG),
                lumB + cosVal * (-lumB) + sinVal * (1f - lumB),
                0f, 0f,
                lumR + cosVal * (-lumR) + sinVal * 0.143f, // G row
                lumG + cosVal * (1f - lumG) + sinVal * 0.140f,
                lumB + cosVal * (-lumB) + sinVal * (-0.283f),
                0f, 0f,
                lumR + cosVal * (-lumR) + sinVal * (-(1f - lumR)), // B row
                lumG + cosVal * (-lumG) + sinVal * lumG,
                lumB + cosVal * (1f - lumB) + sinVal * lumB,
                0f, 0f,
                0f, 0f, 0f, 1f, 0f // Alpha row
            )
            hueMatrix.set(hueValues)
            matrix.postConcat(hueMatrix)
        }

        // Apply warmth (red/blue tint adjustment)
        if (warmth != 0f) {
            val r = warmth * 255f
            val b = -warmth * 255f
            val warmthMatrix = ColorMatrix()
            warmthMatrix.set(floatArrayOf(
                1f, 0f, 0f, 0f, r,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, b,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.postConcat(warmthMatrix)
        }

        // Apply contrast and brightness
        if (contrast != 1f || brightness != 0f) {
            val contrastAdjusted = max(0f, min(2f, contrast))
            val brightnessOffset = brightness * 255f
            val contrastMatrix = ColorMatrix()
            contrastMatrix.set(floatArrayOf(
                contrastAdjusted, 0f, 0f, 0f, brightnessOffset,
                0f, contrastAdjusted, 0f, 0f, brightnessOffset,
                0f, 0f, contrastAdjusted, 0f, brightnessOffset,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.postConcat(contrastMatrix)
        }

        return matrix
    }

    companion object {
        // Predefined filter presets
        val NONE = ImageFilter("filter_original")
        val VINTAGE = ImageFilter("filter_vintage", brightness = 0.1f, contrast = 1.2f, saturation = 0.8f, warmth = 0.2f)
        val VIVID = ImageFilter("filter_vivid", contrast = 1.3f, saturation = 1.4f)
        val MONO = ImageFilter("filter_mono", saturation = 0f, contrast = 1.1f)
        val WARM = ImageFilter("filter_warm", warmth = 0.3f, brightness = 0.05f)
        val COOL = ImageFilter("filter_cool", warmth = -0.2f, saturation = 1.1f)
        val DRAMATIC = ImageFilter("filter_dramatic", contrast = 1.5f, brightness = -0.1f, vignette = 0.3f)
        val SOFT = ImageFilter("filter_soft", brightness = 0.1f, contrast = 0.9f, blur = 0.1f)
        val SHARP = ImageFilter("filter_sharp", contrast = 1.2f, sharpen = 0.3f)

        /**
         * Returns a list of all predefined filters.
         */
        fun getAllFilters() = listOf(
            NONE, VINTAGE, VIVID, MONO, WARM, COOL, DRAMATIC, SOFT, SHARP
        )
    }
}