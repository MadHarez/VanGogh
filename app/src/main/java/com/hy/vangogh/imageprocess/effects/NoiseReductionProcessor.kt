package com.hy.vangogh.imageprocess.effects

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

/**
 * 降噪处理器
 */
class NoiseReductionProcessor {
    
    /**
     * 应用降噪处理
     * @param bitmap 输入图像
     * @param strength 降噪强度 (0.0 到 1.0)
     * @param preserveDetails 是否保留细节
     * @return 处理后的图像
     */
    fun process(bitmap: Bitmap, strength: Float, preserveDetails: Boolean = true): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // 使用双边滤波进行降噪
        val filteredPixels = if (preserveDetails) {
            applyBilateralFilter(pixels, width, height, strength)
        } else {
            applyGaussianFilter(pixels, width, height, strength)
        }
        
        result.setPixels(filteredPixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun applyBilateralFilter(pixels: IntArray, width: Int, height: Int, strength: Float): IntArray {
        val result = IntArray(pixels.size)
        val radius = (strength * 5f + 1f).toInt()
        val sigmaColor = strength * 50f + 10f
        val sigmaSpace = strength * 10f + 5f
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val centerIndex = y * width + x
                val centerPixel = pixels[centerIndex]
                
                var sumR = 0f
                var sumG = 0f
                var sumB = 0f
                var sumWeight = 0f
                
                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val nx = (x + dx).coerceIn(0, width - 1)
                        val ny = (y + dy).coerceIn(0, height - 1)
                        val neighborIndex = ny * width + nx
                        val neighborPixel = pixels[neighborIndex]
                        
                        // 空间权重
                        val spatialDist = sqrt((dx * dx + dy * dy).toFloat())
                        val spatialWeight = exp(-(spatialDist * spatialDist) / (2 * sigmaSpace * sigmaSpace))
                        
                        // 颜色权重
                        val colorDist = colorDistance(centerPixel, neighborPixel)
                        val colorWeight = exp(-(colorDist * colorDist) / (2 * sigmaColor * sigmaColor))
                        
                        val weight = spatialWeight * colorWeight
                        
                        sumR += Color.red(neighborPixel) * weight
                        sumG += Color.green(neighborPixel) * weight
                        sumB += Color.blue(neighborPixel) * weight
                        sumWeight += weight
                    }
                }
                
                if (sumWeight > 0) {
                    val newR = (sumR / sumWeight).roundToInt().coerceIn(0, 255)
                    val newG = (sumG / sumWeight).roundToInt().coerceIn(0, 255)
                    val newB = (sumB / sumWeight).roundToInt().coerceIn(0, 255)
                    result[centerIndex] = Color.argb(Color.alpha(centerPixel), newR, newG, newB)
                } else {
                    result[centerIndex] = centerPixel
                }
            }
        }
        
        return result
    }
    
    private fun applyGaussianFilter(pixels: IntArray, width: Int, height: Int, strength: Float): IntArray {
        val result = IntArray(pixels.size)
        val radius = (strength * 3f + 1f).toInt()
        val sigma = strength * 2f + 1f
        
        // 创建高斯核
        val kernel = createGaussianKernel(radius, sigma)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sumR = 0f
                var sumG = 0f
                var sumB = 0f
                var sumWeight = 0f
                
                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val nx = (x + dx).coerceIn(0, width - 1)
                        val ny = (y + dy).coerceIn(0, height - 1)
                        val pixel = pixels[ny * width + nx]
                        val weight = kernel[dy + radius][dx + radius]
                        
                        sumR += Color.red(pixel) * weight
                        sumG += Color.green(pixel) * weight
                        sumB += Color.blue(pixel) * weight
                        sumWeight += weight
                    }
                }
                
                val centerPixel = pixels[y * width + x]
                val newR = (sumR / sumWeight).roundToInt().coerceIn(0, 255)
                val newG = (sumG / sumWeight).roundToInt().coerceIn(0, 255)
                val newB = (sumB / sumWeight).roundToInt().coerceIn(0, 255)
                
                result[y * width + x] = Color.argb(Color.alpha(centerPixel), newR, newG, newB)
            }
        }
        
        return result
    }
    
    private fun colorDistance(pixel1: Int, pixel2: Int): Float {
        val r1 = Color.red(pixel1)
        val g1 = Color.green(pixel1)
        val b1 = Color.blue(pixel1)
        val r2 = Color.red(pixel2)
        val g2 = Color.green(pixel2)
        val b2 = Color.blue(pixel2)
        
        return sqrt(((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)).toFloat())
    }
    
    private fun createGaussianKernel(radius: Int, sigma: Float): Array<FloatArray> {
        val size = radius * 2 + 1
        val kernel = Array(size) { FloatArray(size) }
        var sum = 0f
        
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - radius
                val dy = y - radius
                kernel[y][x] = exp(-(dx * dx + dy * dy) / (2 * sigma * sigma))
                sum += kernel[y][x]
            }
        }
        
        // 归一化
        for (y in 0 until size) {
            for (x in 0 until size) {
                kernel[y][x] /= sum
            }
        }
        
        return kernel
    }
}
