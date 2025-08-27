package com.hy.vangogh.imageprocess.filters

import android.graphics.*
import com.hy.vangogh.imageprocess.core.FilterProcessor
import kotlin.math.sqrt

class VignetteProcessor : FilterProcessor {
    
    override fun process(bitmap: Bitmap, intensity: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = sqrt(centerX * centerX + centerY * centerY)
        
        val paint = Paint()
        val gradient = RadialGradient(
            centerX, centerY, maxRadius,
            intArrayOf(Color.TRANSPARENT, Color.argb((intensity * 255).toInt(), 0, 0, 0)),
            floatArrayOf(0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        return resultBitmap
    }
}
