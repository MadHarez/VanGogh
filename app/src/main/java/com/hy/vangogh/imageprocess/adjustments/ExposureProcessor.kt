package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Color
import com.hy.vangogh.imageprocess.core.AdjustmentProcessor
import kotlin.math.pow

/**
 * 曝光调节处理器
 * 通过伽马校正调整图像曝光度
 */
class ExposureProcessor : AdjustmentProcessor {
    
    /**
     * 调节图像曝光度
     * @param bitmap 原始图像
     * @param exposure 曝光值 (-2.0 到 2.0，0为原始曝光)
     * @return 调节后的图像
     */
    override fun process(bitmap: Bitmap, value: Float): Bitmap {
        return adjustExposure(bitmap, value)
    }
    
    private fun adjustExposure(bitmap: Bitmap, exposure: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // 将曝光值转换为乘数因子
        val exposureFactor = 2.0.pow(exposure.toDouble()).toFloat()
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // 应用曝光调整
            val newRed = (red * exposureFactor).coerceIn(0f, 255f).toInt()
            val newGreen = (green * exposureFactor).coerceIn(0f, 255f).toInt()
            val newBlue = (blue * exposureFactor).coerceIn(0f, 255f).toInt()
            
            pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
        }
        
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
    
    /**
     * 获取调节参数的有效范围
     */
    fun getValueRange(): ClosedFloatingPointRange<Float> = -2f..2f
    
    /**
     * 获取默认值
     */
    fun getDefaultValue(): Float = 0f
}
