package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Color
import com.hy.vangogh.imageprocess.core.AdjustmentProcessor
import kotlin.math.cos
import kotlin.math.sin

/**
 * 色调调节处理器
 * 通过HSV色彩空间调整图像色调
 */
class TintProcessor : AdjustmentProcessor {
    
    /**
     * 调节图像色调
     * @param bitmap 原始图像
     * @param tint 色调值 (-1.0 到 1.0，对应-180°到180°的色相偏移)
     * @return 调节后的图像
     */
    override fun process(bitmap: Bitmap, value: Float): Bitmap {
        return adjustTint(bitmap, value)
    }
    
    private fun adjustTint(bitmap: Bitmap, tint: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // 将色调值转换为角度偏移（-180°到180°）
        val hueShift = tint * 180f
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // 转换到HSV色彩空间
            val hsv = FloatArray(3)
            Color.RGBToHSV(red, green, blue, hsv)
            
            // 调整色相
            hsv[0] = (hsv[0] + hueShift) % 360f
            if (hsv[0] < 0) hsv[0] += 360f
            
            // 转换回RGB
            val newColor = Color.HSVToColor(hsv)
            pixels[i] = Color.argb(
                alpha,
                Color.red(newColor),
                Color.green(newColor),
                Color.blue(newColor)
            )
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
