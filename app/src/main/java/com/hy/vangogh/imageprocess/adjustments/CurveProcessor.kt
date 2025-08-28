package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

/**
 * 曲线调色处理器
 * 支持RGB曲线和单独的红、绿、蓝通道曲线调整
 */
class CurveProcessor {
    
    /**
     * 应用曲线调整
     * @param bitmap 输入图像
     * @param rgbCurve RGB总曲线调整点 (输入值 -> 输出值)
     * @param redCurve 红色通道曲线调整点
     * @param greenCurve 绿色通道曲线调整点
     * @param blueCurve 蓝色通道曲线调整点
     * @return 处理后的图像
     */
    fun process(
        bitmap: Bitmap,
        rgbCurve: Map<Int, Int> = emptyMap(),
        redCurve: Map<Int, Int> = emptyMap(),
        greenCurve: Map<Int, Int> = emptyMap(),
        blueCurve: Map<Int, Int> = emptyMap()
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        // 构建查找表
        val rgbLut = buildLookupTable(rgbCurve)
        val redLut = buildLookupTable(redCurve)
        val greenLut = buildLookupTable(greenCurve)
        val blueLut = buildLookupTable(blueCurve)
        
        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            var red = Color.red(pixel)
            var green = Color.green(pixel)
            var blue = Color.blue(pixel)
            
            // 应用RGB总曲线
            red = rgbLut[red]
            green = rgbLut[green]
            blue = rgbLut[blue]
            
            // 应用单独通道曲线
            red = redLut[red]
            green = greenLut[green]
            blue = blueLut[blue]
            
            pixels[i] = Color.argb(alpha, red, green, blue)
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    /**
     * 应用预设曲线效果
     */
    fun applyPresetCurve(bitmap: Bitmap, preset: CurvePreset): Bitmap {
        return when (preset) {
            CurvePreset.CONTRAST_BOOST -> {
                val curve = mapOf(
                    0 to 0,
                    64 to 48,
                    128 to 128,
                    192 to 208,
                    255 to 255
                )
                process(bitmap, rgbCurve = curve)
            }
            CurvePreset.SOFT_CONTRAST -> {
                val curve = mapOf(
                    0 to 16,
                    64 to 72,
                    128 to 128,
                    192 to 184,
                    255 to 240
                )
                process(bitmap, rgbCurve = curve)
            }
            CurvePreset.FILM_LOOK -> {
                val redCurve = mapOf(0 to 8, 128 to 136, 255 to 248)
                val greenCurve = mapOf(0 to 4, 128 to 128, 255 to 252)
                val blueCurve = mapOf(0 to 16, 128 to 120, 255 to 240)
                process(bitmap, redCurve = redCurve, greenCurve = greenCurve, blueCurve = blueCurve)
            }
            CurvePreset.VINTAGE -> {
                val rgbCurve = mapOf(0 to 32, 64 to 80, 128 to 144, 192 to 200, 255 to 224)
                process(bitmap, rgbCurve = rgbCurve)
            }
        }
    }
    
    private fun buildLookupTable(curvePoints: Map<Int, Int>): IntArray {
        val lut = IntArray(256) { it }
        
        if (curvePoints.isEmpty()) return lut
        
        val sortedPoints = curvePoints.toSortedMap()
        val keys = sortedPoints.keys.toList()
        val values = sortedPoints.values.toList()
        
        for (i in 0..255) {
            when {
                i <= keys.first() -> lut[i] = values.first()
                i >= keys.last() -> lut[i] = values.last()
                else -> {
                    // 线性插值
                    var j = 0
                    while (j < keys.size - 1 && keys[j + 1] < i) j++
                    
                    val x1 = keys[j]
                    val y1 = values[j]
                    val x2 = keys[j + 1]
                    val y2 = values[j + 1]
                    
                    val t = (i - x1).toFloat() / (x2 - x1)
                    lut[i] = (y1 + t * (y2 - y1)).roundToInt().coerceIn(0, 255)
                }
            }
        }
        
        return lut
    }
    
    enum class CurvePreset {
        CONTRAST_BOOST,
        SOFT_CONTRAST,
        FILM_LOOK,
        VINTAGE
    }
}
