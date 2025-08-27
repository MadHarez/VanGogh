package com.hy.vangogh.imageprocess.core

import android.graphics.Bitmap
import android.net.Uri
import com.hy.vangogh.data.model.ImageFilter

interface ImageProcessorInterface {
    suspend fun applyFilter(imageUri: Uri, filter: ImageFilter): Bitmap?
    suspend fun saveBitmap(bitmap: Bitmap, filename: String): java.io.File?
}

interface FilterProcessor {
    fun process(bitmap: Bitmap, intensity: Float): Bitmap
}

interface ColorProcessor {
    fun adjustBrightness(bitmap: Bitmap, brightness: Float): Bitmap
    fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap
    fun adjustSaturation(bitmap: Bitmap, saturation: Float): Bitmap
    fun adjustWarmth(bitmap: Bitmap, warmth: Float): Bitmap
}

interface EffectProcessor {
    fun applyBlur(bitmap: Bitmap, intensity: Float): Bitmap
    fun applySharpen(bitmap: Bitmap, intensity: Float): Bitmap
    fun applyVignette(bitmap: Bitmap, intensity: Float): Bitmap
}
