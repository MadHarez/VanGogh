package com.hy.vangogh.data.manager

import android.graphics.Bitmap
import com.hy.vangogh.data.model.EditHistory
import com.hy.vangogh.data.model.ImageFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 历史记录管理器
 * 负责管理编辑历史的撤销/重做功能
 */
class HistoryManager {
    
    // 历史记录栈
    private val historyStack = mutableListOf<EditHistory>()
    
    // 当前位置指针
    private var currentIndex = -1
    
    // 最大历史记录数量（防止内存溢出）
    private val maxHistorySize = 20
    
    // 当前历史状态
    private val _currentHistory = MutableStateFlow<EditHistory?>(null)
    val currentHistory: StateFlow<EditHistory?> = _currentHistory.asStateFlow()
    
    // 撤销/重做状态
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    
    /**
     * 添加新的历史记录
     */
    fun addHistory(history: EditHistory) {
        // 如果当前不在最新位置，删除后面的历史记录
        if (currentIndex < historyStack.size - 1) {
            // 清理后面的记录，释放内存
            for (i in currentIndex + 1 until historyStack.size) {
                historyStack[i].thumbnailBitmap?.recycle()
            }
            historyStack.subList(currentIndex + 1, historyStack.size).clear()
        }
        
        // 添加新记录
        historyStack.add(history)
        currentIndex = historyStack.size - 1
        
        // 限制历史记录数量
        if (historyStack.size > maxHistorySize) {
            val removedHistory = historyStack.removeAt(0)
            removedHistory.thumbnailBitmap?.recycle()
            currentIndex--
        }
        
        updateStates()
    }
    
    /**
     * 撤销操作
     */
    fun undo(): EditHistory? {
        if (!canUndo.value || currentIndex <= 0) return null
        
        currentIndex--
        val history = historyStack[currentIndex]
        updateStates()
        return history
    }
    
    /**
     * 重做操作
     */
    fun redo(): EditHistory? {
        if (!canRedo.value || currentIndex >= historyStack.size - 1) return null
        
        currentIndex++
        val history = historyStack[currentIndex]
        updateStates()
        return history
    }
    
    /**
     * 获取当前历史记录
     */
    fun getCurrentHistory(): EditHistory? {
        return if (currentIndex >= 0 && currentIndex < historyStack.size) {
            historyStack[currentIndex]
        } else null
    }
    
    /**
     * 清空历史记录
     */
    fun clearHistory() {
        // 释放所有bitmap内存
        historyStack.forEach { it.thumbnailBitmap?.recycle() }
        historyStack.clear()
        currentIndex = -1
        updateStates()
    }
    
    /**
     * 获取历史记录列表（用于UI显示）
     */
    fun getHistoryList(): List<EditHistory> {
        return historyStack.toList()
    }
    
    /**
     * 获取当前位置
     */
    fun getCurrentIndex(): Int = currentIndex
    
    /**
     * 更新状态
     */
    private fun updateStates() {
        _currentHistory.value = getCurrentHistory()
        _canUndo.value = currentIndex > 0
        _canRedo.value = currentIndex < historyStack.size - 1
    }
    
    /**
     * 创建缩略图
     */
    fun createThumbnail(bitmap: Bitmap, maxSize: Int = 100): Bitmap {
        val ratio = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height
        )
        
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * 获取历史记录统计信息
     */
    fun getHistoryStats(): HistoryStats {
        return HistoryStats(
            totalCount = historyStack.size,
            currentIndex = currentIndex,
            canUndo = canUndo.value,
            canRedo = canRedo.value
        )
    }
}

/**
 * 历史记录统计信息
 */
data class HistoryStats(
    val totalCount: Int,
    val currentIndex: Int,
    val canUndo: Boolean,
    val canRedo: Boolean
)
