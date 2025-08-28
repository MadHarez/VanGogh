package com.hy.vangogh.imageprocess.effects

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*
import kotlin.random.Random

/**
 * 颗粒效果处理器
 */
class GrainProcessor {
    
    /**
     * 应用颗粒效果
     * @param bitmap 输入图像
     * @param intensity 颗粒强度 (0.0 到 1.0)
     * @param grainSize 颗粒大小 (1 到 10)
     * @param grainType 颗粒类型
     * @return 处理后的图像
     */
    fun process(
        bitmap: Bitmap, 
        intensity: Float, 
        grainSize: Int = 2,
        grainType: GrainType = GrainType.FILM
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)
        
        when (grainType) {
            GrainType.FILM -> applyFilmGrain(pixels, width, height, intensity, grainSize)
            GrainType.DIGITAL -> applyDigitalGrain(pixels, width, height, intensity, grainSize)
            GrainType.VINTAGE -> applyVintageGrain(pixels, width, height, intensity, grainSize)
            GrainType.FINE -> applyFineGrain(pixels, width, height, intensity, grainSize)
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun applyFilmGrain(pixels: IntArray, width: Int, height: Int, intensity: Float, grainSize: Int) {
        val random = Random(System.currentTimeMillis())
        
        for (y in 0 until height step grainSize) {
            for (x in 0 until width step grainSize) {
                val grainValue = (random.nextFloat() * 2f - 1f) * intensity * 25f
                
                for (dy in 0 until grainSize) {
                    for (dx in 0 until grainSize) {
                        val px = x + dx
                        val py = y + dy
                        if (px < width && py < height) {
                            val index = py * width + px
                            val pixel = pixels[index]
                            val alpha = Color.alpha(pixel)
                            
                            val red = (Color.red(pixel) + grainValue).roundToInt().coerceIn(0, 255)
                            val green = (Color.green(pixel) + grainValue).roundToInt().coerceIn(0, 255)
                            val blue = (Color.blue(pixel) + grainValue).roundToInt().coerceIn(0, 255)
                            
                            pixels[index] = Color.argb(alpha, red, green, blue)
                        }
                    }
                }
            }
        }
    }
    
    private fun applyDigitalGrain(pixels: IntArray, width: Int, height: Int, intensity: Float, grainSize: Int) {
        val random = Random(42) // 固定种子
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // 数字噪声更均匀
            val noise = (random.nextFloat() - 0.5f) * 2f * intensity * 20f
            
            val newRed = (red + noise).roundToInt().coerceIn(0, 255)
            val newGreen = (green + noise).roundToInt().coerceIn(0, 255)
            val newBlue = (blue + noise).roundToInt().coerceIn(0, 255)
            
            pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
        }
    }
    
    private fun applyVintageGrain(pixels: IntArray, width: Int, height: Int, intensity: Float, grainSize: Int) {
        val random = Random(123)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = Color.alpha(pixel)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                
                // 复古颗粒，偏向暖色调
                val baseNoise = (random.nextFloat() - 0.5f) * 2f * intensity * 15f
                val redNoise = baseNoise + random.nextFloat() * intensity * 5f
                val greenNoise = baseNoise
                val blueNoise = baseNoise - random.nextFloat() * intensity * 3f
                
                val newRed = (red + redNoise).roundToInt().coerceIn(0, 255)
                val newGreen = (green + greenNoise).roundToInt().coerceIn(0, 255)
                val newBlue = (blue + blueNoise).roundToInt().coerceIn(0, 255)
                
                pixels[index] = Color.argb(alpha, newRed, newGreen, newBlue)
            }
        }
    }
    
    private fun applyFineGrain(pixels: IntArray, width: Int, height: Int, intensity: Float, grainSize: Int) {
        val random = Random(456)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // 细颗粒，强度较小但更细腻
            val noise = sin(random.nextFloat() * PI * 2).toFloat() * intensity * 8f
            
            val newRed = (red + noise).roundToInt().coerceIn(0, 255)
            val newGreen = (green + noise).roundToInt().coerceIn(0, 255)
            val newBlue = (blue + noise).roundToInt().coerceIn(0, 255)
            
            pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
        }
    }
    
    enum class GrainType {
        FILM,      // 胶片颗粒
        DIGITAL,   // 数字噪点
        VINTAGE,   // 复古颗粒
        FINE       // 细颗粒
    }
}
