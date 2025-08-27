package com.hy.vangogh.imageprocess.adjustments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Color
import com.hy.vangogh.imageprocess.core.ColorProcessor

class ColorAdjustmentProcessor : ColorProcessor {
    
    override fun adjustBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightness * 255,
                0f, 1f, 0f, 0f, brightness * 255,
                0f, 0f, 1f, 0f, brightness * 255,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(bitmap, colorMatrix)
    }
    
    override fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, 0f,
                0f, contrast, 0f, 0f, 0f,
                0f, 0f, contrast, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(bitmap, colorMatrix)
    }
    
    override fun adjustSaturation(bitmap: Bitmap, saturation: Float): Bitmap {
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(saturation)
        return applyColorMatrix(bitmap, colorMatrix)
    }
    
    override fun adjustWarmth(bitmap: Bitmap, warmth: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val a = Color.alpha(pixel)
            
            val newR = (r + warmth * 30).coerceIn(0f, 255f).toInt()
            val newB = (b - warmth * 20).coerceIn(0f, 255f).toInt()
            
            pixels[i] = Color.argb(a, newR, g, newB)
        }
        
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
    
    private fun applyColorMatrix(bitmap: Bitmap, colorMatrix: ColorMatrix): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint()
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return resultBitmap
    }
}
