package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

/**
 * HSL (色相、饱和度、亮度) 处理器
 */
class HSLProcessor {
    
    /**
     * 应用HSL调整
     * @param bitmap 输入图像
     * @param hueShift 色相偏移 (-180 到 180 度)
     * @param saturationMultiplier 饱和度倍数 (0.0 到 2.0)
     * @param lightnessAdjustment 亮度调整 (-1.0 到 1.0)
     * @return 处理后的图像
     */
    fun process(
        bitmap: Bitmap, 
        hueShift: Float = 0f, 
        saturationMultiplier: Float = 1f, 
        lightnessAdjustment: Float = 0f
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // 转换到HSL色彩空间
            val hsl = rgbToHsl(red, green, blue)
            var hue = hsl[0]
            var saturation = hsl[1]
            var lightness = hsl[2]
            
            // 应用色相偏移
            hue = (hue + hueShift + 360f) % 360f
            
            // 应用饱和度调整
            saturation = (saturation * saturationMultiplier).coerceIn(0f, 1f)
            
            // 应用亮度调整
            lightness = (lightness + lightnessAdjustment).coerceIn(0f, 1f)
            
            // 转换回RGB
            val newRgb = hslToRgb(hue, saturation, lightness)
            
            pixels[i] = Color.argb(alpha, newRgb[0], newRgb[1], newRgb[2])
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun rgbToHsl(r: Int, g: Int, b: Int): FloatArray {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min
        
        val lightness = (max + min) / 2f
        
        val saturation = if (delta == 0f || lightness == 0f || lightness == 1f) {
            0f
        } else {
            delta / (1f - abs(2f * lightness - 1f))
        }
        
        val hue = when {
            delta == 0f -> 0f
            max == rf -> ((gf - bf) / delta) % 6f
            max == gf -> (bf - rf) / delta + 2f
            else -> (rf - gf) / delta + 4f
        } * 60f
        
        return floatArrayOf(if (hue < 0) hue + 360f else hue, saturation.coerceIn(0f, 1f), lightness.coerceIn(0f, 1f))
    }
    
    private fun hslToRgb(h: Float, s: Float, l: Float): IntArray {
        if (s == 0f) {
            val gray = (l * 255f).roundToInt().coerceIn(0, 255)
            return intArrayOf(gray, gray, gray)
        }
        
        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        
        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255)
        
        return intArrayOf(r, g, b)
    }
}
