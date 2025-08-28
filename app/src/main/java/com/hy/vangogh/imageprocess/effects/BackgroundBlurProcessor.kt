package com.hy.vangogh.imageprocess.effects

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

/**
 * 背景虚化处理器
 */
class BackgroundBlurProcessor {
    
    /**
     * 应用背景虚化效果
     * @param bitmap 输入图像
     * @param blurRadius 模糊半径 (1 到 25)
     * @param focusX 焦点X坐标 (0.0 到 1.0)
     * @param focusY 焦点Y坐标 (0.0 到 1.0)
     * @param focusRadius 焦点半径 (0.1 到 1.0)
     * @return 处理后的图像
     */
    fun process(
        bitmap: Bitmap, 
        blurRadius: Int = 10,
        focusX: Float = 0.5f,
        focusY: Float = 0.5f,
        focusRadius: Float = 0.3f
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        // 创建模糊版本
        val blurredBitmap = applyGaussianBlur(bitmap, blurRadius)
        
        val originalPixels = IntArray(width * height)
        val blurredPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)
        
        bitmap.getPixels(originalPixels, 0, width, 0, 0, width, height)
        blurredBitmap.getPixels(blurredPixels, 0, width, 0, 0, width, height)
        
        val centerX = (focusX * width).toInt()
        val centerY = (focusY * height).toInt()
        val maxRadius = focusRadius * min(width, height) / 2f
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                
                // 计算到焦点的距离
                val distance = sqrt(((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)).toFloat())
                
                // 计算混合比例
                val blendRatio = if (distance <= maxRadius) {
                    0f // 焦点区域不模糊
                } else {
                    val fadeDistance = maxRadius * 0.5f
                    ((distance - maxRadius) / fadeDistance).coerceIn(0f, 1f)
                }
                
                // 混合原图和模糊图
                val originalPixel = originalPixels[index]
                val blurredPixel = blurredPixels[index]
                
                val alpha = Color.alpha(originalPixel)
                val red = lerp(Color.red(originalPixel), Color.red(blurredPixel), blendRatio)
                val green = lerp(Color.green(originalPixel), Color.green(blurredPixel), blendRatio)
                val blue = lerp(Color.blue(originalPixel), Color.blue(blurredPixel), blendRatio)
                
                resultPixels[index] = Color.argb(alpha, red, green, blue)
            }
        }
        
        result.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun applyGaussianBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // 简化的高斯模糊实现
        val kernel = createGaussianKernel(radius)
        val blurredPixels = IntArray(width * height)
        
        // 水平模糊
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0f
                var g = 0f
                var b = 0f
                var a = 0f
                
                for (i in kernel.indices) {
                    val px = (x + i - radius).coerceIn(0, width - 1)
                    val pixel = pixels[y * width + px]
                    val weight = kernel[i]
                    
                    r += Color.red(pixel) * weight
                    g += Color.green(pixel) * weight
                    b += Color.blue(pixel) * weight
                    a += Color.alpha(pixel) * weight
                }
                
                blurredPixels[y * width + x] = Color.argb(
                    a.roundToInt().coerceIn(0, 255),
                    r.roundToInt().coerceIn(0, 255),
                    g.roundToInt().coerceIn(0, 255),
                    b.roundToInt().coerceIn(0, 255)
                )
            }
        }
        
        // 垂直模糊
        for (x in 0 until width) {
            for (y in 0 until height) {
                var r = 0f
                var g = 0f
                var b = 0f
                var a = 0f
                
                for (i in kernel.indices) {
                    val py = (y + i - radius).coerceIn(0, height - 1)
                    val pixel = blurredPixels[py * width + x]
                    val weight = kernel[i]
                    
                    r += Color.red(pixel) * weight
                    g += Color.green(pixel) * weight
                    b += Color.blue(pixel) * weight
                    a += Color.alpha(pixel) * weight
                }
                
                pixels[y * width + x] = Color.argb(
                    a.roundToInt().coerceIn(0, 255),
                    r.roundToInt().coerceIn(0, 255),
                    g.roundToInt().coerceIn(0, 255),
                    b.roundToInt().coerceIn(0, 255)
                )
            }
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun createGaussianKernel(radius: Int): FloatArray {
        val size = radius * 2 + 1
        val kernel = FloatArray(size)
        val sigma = radius / 3f
        var sum = 0f
        
        for (i in 0 until size) {
            val x = i - radius
            kernel[i] = exp(-(x * x) / (2 * sigma * sigma))
            sum += kernel[i]
        }
        
        // 归一化
        for (i in kernel.indices) {
            kernel[i] /= sum
        }
        
        return kernel
    }
    
    private fun lerp(a: Int, b: Int, t: Float): Int {
        return (a + t * (b - a)).roundToInt().coerceIn(0, 255)
    }
}
