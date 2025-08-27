package com.hy.vangogh.imageprocess.core

import android.content.Context
import com.hy.vangogh.imageprocess.adjustments.ColorAdjustmentProcessor
import com.hy.vangogh.imageprocess.filters.BlurProcessor
import com.hy.vangogh.imageprocess.filters.SharpenProcessor
import com.hy.vangogh.imageprocess.filters.VignetteProcessor

class ImageProcessorFactory(private val context: Context) {
    
    fun createColorProcessor(): ColorProcessor = ColorAdjustmentProcessor()
    
    fun createBlurProcessor(): FilterProcessor = BlurProcessor(context)
    
    fun createSharpenProcessor(): FilterProcessor = SharpenProcessor()
    
    fun createVignetteProcessor(): FilterProcessor = VignetteProcessor()
}
