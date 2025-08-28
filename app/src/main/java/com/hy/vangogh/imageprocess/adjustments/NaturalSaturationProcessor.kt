package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

/**
 * 自然饱和度处理器
 * 与普通饱和度不同，自然饱和度会智能地调整饱和度，避免过度饱和
 */
class NaturalSaturationProcessor {
    
    /**
     * 应用自然饱和度调整
     * @param bitmap 输入图像
     * @param intensity 强度 (-1.0 到 1.0)
     * @return 处理后的图像
     */
    fun process(bitmap: Bitmap, intensity: Float): Bitmap {
        try {
            val width = bitmap.width
            val height = bitmap.height
            val result = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
            
            val pixels = IntArray(width * height)
            result.getPixels(pixels, 0, width, 0, 0, width, height)
            
            for (i in pixels.indices) {
                val pixel = pixels[i]
                val alpha = Color.alpha(pixel)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                
                // 转换到HSL色彩空间
                val hsl = rgbToHsl(red, green, blue)
                val hue = hsl[0]
                val saturation = hsl[1]
                val lightness = hsl[2]
                
                // 计算当前像素的饱和度权重
                // 已经高饱和度的像素受到的影响较小
                val saturationWeight = 1.0f - saturation
                val adjustedIntensity = intensity * saturationWeight
                
                // 应用自然饱和度调整
                val newSaturation = (saturation + adjustedIntensity).coerceIn(0f, 1f)
                
                // 转换回RGB
                val newRgb = hslToRgb(hue, newSaturation, lightness)
                
                pixels[i] = Color.argb(alpha, newRgb[0], newRgb[1], newRgb[2])
            }
            
            result.setPixels(pixels, 0, width, 0, 0, width, height)
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return bitmap // 返回原图像以防崩溃
        }
    }
    
    private fun rgbToHsl(r: Int, g: Int, b: Int): FloatArray {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min
        
        val lightness = (max + min) / 2f
        
        val saturation = if (delta == 0f) {
            0f
        } else {
            val denominator = 1f - abs(2f * lightness - 1f)
            if (denominator == 0f) 0f else delta / denominator
        }
        
        val hue = when {
            delta == 0f -> 0f
            max == rf -> {
                val h = ((gf - bf) / delta) % 6f
                if (h < 0f) h + 6f else h
            }
            max == gf -> (bf - rf) / delta + 2f
            else -> (rf - gf) / delta + 4f
        } * 60f
        
        return floatArrayOf(
            if (hue.isNaN()) 0f else hue,
            if (saturation.isNaN()) 0f else saturation.coerceIn(0f, 1f),
            if (lightness.isNaN()) 0f else lightness.coerceIn(0f, 1f)
        )
    }
    
    private fun hslToRgb(h: Float, s: Float, l: Float): IntArray {
        val hNorm = if (h.isNaN() || h < 0f) 0f else h % 360f
        val sNorm = s.coerceIn(0f, 1f)
        val lNorm = l.coerceIn(0f, 1f)
        
        val c = (1f - abs(2f * lNorm - 1f)) * sNorm
        val x = c * (1f - abs((hNorm / 60f) % 2f - 1f))
        val m = lNorm - c / 2f
        
        val (r1, g1, b1) = when {
            hNorm < 60f -> Triple(c, x, 0f)
            hNorm < 120f -> Triple(x, c, 0f)
            hNorm < 180f -> Triple(0f, c, x)
            hNorm < 240f -> Triple(0f, x, c)
            hNorm < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255)
        
        return intArrayOf(r, g, b)
    }
}
