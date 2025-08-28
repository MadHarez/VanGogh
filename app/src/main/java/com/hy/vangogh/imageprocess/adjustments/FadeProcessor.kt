package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

/**
 * 褪色效果处理器
 */
class FadeProcessor {
    
    /**
     * 应用褪色效果
     * @param bitmap 输入图像
     * @param intensity 褪色强度 (0.0 到 1.0)
     * @param fadeColor 褪色颜色 (默认白色)
     * @return 处理后的图像
     */
    fun process(bitmap: Bitmap, intensity: Float, fadeColor: Int = Color.WHITE): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val fadeR = Color.red(fadeColor)
        val fadeG = Color.green(fadeColor)
        val fadeB = Color.blue(fadeColor)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // 混合原色和褪色颜色
            val newRed = (red * (1f - intensity) + fadeR * intensity).roundToInt().coerceIn(0, 255)
            val newGreen = (green * (1f - intensity) + fadeG * intensity).roundToInt().coerceIn(0, 255)
            val newBlue = (blue * (1f - intensity) + fadeB * intensity).roundToInt().coerceIn(0, 255)
            
            pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
}
