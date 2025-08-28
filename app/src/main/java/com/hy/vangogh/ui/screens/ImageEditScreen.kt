package com.hy.vangogh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.widget.Toast
import com.hy.vangogh.R
import com.hy.vangogh.data.model.ImageFilter
import com.hy.vangogh.data.model.Project
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModelFactory
import com.hy.vangogh.ui.components.HistoryDialog
import com.hy.vangogh.ui.components.ConfirmationDialog
import com.hy.vangogh.ui.screens.adjustments.BasicAdjustmentToolPanel
import com.hy.vangogh.ui.screens.adjustments.AdvancedAdjustmentToolPanel
import com.hy.vangogh.ui.screens.adjustments.AdvancedAdjustment
import com.hy.vangogh.ui.screens.effects.EnhancedEffectsToolPanel
import com.hy.vangogh.ui.screens.effects.EffectCategory
import com.hy.vangogh.ui.screens.filters.FilterToolPanel
import com.hy.vangogh.ui.screens.crop.CropToolPanel
import com.hy.vangogh.ui.screens.text.TextToolPanel

// Editing tool categories
enum class EditingTool {
    FILTERS, BASIC_ADJUST, ADVANCED_ADJUST, EFFECTS, CROP, TEXT
}


// Effects categories
enum class EffectCategory {
    BLUR, TEXTURE, GRAIN, NOISE_REDUCTION, ARTISTIC
}

// Text overlay data class
data class TextOverlay(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    val color: Color,
    val fontSize: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditScreen(
    project: Project? = null,
    onBackPressed: () -> Unit,
    viewModel: ImageEditViewModel = viewModel(factory = ImageEditViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current

    // Initialize processor if not already done
    LaunchedEffect(Unit) {
        viewModel.initializeProcessor(context)
    }

    var selectedTool by remember { mutableStateOf<EditingTool?>(null) }
    var selectedAdjustment by remember { mutableStateOf<String?>(null) }
    var selectedAdvancedAdjustment by remember { mutableStateOf<AdvancedAdjustment?>(null) }
    var selectedEffectCategory by remember { mutableStateOf<EffectCategory?>(null) }

    // Dialog states
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    // Collect StateFlow values
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val textOverlays by viewModel.textOverlays.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Modern Top Panel - Completely redesigned
            ModernTopPanel(
                onBackPressed = {
                    if (viewModel.hasEdits()) {
                        showBackConfirmDialog = true
                    } else {
                        onBackPressed()
                    }
                },
                onHistoryClick = { showHistoryDialog = true },
                viewModel = viewModel,
                canUndo = canUndo,
                canRedo = canRedo,
                context = context
            )

            // Full Screen Image Preview with Text Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                viewModel.selectedImageUri?.let { uri ->
                    if (viewModel.processedBitmap != null) {
                        AsyncImage(
                            model = viewModel.processedBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Text Overlays
                textOverlays.forEach { textOverlay ->
                    DraggableText(
                        textOverlay = textOverlay,
                        onPositionChanged = { newX, newY ->
                            viewModel.updateTextOverlayPosition(textOverlay.id, newX, newY)
                        },
                        onRemove = {
                            viewModel.removeTextOverlay(textOverlay.id)
                        }
                    )
                }

                if (viewModel.isProcessing) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Bottom Tool Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f),
                                Color.Black
                            )
                        )
                    )
            ) {
                // Adjustment Controls (when tool is selected)
                AnimatedVisibility(
                    visible = selectedTool != null,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    when (selectedTool) {
                        EditingTool.FILTERS -> FilterToolPanel(viewModel)
                        EditingTool.BASIC_ADJUST -> BasicAdjustmentToolPanel(
                            viewModel = viewModel,
                            selectedAdjustment = selectedAdjustment,
                            onAdjustmentSelected = { selectedAdjustment = it }
                        )
                        EditingTool.ADVANCED_ADJUST -> AdvancedAdjustmentToolPanel(
                            viewModel = viewModel,
                            selectedAdvancedAdjustment = selectedAdvancedAdjustment,
                            onAdvancedAdjustmentSelected = { selectedAdvancedAdjustment = it }
                        )
                        EditingTool.EFFECTS -> EnhancedEffectsToolPanel(
                            viewModel = viewModel,
                            selectedEffectCategory = selectedEffectCategory,
                            onEffectCategorySelected = { selectedEffectCategory = it }
                        )
                        EditingTool.CROP -> CropToolPanel(viewModel)
                        EditingTool.TEXT -> TextToolPanel(viewModel)
                        else -> Box(modifier = Modifier.height(120.dp))
                    }
                }

                // Main Tool Selection
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(EditingTool.values()) { tool ->
                        ToolButton(
                            tool = tool,
                            isSelected = selectedTool == tool,
                            onClick = {
                                selectedTool = if (selectedTool == tool) null else tool
                                selectedAdjustment = null
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Dialogs
        if (showHistoryDialog) {
            HistoryDialog(
                historyList = viewModel.showHistory(),
                onDismiss = { showHistoryDialog = false }
            )
        }
        
        if (showBackConfirmDialog) {
            ConfirmationDialog(
                title = "退出编辑",
                message = "您有未保存的编辑内容，确定要退出吗？",
                confirmText = "退出",
                cancelText = "继续编辑",
                onConfirm = {
                    showBackConfirmDialog = false
                    onBackPressed()
                },
                onCancel = { showBackConfirmDialog = false }
            )
        }
    }
}

@Composable
private fun ToolButton(
    tool: EditingTool,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val (iconRes, labelRes) = when (tool) {
        EditingTool.FILTERS -> R.drawable.ic_image to R.string.filters
        EditingTool.BASIC_ADJUST -> R.drawable.ic_adjust to R.string.basic_adjustments_tab
        EditingTool.ADVANCED_ADJUST -> R.drawable.ic_tune to R.string.advanced_adjustments
        EditingTool.EFFECTS -> R.drawable.ic_effects to R.string.tool_effects
        EditingTool.CROP -> R.drawable.ic_crop to R.string.tool_crop
        EditingTool.TEXT -> R.drawable.ic_text to R.string.tool_text
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isSelected) Color(0xFF4CAF50) else Color.Transparent,
                    CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(labelRes),
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun FilterToolPanel(viewModel: ImageEditViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.filters),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(ImageFilter.getAllFilters()) { filter ->
                FilterPreviewItem(
                    filter = filter,
                    isSelected = viewModel.currentFilter == filter,
                    onClick = { viewModel.applyFilter(filter) }
                )
            }
        }
    }
}

@Composable
private fun FilterPreviewItem(
    filter: ImageFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val filterName = when (filter.name) {
        "filter_original" -> context.getString(R.string.filter_original)
        "filter_vintage" -> context.getString(R.string.filter_vintage)
        "filter_vivid" -> context.getString(R.string.filter_vivid)
        "filter_mono" -> context.getString(R.string.filter_mono)
        "filter_warm" -> context.getString(R.string.filter_warm)
        "filter_cool" -> context.getString(R.string.filter_cool)
        "filter_dramatic" -> context.getString(R.string.filter_dramatic)
        "filter_soft" -> context.getString(R.string.filter_soft)
        "filter_sharp" -> context.getString(R.string.filter_sharp)
        else -> filter.name
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (isSelected) Color(0xFF4CAF50) else Color(0xFF333333),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = filterName.first().toString().uppercase(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = filterName,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun BasicAdjustmentToolPanel(
    viewModel: ImageEditViewModel,
    selectedAdjustment: String?,
    onAdjustmentSelected: (String?) -> Unit
) {
    val adjustments = listOf(
        "brightness" to R.string.brightness,
        "contrast" to R.string.contrast,
        "saturation" to R.string.saturation,
        "natural_saturation" to R.string.natural_saturation,
        "temperature" to R.string.temperature
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (selectedAdjustment == null) {
            Text(
                text = stringResource(R.string.basic_adjustments_tab),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(adjustments) { (key, labelRes) ->
                    AdjustmentButton(
                        label = stringResource(labelRes),
                        onClick = { onAdjustmentSelected(key) }
                    )
                }
            }
        } else {
            // Show slider for selected adjustment
            AdjustmentSliderPanel(
                adjustment = selectedAdjustment,
                viewModel = viewModel,
                onBack = { onAdjustmentSelected(null) }
            )
        }
    }
}

@Composable
private fun AdjustmentButton(
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFF333333), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.first().toString().uppercase(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun AdjustmentSliderPanel(
    adjustment: String,
    viewModel: ImageEditViewModel,
    onBack: () -> Unit
) {
    val value: Float
    val onValueChange: (Float) -> Unit
    val range: ClosedFloatingPointRange<Float>
    val labelRes: Int
    
    when (adjustment) {
        "brightness" -> {
            value = viewModel.customBrightness
            onValueChange = viewModel::updateCustomBrightness
            range = -1f..1f
            labelRes = R.string.brightness
        }
        "contrast" -> {
            value = viewModel.customContrast
            onValueChange = viewModel::updateCustomContrast
            range = 0.5f..2f
            labelRes = R.string.contrast
        }
        "saturation" -> {
            value = viewModel.customSaturation
            onValueChange = viewModel::updateCustomSaturation
            range = 0f..2f
            labelRes = R.string.saturation
        }
        "temperature" -> {
            value = viewModel.customWarmth
            onValueChange = viewModel::updateCustomWarmth
            range = -1f..1f
            labelRes = R.string.temperature
        }
        "natural_saturation" -> {
            value = viewModel.customNaturalSaturation
            onValueChange = viewModel::updateCustomNaturalSaturation
            range = 0f..2f
            labelRes = R.string.natural_saturation
        }
        else -> {
            value = 0f
            onValueChange = {}
            range = 0f..1f
            labelRes = R.string.brightness
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrowback),
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = stringResource(labelRes),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = String.format("%.0f", value * 100),
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CropToolPanel(viewModel: ImageEditViewModel) {
    val cropRatios = listOf(
        "自由" to null,
        "1:1" to 1f,
        "4:3" to 4f/3f,
        "16:9" to 16f/9f,
        "3:4" to 3f/4f,
        "9:16" to 9f/16f
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_crop),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(cropRatios) { (name, ratio) ->
                CropRatioButton(
                    name = name,
                    isSelected = viewModel.cropRatio == ratio,
                    onClick = { 
                        viewModel.updateCropRatio(ratio)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { 
                    viewModel.resetCrop()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333)
                )
            ) {
                Text("重置", color = Color.White)
            }
            
            Button(
                onClick = { 
                    viewModel.applyCrop()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("应用", color = Color.White)
            }
        }
    }
}

@Composable
private fun CropRatioButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isSelected) Color(0xFF4CAF50) else Color(0xFF333333),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.first().toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = name,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun EffectsToolPanel(viewModel: ImageEditViewModel) {
    val effects = listOf(
        "模糊" to "blur",
        "锐化" to "sharpen",
        "浮雕" to "emboss",
        "边缘" to "edge",
        "噪点" to "noise",
        "晕影" to "vignette"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_effects),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(effects) { (name, effect) ->
                EffectButton(
                    name = name,
                    isSelected = viewModel.currentEffect == effect,
                    onClick = { 
                        viewModel.applyEffect(effect)
                    }
                )
            }
        }
    }
}

@Composable
private fun EffectButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (isSelected) Color(0xFF4CAF50) else Color(0xFF333333),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.first().toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = name,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun TextToolPanel(viewModel: ImageEditViewModel) {
    var textInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.White) }
    var fontSize by remember { mutableStateOf(24f) }
    
    val textColors = listOf(
        Color.White, Color.Black, Color.Red, Color.Blue, 
        Color.Green, Color.Yellow, Color.Magenta, Color.Cyan
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tool_text),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 文字输入框
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("输入文字", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 颜色选择
        Text(
            text = "颜色",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(textColors) { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, CircleShape)
                        .clip(CircleShape)
                        .clickable { selectedColor = color }
                        .then(
                            if (selectedColor == color) {
                                Modifier.padding(2.dp)
                            } else Modifier
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 字体大小
        Text(
            text = "大小: ${fontSize.toInt()}",
            color = Color.White,
            fontSize = 14.sp
        )
        
        Slider(
            value = fontSize,
            onValueChange = { fontSize = it },
            valueRange = 12f..72f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 添加文字按钮
        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    viewModel.addTextOverlay(
                        text = textInput,
                        color = selectedColor,
                        fontSize = fontSize,
                        x = 0.5f, // 居中位置
                        y = 0.5f
                    )
                    textInput = "" // 清空输入框
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("添加文字", color = Color.White)
        }
    }
}

@Composable
private fun DraggableText(
    textOverlay: TextOverlay,
    onPositionChanged: (Float, Float) -> Unit,
    onRemove: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .offset(
                x = (textOverlay.x * 300).dp + offsetX.dp, // 300dp as reference width
                y = (textOverlay.y * 400).dp + offsetY.dp  // 400dp as reference height
            )
            .pointerInput(textOverlay.id) {
                detectDragGestures(
                    onDragEnd = {
                        // Update the actual position in the overlay
                        val newX = (textOverlay.x + offsetX / 300f).coerceIn(0f, 1f)
                        val newY = (textOverlay.y + offsetY / 400f).coerceIn(0f, 1f)
                        onPositionChanged(newX, newY)
                        offsetX = 0f
                        offsetY = 0f
                    }
                ) { change, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .background(
                Color.Black.copy(alpha = 0.3f),
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = textOverlay.text,
                color = textOverlay.color,
                fontSize = textOverlay.fontSize.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Remove button
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "×",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ModernTopPanel(
    onBackPressed: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: ImageEditViewModel,
    canUndo: Boolean,
    canRedo: Boolean,
    context: android.content.Context
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.9f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Section - Back & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modern Back Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable { onBackPressed() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrowback),
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Title Section
                Column {
                    Text(
                        text = "VanGogh",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right Section - Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo Button
                ModernActionButton(
                    iconRes = R.drawable.ic_undo,
                    contentDescription = stringResource(R.string.undo),
                    enabled = canUndo,
                    onClick = { viewModel.undo() }
                )
                
                // Redo Button  
                ModernActionButton(
                    iconRes = R.drawable.ic_redo,
                    contentDescription = stringResource(R.string.redo),
                    enabled = canRedo,
                    onClick = { viewModel.redo() }
                )
                
                // History Button
                ModernActionButton(
                    iconRes = R.drawable.ic_history,
                    contentDescription = "History",
                    enabled = true,
                    onClick = onHistoryClick
                )
                
                // Save Button
                ModernActionButton(
                    iconRes = R.drawable.ic_save,
                    contentDescription = stringResource(R.string.save),
                    enabled = true,
                    isHighlighted = true,
                    onClick = {
                        viewModel.processedBitmap?.let { bitmap ->
                            viewModel.saveImage("edited_${System.currentTimeMillis()}") { file ->
                                if (file != null) {
                                    Toast.makeText(context, "图片已保存", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } ?: Toast.makeText(context, "没有图片可保存", Toast.LENGTH_SHORT).show()
                    }
                )
                
                // Share Button
                ModernActionButton(
                    iconRes = R.drawable.ic_share,
                    contentDescription = stringResource(R.string.share),
                    enabled = true,
                    onClick = { 
                        viewModel.processedBitmap?.let { bitmap ->
                            // 先保存到相册，然后分享
                            viewModel.saveToGallery { success ->
                                if (success) {
                                    viewModel.shareImage(bitmap, context)
                                    Toast.makeText(context, "图片已保存到相册并分享", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } ?: Toast.makeText(context, "没有图片可分享", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ModernActionButton(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isHighlighted -> Color(0xFF007AFF)
        enabled -> Color.White.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.05f)
    }
    
    val iconTint = when {
        isHighlighted -> Color.White
        enabled -> Color.White
        else -> Color.White.copy(alpha = 0.4f)
    }
    
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
    }
}