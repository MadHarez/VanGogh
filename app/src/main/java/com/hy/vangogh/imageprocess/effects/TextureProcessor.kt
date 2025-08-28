package com.hy.vangogh.imageprocess.effects

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*
import kotlin.random.Random

/**
 * 纹理效果处理器
 */
class TextureProcessor {
    
    /**
     * 应用纹理效果
     * @param bitmap 输入图像
     * @param textureType 纹理类型
     * @param intensity 强度 (0.0 到 1.0)
     * @return 处理后的图像
     */
    fun process(bitmap: Bitmap, textureType: TextureType, intensity: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)
        
        when (textureType) {
            TextureType.PAPER -> applyPaperTexture(pixels, width, height, intensity)
            TextureType.CANVAS -> applyCanvasTexture(pixels, width, height, intensity)
            TextureType.FABRIC -> applyFabricTexture(pixels, width, height, intensity)
            TextureType.METAL -> applyMetalTexture(pixels, width, height, intensity)
            TextureType.WOOD -> applyWoodTexture(pixels, width, height, intensity)
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun applyPaperTexture(pixels: IntArray, width: Int, height: Int, intensity: Float) {
        val random = Random(42) // 固定种子确保一致性
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // 生成纸张纹理噪声
            val noise = (random.nextFloat() - 0.5f) * 2f * intensity * 30f
            
            val newRed = (red + noise).roundToInt().coerceIn(0, 255)
            val newGreen = (green + noise).roundToInt().coerceIn(0, 255)
            val newBlue = (blue + noise).roundToInt().coerceIn(0, 255)
            
            pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
        }
    }
    
    private fun applyCanvasTexture(pixels: IntArray, width: Int, height: Int, intensity: Float) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = Color.alpha(pixel)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                
                // 创建画布纹理模式
                val patternX = sin(x * 0.1f) * cos(y * 0.08f)
                val patternY = cos(x * 0.08f) * sin(y * 0.1f)
                val textureValue = (patternX + patternY) * intensity * 15f
                
                val newRed = (red + textureValue).roundToInt().coerceIn(0, 255)
                val newGreen = (green + textureValue).roundToInt().coerceIn(0, 255)
                val newBlue = (blue + textureValue).roundToInt().coerceIn(0, 255)
                
                pixels[index] = Color.argb(alpha, newRed, newGreen, newBlue)
            }
        }
    }
    
    private fun applyFabricTexture(pixels: IntArray, width: Int, height: Int, intensity: Float) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = Color.alpha(pixel)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                
                // 创建织物纹理
                val warpPattern = sin(x * 0.2f) * 0.5f + 0.5f
                val weftPattern = sin(y * 0.2f) * 0.5f + 0.5f
                val fabricTexture = (warpPattern * weftPattern - 0.25f) * intensity * 20f
                
                val newRed = (red + fabricTexture).roundToInt().coerceIn(0, 255)
                val newGreen = (green + fabricTexture).roundToInt().coerceIn(0, 255)
                val newBlue = (blue + fabricTexture).roundToInt().coerceIn(0, 255)
                
                pixels[index] = Color.argb(alpha, newRed, newGreen, newBlue)
            }
        }
    }
    
    private fun applyMetalTexture(pixels: IntArray, width: Int, height: Int, intensity: Float) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = Color.alpha(pixel)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                
                // 创建金属拉丝纹理
                val brushPattern = sin(x * 0.05f + y * 0.02f) * intensity * 25f
                val reflection = cos(x * 0.03f) * intensity * 10f
                
                val newRed = (red + brushPattern + reflection).roundToInt().coerceIn(0, 255)
                val newGreen = (green + brushPattern + reflection).roundToInt().coerceIn(0, 255)
                val newBlue = (blue + brushPattern + reflection).roundToInt().coerceIn(0, 255)
                
                pixels[index] = Color.argb(alpha, newRed, newGreen, newBlue)
            }
        }
    }
    
    private fun applyWoodTexture(pixels: IntArray, width: Int, height: Int, intensity: Float) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val pixel = pixels[index]
                val alpha = Color.alpha(pixel)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                
                // 创建木纹纹理
                val grainPattern = sin(y * 0.02f + sin(x * 0.01f) * 5f) * intensity * 20f
                val ringPattern = sin(sqrt((x * x + y * y).toFloat()) * 0.01f) * intensity * 10f
                
                val woodTexture = grainPattern + ringPattern
                
                val newRed = (red + woodTexture).roundToInt().coerceIn(0, 255)
                val newGreen = (green + woodTexture * 0.8f).roundToInt().coerceIn(0, 255)
                val newBlue = (blue + woodTexture * 0.6f).roundToInt().coerceIn(0, 255)
                
                pixels[index] = Color.argb(alpha, newRed, newGreen, newBlue)
            }
        }
    }
    
    enum class TextureType {
        PAPER,
        CANVAS,
        FABRIC,
        METAL,
        WOOD
    }
}
