package com.hy.vangogh.imageprocess.filters

import android.graphics.Bitmap
import android.graphics.Color
import com.hy.vangogh.imageprocess.core.FilterProcessor

class SharpenProcessor : FilterProcessor {
    
    override fun process(bitmap: Bitmap, intensity: Float): Bitmap {
        val sharpenKernel = floatArrayOf(
            0f, -intensity, 0f,
            -intensity, 1f + 4f * intensity, -intensity,
            0f, -intensity, 0f
        )
        
        return applyConvolution(bitmap, sharpenKernel, 3)
    }
    
    private fun applyConvolution(bitmap: Bitmap, kernel: FloatArray, kernelSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val offset = kernelSize / 2
        
        for (y in offset until height - offset) {
            for (x in offset until width - offset) {
                var r = 0f
                var g = 0f
                var b = 0f
                
                for (ky in 0 until kernelSize) {
                    for (kx in 0 until kernelSize) {
                        val pixelIndex = (y + ky - offset) * width + (x + kx - offset)
                        val pixel = pixels[pixelIndex]
                        val kernelValue = kernel[ky * kernelSize + kx]
                        
                        r += Color.red(pixel) * kernelValue
                        g += Color.green(pixel) * kernelValue
                        b += Color.blue(pixel) * kernelValue
                    }
                }
                
                val resultIndex = y * width + x
                val originalPixel = pixels[resultIndex]
                resultPixels[resultIndex] = Color.argb(
                    Color.alpha(originalPixel),
                    r.coerceIn(0f, 255f).toInt(),
                    g.coerceIn(0f, 255f).toInt(),
                    b.coerceIn(0f, 255f).toInt()
                )
            }
        }
        
        return Bitmap.createBitmap(resultPixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
