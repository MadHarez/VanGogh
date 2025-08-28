package com.hy.vangogh.imageprocess.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.hy.vangogh.data.model.ImageFilter
import com.hy.vangogh.imageprocess.adjustments.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainImageProcessor(private val context: Context) : ImageProcessorInterface {
    
    private val factory = ImageProcessorFactory(context)
    private val colorProcessor = factory.createColorProcessor()
    private val blurProcessor = factory.createBlurProcessor()
    private val sharpenProcessor = factory.createSharpenProcessor()
    private val vignetteProcessor = factory.createVignetteProcessor()
    
    // 新增的独立调节处理器
    private val brightnessProcessor = BrightnessProcessor()
    private val exposureProcessor = ExposureProcessor()
    private val saturationProcessor = SaturationProcessor()
    private val highlightProcessor = HighlightProcessor()
    private val shadowProcessor = ShadowProcessor()
    private val temperatureProcessor = TemperatureProcessor()
    private val tintProcessor = TintProcessor()
    private val naturalSaturationProcessor = NaturalSaturationProcessor()
    
    // 高级调节处理器
    private val hslProcessor = HSLProcessor()
    private val curveProcessor = CurveProcessor()
    private val fadeProcessor = FadeProcessor()
    
    override suspend fun applyFilter(imageUri: Uri, filter: ImageFilter): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) return@withContext null
            
            applyFilterToBitmap(originalBitmap, filter)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 应用独立的调节功能到图像
     */
    suspend fun applyAdjustments(
        imageUri: Uri,
        brightness: Float = 0f,
        exposure: Float = 0f,
        contrast: Float = 1f,
        saturation: Float = 1f,
        highlight: Float = 0f,
        shadow: Float = 0f,
        temperature: Float = 0f,
        tint: Float = 0f,
        naturalSaturation: Float = 1f
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) return@withContext null
            
            applyAdjustmentsToBitmap(originalBitmap, brightness, exposure, contrast, saturation, highlight, shadow, temperature, tint, naturalSaturation)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun applyFilterToBitmap(bitmap: Bitmap, filter: ImageFilter): Bitmap {
        var processedBitmap = bitmap
        
        // Apply color adjustments
        if (filter.brightness != 0f) {
            processedBitmap = colorProcessor.adjustBrightness(processedBitmap, filter.brightness)
        }
        
        if (filter.contrast != 1f) {
            processedBitmap = colorProcessor.adjustContrast(processedBitmap, filter.contrast)
        }
        
        if (filter.saturation != 1f) {
            processedBitmap = colorProcessor.adjustSaturation(processedBitmap, filter.saturation)
        }
        
        if (filter.warmth != 0f) {
            processedBitmap = colorProcessor.adjustWarmth(processedBitmap, filter.warmth)
        }
        
        // Apply effects
        if (filter.blur > 0f) {
            processedBitmap = blurProcessor.process(processedBitmap, filter.blur)
        }
        
        if (filter.sharpen > 0f) {
            processedBitmap = sharpenProcessor.process(processedBitmap, filter.sharpen)
        }
        
        if (filter.vignette > 0f) {
            processedBitmap = vignetteProcessor.process(processedBitmap, filter.vignette)
        }
        
        return processedBitmap
    }
    
    /**
     * 应用独立调节功能到Bitmap
     */
    private fun applyAdjustmentsToBitmap(
        bitmap: Bitmap,
        brightness: Float,
        exposure: Float,
        contrast: Float,
        saturation: Float,
        highlight: Float,
        shadow: Float,
        temperature: Float,
        tint: Float,
        naturalSaturation: Float
    ): Bitmap {
        var processedBitmap = bitmap
        
        // 按照处理顺序应用调节
        // 1. 曝光调节（影响整体亮度）
        if (exposure != 0f) {
            processedBitmap = exposureProcessor.process(processedBitmap, exposure)
        }
        
        // 2. 亮度调节
        if (brightness != 0f) {
            processedBitmap = brightnessProcessor.process(processedBitmap, brightness)
        }
        
        // 3. 高光和阴影调节
        if (highlight != 0f) {
            processedBitmap = highlightProcessor.process(processedBitmap, highlight)
        }
        
        if (shadow != 0f) {
            processedBitmap = shadowProcessor.process(processedBitmap, shadow)
        }
        
        // 4. 对比度调节
        if (contrast != 1f) {
            processedBitmap = colorProcessor.adjustContrast(processedBitmap, contrast)
        }
        
        // 5. 饱和度调节
        if (saturation != 1f) {
            processedBitmap = saturationProcessor.process(processedBitmap, saturation)
        }
        
        // 6. 色温调节
        if (temperature != 0f) {
            processedBitmap = temperatureProcessor.process(processedBitmap, temperature)
        }
        
        // 7. 色调调节
        if (tint != 0f) {
            processedBitmap = tintProcessor.process(processedBitmap, tint)
        }
        
        // 8. 自然饱和度调节
        if (naturalSaturation != 1f) {
            val intensity = naturalSaturation - 1f
            processedBitmap = naturalSaturationProcessor.process(processedBitmap, intensity)
        }
        
        return processedBitmap
    }
    
    override suspend fun saveBitmap(bitmap: Bitmap, filename: String): File? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.getExternalFilesDir(null), "$filename.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // 裁切功能
    fun cropImage(bitmap: Bitmap?, ratio: Float?): Bitmap? {
        if (bitmap == null) return null
        if (ratio == null) return bitmap
        
        val width = bitmap.width
        val height = bitmap.height
        
        val newWidth: Int
        val newHeight: Int
        val x: Int
        val y: Int
        
        if (width.toFloat() / height > ratio) {
            // 图片太宽，需要裁切宽度
            newHeight = height
            newWidth = (height * ratio).toInt()
            x = (width - newWidth) / 2
            y = 0
        } else {
            // 图片太高，需要裁切高度
            newWidth = width
            newHeight = (width / ratio).toInt()
            x = 0
            y = (height - newHeight) / 2
        }
        
        return Bitmap.createBitmap(bitmap, x, y, newWidth, newHeight)
    }
    
    // 模糊效果
    fun applyBlur(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        return blurProcessor.process(bitmap, 15f)
    }
    
    // 锐化效果
    fun applySharpen(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        return sharpenProcessor.process(bitmap, 1.5f)
    }
    
    // 浮雕效果
    fun applyEmboss(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                val pixel = pixels[index]
                val nextPixel = pixels[index + 1]
                
                val r = ((pixel shr 16) and 0xFF) - ((nextPixel shr 16) and 0xFF) + 128
                val g = ((pixel shr 8) and 0xFF) - ((nextPixel shr 8) and 0xFF) + 128
                val b = (pixel and 0xFF) - (nextPixel and 0xFF) + 128
                
                val newR = r.coerceIn(0, 255)
                val newG = g.coerceIn(0, 255)
                val newB = b.coerceIn(0, 255)
                
                pixels[index] = (0xFF shl 24) or (newR shl 16) or (newG shl 8) or newB
            }
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    // 边缘检测
    fun applyEdgeDetection(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val sobelX = arrayOf(
            intArrayOf(-1, 0, 1),
            intArrayOf(-2, 0, 2),
            intArrayOf(-1, 0, 1)
        )
        
        val sobelY = arrayOf(
            intArrayOf(-1, -2, -1),
            intArrayOf(0, 0, 0),
            intArrayOf(1, 2, 1)
        )
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var gx = 0
                var gy = 0
                
                for (i in -1..1) {
                    for (j in -1..1) {
                        val pixel = pixels[(y + i) * width + (x + j)]
                        val gray = ((pixel shr 16) and 0xFF) * 0.299 + 
                                  ((pixel shr 8) and 0xFF) * 0.587 + 
                                  (pixel and 0xFF) * 0.114
                        
                        gx += (gray * sobelX[i + 1][j + 1]).toInt()
                        gy += (gray * sobelY[i + 1][j + 1]).toInt()
                    }
                }
                
                val magnitude = kotlin.math.sqrt((gx * gx + gy * gy).toDouble()).toInt()
                val edgeValue = magnitude.coerceIn(0, 255)
                
                pixels[y * width + x] = (0xFF shl 24) or (edgeValue shl 16) or (edgeValue shl 8) or edgeValue
            }
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    // 噪点效果
    fun applyNoise(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        
        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val random = kotlin.random.Random
        
        for (i in pixels.indices) {
            if (random.nextFloat() < 0.1f) { // 10% 的像素添加噪点
                val noise = random.nextInt(-50, 51)
                val pixel = pixels[i]
                
                val r = (((pixel shr 16) and 0xFF) + noise).coerceIn(0, 255)
                val g = (((pixel shr 8) and 0xFF) + noise).coerceIn(0, 255)
                val b = ((pixel and 0xFF) + noise).coerceIn(0, 255)
                
                pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    // 晕影效果
    fun applyVignette(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        return vignetteProcessor.process(bitmap, 0.8f)
    }
    
    // HSL调节
    fun applyHSL(bitmap: Bitmap?, hue: Float, saturation: Float, lightness: Float): Bitmap? {
        if (bitmap == null) return null
        return hslProcessor.process(bitmap, hue, saturation, lightness)
    }
    
    // 曲线调节
    fun applyCurves(bitmap: Bitmap?, curveType: String): Bitmap? {
        if (bitmap == null) return null
        val preset = when (curveType) {
            "Increased Contrast" -> CurveProcessor.CurvePreset.CONTRAST_BOOST
            "Soft Contrast" -> CurveProcessor.CurvePreset.SOFT_CONTRAST
            "Film Style" -> CurveProcessor.CurvePreset.FILM_LOOK
            "Vintage" -> CurveProcessor.CurvePreset.VINTAGE
            else -> CurveProcessor.CurvePreset.CONTRAST_BOOST
        }
        return curveProcessor.applyPresetCurve(bitmap, preset)
    }
    
    // 褪色效果
    fun applyFade(bitmap: Bitmap?, intensity: Float): Bitmap? {
        if (bitmap == null) return null
        return fadeProcessor.process(bitmap, intensity)
    }
}
