package com.hy.vangogh.data.model

import androidx.compose.ui.graphics.ColorMatrix

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
    fun toColorMatrix(): ColorMatrix {
        val matrix = ColorMatrix()
        
        // Apply saturation
        if (saturation != 1f) {
            val satMatrix = ColorMatrix()
            satMatrix.setToSaturation(saturation)
            matrix.timesAssign(satMatrix)
        }
        
        // Apply contrast and brightness
        if (contrast != 1f || brightness != 0f) {
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness * 255,
                    0f, contrast, 0f, 0f, brightness * 255,
                    0f, 0f, contrast, 0f, brightness * 255,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.timesAssign(contrastMatrix)
        }
        
        return matrix
    }
    
    companion object {
        // Note: Filter names will be resolved at runtime using string resources
        val NONE = ImageFilter("filter_original")
        val VINTAGE = ImageFilter("filter_vintage", brightness = 0.1f, contrast = 1.2f, saturation = 0.8f, warmth = 0.2f)
        val VIVID = ImageFilter("filter_vivid", contrast = 1.3f, saturation = 1.4f)
        val MONO = ImageFilter("filter_mono", saturation = 0f, contrast = 1.1f)
        val WARM = ImageFilter("filter_warm", warmth = 0.3f, brightness = 0.05f)
        val COOL = ImageFilter("filter_cool", warmth = -0.2f, saturation = 1.1f)
        val DRAMATIC = ImageFilter("filter_dramatic", contrast = 1.5f, brightness = -0.1f, vignette = 0.3f)
        val SOFT = ImageFilter("filter_soft", brightness = 0.1f, contrast = 0.9f, blur = 0.1f)
        val SHARP = ImageFilter("filter_sharp", contrast = 1.2f, sharpen = 0.3f)
        
        fun getAllFilters() = listOf(
            NONE, VINTAGE, VIVID, MONO, WARM, COOL, DRAMATIC, SOFT, SHARP
        )
    }
}
