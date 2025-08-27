package com.hy.vangogh.imageprocess.filters

import android.content.Context
import android.graphics.Bitmap
import com.hy.vangogh.imageprocess.core.FilterProcessor

class BlurProcessor(private val context: Context) : FilterProcessor {
    
    override fun process(bitmap: Bitmap, intensity: Float): Bitmap {
        val radius = (intensity * 10).coerceAtMost(25f)
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            applyRenderScriptBlur(bitmap, radius)
        } else {
            applyGaussianBlur(bitmap, intensity)
        }
    }
    
    private fun applyRenderScriptBlur(bitmap: Bitmap, radius: Float): Bitmap {
        return try {
            val renderScript = android.renderscript.RenderScript.create(context)
            val input = android.renderscript.Allocation.createFromBitmap(renderScript, bitmap)
            val output = android.renderscript.Allocation.createTyped(renderScript, input.type)
            val script = android.renderscript.ScriptIntrinsicBlur.create(renderScript, android.renderscript.Element.U8_4(renderScript))
            script.setRadius(radius)
            script.setInput(input)
            script.forEach(output)
            val blurredBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            output.copyTo(blurredBitmap)
            renderScript.destroy()
            blurredBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }
    
    private fun applyGaussianBlur(bitmap: Bitmap, intensity: Float): Bitmap {
        // Fallback implementation for older Android versions
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val blurRadius = (intensity * 5).toInt().coerceAtLeast(1)
        val blurredPixels = gaussianBlur(pixels, width, height, blurRadius)
        
        return Bitmap.createBitmap(blurredPixels, width, height, Bitmap.Config.ARGB_8888)
    }
    
    private fun gaussianBlur(pixels: IntArray, width: Int, height: Int, radius: Int): IntArray {
        val result = IntArray(pixels.size)
        
        // Simple box blur approximation
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0
                var g = 0
                var b = 0
                var a = 0
                var count = 0
                
                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val nx = (x + dx).coerceIn(0, width - 1)
                        val ny = (y + dy).coerceIn(0, height - 1)
                        val pixel = pixels[ny * width + nx]
                        
                        a += (pixel shr 24) and 0xFF
                        r += (pixel shr 16) and 0xFF
                        g += (pixel shr 8) and 0xFF
                        b += pixel and 0xFF
                        count++
                    }
                }
                
                result[y * width + x] = (a / count shl 24) or
                        (r / count shl 16) or
                        (g / count shl 8) or
                        (b / count)
            }
        }
        
        return result
    }
}
