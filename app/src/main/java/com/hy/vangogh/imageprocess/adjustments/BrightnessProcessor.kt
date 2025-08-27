package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import com.hy.vangogh.imageprocess.core.AdjustmentProcessor

/**
 * 亮度调节处理器
 * 通过ColorMatrix调整图像亮度
 */
class BrightnessProcessor : AdjustmentProcessor {
    
    /**
     * 调节图像亮度
     * @param bitmap 原始图像
     * @param brightness 亮度值 (-1.0 到 1.0，0为原始亮度)
     * @return 调节后的图像
     */
    override fun process(bitmap: Bitmap, value: Float): Bitmap {
        return adjustBrightness(bitmap, value)
    }
    
    private fun adjustBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // 使用ColorMatrix进行亮度调整
        val brightnessValue = brightness * 255f
        val colorMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightnessValue,
            0f, 1f, 0f, 0f, brightnessValue,
            0f, 0f, 1f, 0f, brightnessValue,
            0f, 0f, 0f, 1f, 0f
        ))
        
        val canvas = android.graphics.Canvas(result)
        val paint = android.graphics.Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
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
