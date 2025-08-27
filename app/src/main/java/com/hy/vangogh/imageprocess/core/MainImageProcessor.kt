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
        tint: Float = 0f
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) return@withContext null
            
            applyAdjustmentsToBitmap(originalBitmap, brightness, exposure, contrast, saturation, highlight, shadow, temperature, tint)
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
        tint: Float
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
}
