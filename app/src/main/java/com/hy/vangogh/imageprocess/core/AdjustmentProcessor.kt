package com.hy.vangogh.imageprocess.core

import android.graphics.Bitmap

/**
 * 图像调节处理器接口
 * 定义所有调节处理器的通用接口
 */
interface AdjustmentProcessor {
    
    /**
     * 处理图像调节
     * @param bitmap 原始图像
     * @param value 调节值
     * @return 处理后的图像
     */
    fun process(bitmap: Bitmap, value: Float): Bitmap
}
