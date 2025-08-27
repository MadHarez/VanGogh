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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hy.vangogh.R
import com.hy.vangogh.data.model.ImageFilter
import com.hy.vangogh.data.model.Project
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

// Editing tool categories
enum class EditingTool {
    FILTERS, ADJUST, CROP, EFFECTS, TEXT
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
    viewModel: ImageEditViewModel = viewModel()
) {
    val context = LocalContext.current

    // 加载项目数据
    LaunchedEffect(project) {
        project?.let { viewModel.loadProject(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.initializeProcessor(context)
    }

    var selectedTool by remember { mutableStateOf<EditingTool?>(null) }
    var selectedAdjustment by remember { mutableStateOf<String?>(null) }

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
            // Top Bar with Image Editing style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Back button and title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrowback),
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Image",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Editing",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                // Right side - Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_undo),
                            contentDescription = stringResource(R.string.undo),
                            tint = if (canUndo) Color.White else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_redo),
                            contentDescription = stringResource(R.string.redo),
                            tint = if (canRedo) Color.White else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { 
                            // TODO: 显示历史记录对话框
                            viewModel.showHistory()
                            // 可以在这里显示历史记录列表
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_history),
                            contentDescription = "History",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.saveImage("edited_${System.currentTimeMillis()}") { file ->
                                // Handle save result
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = stringResource(R.string.save),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { 
                            viewModel.processedBitmap?.let { bitmap ->
                                viewModel.shareImage(bitmap, context)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = stringResource(R.string.share),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

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
                        EditingTool.ADJUST -> AdjustmentToolPanel(
                            viewModel = viewModel,
                            selectedAdjustment = selectedAdjustment,
                            onAdjustmentSelected = { selectedAdjustment = it }
                        )
                        EditingTool.CROP -> CropToolPanel(viewModel)
                        EditingTool.EFFECTS -> EffectsToolPanel(viewModel)
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
        EditingTool.ADJUST -> R.drawable.ic_adjust to R.string.basic_adjustments_tab
        EditingTool.CROP -> R.drawable.ic_crop to R.string.tool_crop
        EditingTool.EFFECTS -> R.drawable.ic_effects to R.string.tool_effects
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
private fun AdjustmentToolPanel(
    viewModel: ImageEditViewModel,
    selectedAdjustment: String?,
    onAdjustmentSelected: (String?) -> Unit
) {
    val adjustments = listOf(
        "brightness" to R.string.brightness,
        "contrast" to R.string.contrast,
        "saturation" to R.string.saturation,
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
                        viewModel.setCropRatio(ratio)
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