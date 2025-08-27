package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Color
import com.hy.vangogh.imageprocess.core.AdjustmentProcessor

/**
 * 色温调节处理器
 * 通过调整红蓝通道比例来改变图像色温
 */
class TemperatureProcessor : AdjustmentProcessor {
    
    /**
     * 调节图像色温
     * @param bitmap 原始图像
     * @param temperature 色温值 (-1.0 到 1.0，负值偏冷，正值偏暖)
     * @return 调节后的图像
     */
    override fun process(bitmap: Bitmap, value: Float): Bitmap {
        return adjustTemperature(bitmap, value)
    }
    
    private fun adjustTemperature(bitmap: Bitmap, temperature: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // 色温调整系数
        val warmthFactor = if (temperature > 0) temperature else 0f
        val coolnessFactor = if (temperature < 0) -temperature else 0f
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            var newRed = red.toFloat()
            var newGreen = green.toFloat()
            var newBlue = blue.toFloat()
            
            if (temperature > 0) {
                // 暖色调：增加红色，减少蓝色
                newRed = (red + warmthFactor * 30).coerceIn(0f, 255f)
                newBlue = (blue - warmthFactor * 20).coerceIn(0f, 255f)
            } else if (temperature < 0) {
                // 冷色调：增加蓝色，减少红色
                newBlue = (blue + coolnessFactor * 30).coerceIn(0f, 255f)
                newRed = (red - coolnessFactor * 20).coerceIn(0f, 255f)
            }
            
            pixels[i] = Color.argb(alpha, newRed.toInt(), newGreen.toInt(), newBlue.toInt())
        }
        
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
    
    /**
     * 获取调节参数的有效范围
     */
    fun getValueRange(): ClosedFloatingPointRange<Float> = -1f..1f
    
    /**
     * 获取默认值
     */
    fun getDefaultValue(): Float = 0f
}
