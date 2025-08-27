package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import com.hy.vangogh.imageprocess.core.AdjustmentProcessor

/**
 * 饱和度调节处理器
 * 通过ColorMatrix调整图像饱和度
 */
class SaturationProcessor : AdjustmentProcessor {
    
    /**
     * 调节图像饱和度
     * @param bitmap 原始图像
     * @param saturation 饱和度值 (0.0 到 2.0，1.0为原始饱和度)
     * @return 调节后的图像
     */
    override fun process(bitmap: Bitmap, value: Float): Bitmap {
        return adjustSaturation(bitmap, value)
    }
    
    private fun adjustSaturation(bitmap: Bitmap, saturation: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // 使用ColorMatrix进行饱和度调整
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(saturation)
        
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
    fun getValueRange(): ClosedFloatingPointRange<Float> = 0f..2f
    
    /**
     * 获取默认值
     */
    fun getDefaultValue(): Float = 1f
}
