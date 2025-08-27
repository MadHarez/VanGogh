package com.hy.vangogh.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hy.vangogh.data.model.ImageFilter
import com.hy.vangogh.data.model.Project
import com.hy.vangogh.data.model.EditHistory
import com.hy.vangogh.data.repository.ProjectRepository
import com.hy.vangogh.data.manager.HistoryManager
import com.hy.vangogh.imageprocess.core.MainImageProcessor
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class ImageEditViewModel : ViewModel() {
    
    var currentProject by mutableStateOf<Project?>(null)
        private set
    
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set
    
    var originalBitmap by mutableStateOf<Bitmap?>(null)
        private set
    
    var processedBitmap by mutableStateOf<Bitmap?>(null)
        private set
    
    var currentFilter by mutableStateOf(ImageFilter.NONE)
        private set
    
    var isProcessing by mutableStateOf(false)
        private set
    
    var customBrightness by mutableStateOf(0f)
        private set
    
    var customContrast by mutableStateOf(1f)
        private set
    
    var customSaturation by mutableStateOf(1f)
        private set
    
    var customWarmth by mutableStateOf(0f)
        private set
    
    // 新增的调节参数
    var customExposure by mutableStateOf(0f)
        private set
    
    var customHighlight by mutableStateOf(0f)
        private set
    
    var customShadow by mutableStateOf(0f)
        private set
    
    var customTemperature by mutableStateOf(0f)
        private set
    
    var customTint by mutableStateOf(0f)
        private set
    
    // 文本覆盖相关
    var textOverlays by mutableStateOf(listOf<TextOverlay>())
        private set
    
    // 裁切相关状态
    var cropRatio by mutableStateOf<Float?>(null)
        private set
    
    // 特效相关状态
    var currentEffect by mutableStateOf<String?>(null)
        private set
    
    private lateinit var imageProcessor: MainImageProcessor
    private lateinit var projectRepository: ProjectRepository
    private val historyManager = HistoryManager()
    
    // 历史记录相关状态
    val canUndo: StateFlow<Boolean> = historyManager.canUndo
    val canRedo: StateFlow<Boolean> = historyManager.canRedo
    val currentHistory: StateFlow<EditHistory?> = historyManager.currentHistory
    
    fun initializeProcessor(context: Context) {
        imageProcessor = MainImageProcessor(context)
        projectRepository = ProjectRepository(context)
    }
    
    fun loadProject(project: Project) {
        currentProject = project
        selectedImageUri = project.originalImageUri
        currentFilter = project.currentFilter
        customBrightness = project.customBrightness
        customContrast = project.customContrast
        customSaturation = project.customSaturation
        customWarmth = project.customWarmth
        customExposure = project.customExposure
        customHighlight = project.customHighlight
        customShadow = project.customShadow
        customTemperature = project.customTemperature
        customTint = project.customTint
        
        // 清空历史记录并添加初始状态
        historyManager.clearHistory()
        val initialHistory = EditHistory.createInitialState(project.currentFilter)
        historyManager.addHistory(initialHistory)
        
        // Load and process the image if available
        project.originalImageUri?.let { uri ->
            applyCustomAdjustments()
        }
    }
    
    fun selectImage(uri: Uri) {
        selectedImageUri = uri
        originalBitmap = null
        processedBitmap = null
        resetCustomValues()
        
        // 重新初始化历史记录
        historyManager.clearHistory()
        val initialHistory = EditHistory.createInitialState()
        historyManager.addHistory(initialHistory)
    }
    
    fun applyFilter(filter: ImageFilter) {
        currentFilter = filter
        selectedImageUri?.let { uri ->
            processImage(uri, filter)
        }
        
        // 添加到历史记录
        addFilterToHistory(filter)
        saveProjectChanges()
    }
    
    fun updateCustomBrightness(value: Float) {
        customBrightness = value
        applyCustomFilter()
        addAdjustmentToHistory("亮度", value)
        saveProjectChanges()
    }
    
    fun updateCustomContrast(value: Float) {
        customContrast = value
        applyCustomFilter()
        addAdjustmentToHistory("对比度", value)
        saveProjectChanges()
    }
    
    fun updateCustomSaturation(value: Float) {
        customSaturation = value
        applyCustomFilter()
        addAdjustmentToHistory("饱和度", value)
        saveProjectChanges()
    }
    
    fun updateCustomWarmth(value: Float) {
        customWarmth = value
        applyCustomFilter()
        addAdjustmentToHistory("色温", value)
        saveProjectChanges()
    }
    
    // 新增的调节功能
    fun updateCustomExposure(value: Float) {
        customExposure = value
        applyCustomFilter()
        addAdjustmentToHistory("曝光", value)
        saveProjectChanges()
    }
    
    fun updateCustomHighlight(value: Float) {
        customHighlight = value
        applyCustomFilter()
        addAdjustmentToHistory("高光", value)
        saveProjectChanges()
    }
    
    fun updateCustomShadow(value: Float) {
        customShadow = value
        applyCustomFilter()
        addAdjustmentToHistory("阴影", value)
        saveProjectChanges()
    }
    
    fun updateCustomTemperature(value: Float) {
        customTemperature = value
        applyCustomFilter()
        addAdjustmentToHistory("色温", value)
        saveProjectChanges()
    }
    
    fun updateCustomTint(value: Float) {
        customTint = value
        applyCustomFilter()
        addAdjustmentToHistory("色调", value)
        saveProjectChanges()
    }
    
    private fun getEffectiveFilter(): ImageFilter {
        return if (currentFilter != ImageFilter.NONE) {
            currentFilter
        } else {
            ImageFilter(
                name = "自定义",
                brightness = customBrightness,
                contrast = customContrast,
                saturation = customSaturation,
                warmth = customWarmth
            )
        }
    }
    
    /**
     * 应用自定义调节（使用新的独立处理器）
     */
    private fun applyCustomAdjustments() {
        selectedImageUri?.let { uri ->
            viewModelScope.launch {
                isProcessing = true
                try {
                    val bitmap = imageProcessor.applyAdjustments(
                        imageUri = uri,
                        brightness = customBrightness,
                        exposure = customExposure,
                        contrast = customContrast,
                        saturation = customSaturation,
                        highlight = customHighlight,
                        shadow = customShadow,
                        temperature = customTemperature,
                        tint = customTint
                    )
                    processedBitmap = bitmap
                    if (originalBitmap == null) {
                        originalBitmap = bitmap
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessing = false
                }
            }
        }
    }
    
    private fun applyCustomFilter() {
        // 使用新的独立调节功能
        applyCustomAdjustments()
    }
    
    private fun saveProjectChanges() {
        currentProject?.let { project ->
            val updatedProject = project.copy(
                currentFilter = currentFilter,
                customBrightness = customBrightness,
                customContrast = customContrast,
                customSaturation = customSaturation,
                customWarmth = customWarmth,
                customExposure = customExposure,
                customHighlight = customHighlight,
                customShadow = customShadow,
                customTemperature = customTemperature,
                customTint = customTint,
                lastModified = System.currentTimeMillis()
            )
            if (::projectRepository.isInitialized) {
                projectRepository.updateProject(updatedProject)
                currentProject = updatedProject
            }
        }
    }
    
    private fun processImage(uri: Uri, filter: ImageFilter) {
        viewModelScope.launch {
            isProcessing = true
            try {
                val bitmap = imageProcessor.applyFilter(uri, filter)
                processedBitmap = bitmap
                if (originalBitmap == null && filter == ImageFilter.NONE) {
                    originalBitmap = bitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isProcessing = false
            }
        }
    }
    
    fun saveImage(filename: String, onResult: (File?) -> Unit) {
        processedBitmap?.let { bitmap ->
            viewModelScope.launch {
                val file = imageProcessor.saveBitmap(bitmap, filename)
                onResult(file)
            }
        }
    }
    
    private fun resetCustomValues() {
        customBrightness = 0f
        customContrast = 1f
        customSaturation = 1f
        customWarmth = 0f
        customExposure = 0f
        customHighlight = 0f
        customShadow = 0f
        customTemperature = 0f
        customTint = 0f
        currentFilter = ImageFilter.NONE
    }
    
    fun resetToOriginal() {
        resetCustomValues()
        selectedImageUri?.let { uri ->
            processImage(uri, ImageFilter.NONE)
        }
        saveProjectChanges()
    }
    
    // 撤销/重做功能
    fun undo() {
        val previousHistory = historyManager.undo()
        previousHistory?.let { history ->
            restoreFromHistory(history)
        }
    }
    
    fun redo() {
        val nextHistory = historyManager.redo()
        nextHistory?.let { history ->
            restoreFromHistory(history)
        }
    }
    
    // 从历史记录恢复状态
    private fun restoreFromHistory(history: EditHistory) {
        currentFilter = history.imageFilter
        customBrightness = history.customBrightness
        customContrast = history.customContrast
        customSaturation = history.customSaturation
        customWarmth = history.customWarmth
        customExposure = history.customExposure
        customHighlight = history.customHighlight
        customShadow = history.customShadow
        customTemperature = history.customTemperature
        customTint = history.customTint
        
        // 重新处理图像
        selectedImageUri?.let { uri ->
            applyCustomAdjustments()
        }
        
        saveProjectChanges()
    }
    
    // 添加滤镜到历史记录
    private fun addFilterToHistory(filter: ImageFilter) {
        val thumbnail = processedBitmap?.let { historyManager.createThumbnail(it) }
        val history = EditHistory.createFilterState(
            filter = filter,
            brightness = customBrightness,
            contrast = customContrast,
            saturation = customSaturation,
            warmth = customWarmth,
            exposure = customExposure,
            highlight = customHighlight,
            shadow = customShadow,
            temperature = customTemperature,
            tint = customTint,
            thumbnail = thumbnail
        )
        historyManager.addHistory(history)
    }
    
    // 添加调整到历史记录
    private fun addAdjustmentToHistory(adjustmentType: String, value: Float) {
        val thumbnail = processedBitmap?.let { historyManager.createThumbnail(it) }
        val history = EditHistory.createAdjustmentState(
            adjustmentType = adjustmentType,
            value = value,
            currentFilter = currentFilter,
            brightness = customBrightness,
            contrast = customContrast,
            saturation = customSaturation,
            warmth = customWarmth,
            exposure = customExposure,
            highlight = customHighlight,
            shadow = customShadow,
            temperature = customTemperature,
            tint = customTint,
            thumbnail = thumbnail
        )
        historyManager.addHistory(history)
    }
    
    // 获取历史记录列表（用于UI显示）
    fun getHistoryList(): List<EditHistory> {
        return historyManager.getHistoryList()
    }
    
    // 获取历史记录统计信息
    fun getHistoryStats() = historyManager.getHistoryStats()
    
    // 分享图像
    fun shareImage(bitmap: Bitmap, context: Context) {
        viewModelScope.launch {
            try {
                val file = imageProcessor.saveBitmap(bitmap, "shared_${System.currentTimeMillis()}")
                file?.let {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        it
                    )
                    
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        type = "image/*"
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    context.startActivity(
                        android.content.Intent.createChooser(shareIntent, "分享图片")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // 裁切功能
    fun setCropRatio(ratio: Float?) {
        cropRatio = ratio
    }
    
    fun applyCrop() {
        selectedImageUri?.let { uri ->
            viewModelScope.launch {
                isProcessing = true
                try {
                    val bitmap = imageProcessor.cropImage(uri, cropRatio)
                    processedBitmap = bitmap
                    addToHistory("裁切")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessing = false
                }
            }
        }
    }
    
    fun resetCrop() {
        cropRatio = null
        processedBitmap = originalBitmap
    }
    
    // 特效功能
    fun applyEffect(effect: String) {
        currentEffect = effect
        selectedImageUri?.let { uri ->
            viewModelScope.launch {
                isProcessing = true
                try {
                    val bitmap = when (effect) {
                        "blur" -> imageProcessor.applyBlur(uri)
                        "sharpen" -> imageProcessor.applySharpen(uri)
                        "emboss" -> imageProcessor.applyEmboss(uri)
                        "edge" -> imageProcessor.applyEdgeDetection(uri)
                        "noise" -> imageProcessor.applyNoise(uri)
                        "vignette" -> imageProcessor.applyVignette(uri)
                        else -> processedBitmap
                    }
                    processedBitmap = bitmap
                    addToHistory("特效: $effect")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessing = false
                }
            }
        }
    }
    
    // 历史记录功能
    fun showHistory(): List<EditHistory> {
        return historyManager.getHistory()
    }
    
    private fun addToHistory(action: String) {
        val history = EditHistory(
            id = System.currentTimeMillis().toString(),
            action = action,
            timestamp = System.currentTimeMillis(),
            filter = currentFilter,
            brightness = customBrightness,
            contrast = customContrast,
            saturation = customSaturation,
            warmth = customWarmth
        )
        historyManager.addHistory(history)
    }
    
    fun addTextOverlay(text: String, color: Color, fontSize: Float, x: Float, y: Float) {
        val newOverlay = TextOverlay(
            id = System.currentTimeMillis().toString(),
            text = text,
            color = color,
            fontSize = fontSize,
            x = x,
            y = y
        )
        textOverlays = textOverlays + newOverlay
        addToHistory("添加文本: $text")
    }
    
    fun updateTextOverlayPosition(id: String, x: Float, y: Float) {
        textOverlays = textOverlays.map { overlay ->
            if (overlay.id == id) {
                overlay.copy(x = x, y = y)
            } else {
                overlay
            }
        }
    }
    
    fun removeTextOverlay(id: String) {
        val removedOverlay = textOverlays.find { it.id == id }
        textOverlays = textOverlays.filter { it.id != id }
        removedOverlay?.let {
            addToHistory("删除文本: ${it.text}")
        }
    }
}
