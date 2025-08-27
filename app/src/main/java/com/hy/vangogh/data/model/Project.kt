package com.hy.vangogh.data.model

import android.net.Uri
import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val originalImageUri: Uri? = null,
    val processedImagePath: String? = null,
    val currentFilter: ImageFilter = ImageFilter.NONE,
    val customBrightness: Float = 0f,
    val customContrast: Float = 1f,
    val customSaturation: Float = 1f,
    val customWarmth: Float = 0f,
    // 新增的调节参数
    val customExposure: Float = 0f,
    val customHighlight: Float = 0f,
    val customShadow: Float = 0f,
    val customTemperature: Float = 0f,
    val customTint: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
) {
    fun hasImage(): Boolean = originalImageUri != null
    
    fun isModified(): Boolean = currentFilter != ImageFilter.NONE || 
            customBrightness != 0f || 
            customContrast != 1f || 
            customSaturation != 1f || 
            customWarmth != 0f ||
            customExposure != 0f ||
            customHighlight != 0f ||
            customShadow != 0f ||
            customTemperature != 0f ||
            customTint != 0f
}
