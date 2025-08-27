package com.hy.vangogh.data.model

import android.graphics.Bitmap

/**
 * 编辑历史记录数据模型
 * 存储每次编辑操作的状态快照
 */
data class EditHistory(
    val id: String,
    val timestamp: Long,
    val actionType: EditActionType,
    val actionDescription: String,
    val imageFilter: ImageFilter,
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
    // 存储缩略图以节省内存
    val thumbnailBitmap: Bitmap? = null
) {
    companion object {
        fun createInitialState(originalFilter: ImageFilter = ImageFilter.NONE): EditHistory {
            return EditHistory(
                id = "initial_${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis(),
                actionType = EditActionType.INITIAL,
                actionDescription = "原始图像",
                imageFilter = originalFilter
            )
        }
        
        fun createFilterState(
            filter: ImageFilter,
            brightness: Float = 0f,
            contrast: Float = 1f,
            saturation: Float = 1f,
            warmth: Float = 0f,
            exposure: Float = 0f,
            highlight: Float = 0f,
            shadow: Float = 0f,
            temperature: Float = 0f,
            tint: Float = 0f,
            thumbnail: Bitmap? = null
        ): EditHistory {
            return EditHistory(
                id = "filter_${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis(),
                actionType = EditActionType.FILTER_APPLIED,
                actionDescription = "应用滤镜: ${filter.name}",
                imageFilter = filter,
                customBrightness = brightness,
                customContrast = contrast,
                customSaturation = saturation,
                customWarmth = warmth,
                customExposure = exposure,
                customHighlight = highlight,
                customShadow = shadow,
                customTemperature = temperature,
                customTint = tint,
                thumbnailBitmap = thumbnail
            )
        }
        
        fun createAdjustmentState(
            adjustmentType: String,
            value: Float,
            currentFilter: ImageFilter,
            brightness: Float,
            contrast: Float,
            saturation: Float,
            warmth: Float,
            exposure: Float,
            highlight: Float,
            shadow: Float,
            temperature: Float,
            tint: Float,
            thumbnail: Bitmap? = null
        ): EditHistory {
            return EditHistory(
                id = "adjustment_${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis(),
                actionType = EditActionType.ADJUSTMENT_MADE,
                actionDescription = "调整$adjustmentType: $value",
                imageFilter = currentFilter,
                customBrightness = brightness,
                customContrast = contrast,
                customSaturation = saturation,
                customWarmth = warmth,
                customExposure = exposure,
                customHighlight = highlight,
                customShadow = shadow,
                customTemperature = temperature,
                customTint = tint,
                thumbnailBitmap = thumbnail
            )
        }
    }
}

/**
 * 编辑操作类型枚举
 */
enum class EditActionType {
    INITIAL,           // 初始状态
    FILTER_APPLIED,    // 应用滤镜
    ADJUSTMENT_MADE,   // 手动调整
    RESET              // 重置操作
}
