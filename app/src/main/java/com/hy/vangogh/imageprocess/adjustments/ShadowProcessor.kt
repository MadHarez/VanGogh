package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Color
import com.hy.vangogh.imageprocess.core.AdjustmentProcessor

/**
 * 阴影调节处理器
 * 选择性调整图像中的阴影区域
 */
class ShadowProcessor : AdjustmentProcessor {
    
    /**
     * 调节图像阴影
     * @param bitmap 原始图像
     * @param shadow 阴影值 (-1.0 到 1.0，0为原始阴影)
     * @return 调节后的图像
     */
    override fun process(bitmap: Bitmap, value: Float): Bitmap {
        return adjustShadow(bitmap, value)
    }
    
    private fun adjustShadow(bitmap: Bitmap, shadow: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // 阴影阈值，亮度小于此值的像素被认为是阴影区域
        val shadowThreshold = 75
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // 计算像素亮度
            val luminance = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
            
            // 只对阴影区域进行调整
            if (luminance < shadowThreshold) {
                // 计算调整强度，亮度越低调整越强
                val intensity = ((shadowThreshold - luminance) / shadowThreshold.toFloat()) * shadow
                
                val newRed = (red + red * intensity).coerceIn(0f, 255f).toInt()
                val newGreen = (green + green * intensity).coerceIn(0f, 255f).toInt()
                val newBlue = (blue + blue * intensity).coerceIn(0f, 255f).toInt()
                
                pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
            }
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
