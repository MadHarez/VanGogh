package com.hy.vangogh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hy.vangogh.data.model.EditHistory
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPanel(
    viewModel: ImageEditViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val historyList = viewModel.getHistoryList()
    val currentIndex = viewModel.getHistoryStats().currentIndex
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "编辑历史",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 历史记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(historyList) { index, history ->
                    HistoryItem(
                        history = history,
                        isSelected = index == currentIndex,
                        timestamp = dateFormat.format(Date(history.timestamp)),
                        onClick = {
                            // 通过撤销/重做导航到指定历史记录
                            navigateToHistory(viewModel, currentIndex, index)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    history: EditHistory,
    isSelected: Boolean,
    timestamp: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
            else 
                Color.Gray.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 缩略图
            history.thumbnailBitmap?.let { thumbnail ->
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 历史信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = history.actionDescription,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                
                // 显示调节参数摘要
                val adjustmentSummary = buildAdjustmentSummary(history)
                if (adjustmentSummary.isNotEmpty()) {
                    Text(
                        text = adjustmentSummary,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                
                Text(
                    text = timestamp,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            // 选中指示器
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
}

// 导航到指定历史记录的辅助函数
private fun navigateToHistory(
    viewModel: ImageEditViewModel,
    currentIndex: Int,
    targetIndex: Int
) {
    when {
        targetIndex < currentIndex -> {
            // 需要撤销
            repeat(currentIndex - targetIndex) {
                viewModel.undo()
            }
        }
        targetIndex > currentIndex -> {
            // 需要重做
            repeat(targetIndex - currentIndex) {
                viewModel.redo()
            }
        }
        // targetIndex == currentIndex 时不需要操作
    }
}

// 构建调节参数摘要
private fun buildAdjustmentSummary(history: EditHistory): String {
    val adjustments = mutableListOf<String>()
    
    if (history.customBrightness != 0f) {
        adjustments.add("亮度${String.format("%.1f", history.customBrightness)}")
    }
    if (history.customExposure != 0f) {
        adjustments.add("曝光${String.format("%.1f", history.customExposure)}")
    }
    if (history.customContrast != 1f) {
        adjustments.add("对比度${String.format("%.1f", history.customContrast)}")
    }
    if (history.customSaturation != 1f) {
        adjustments.add("饱和度${String.format("%.1f", history.customSaturation)}")
    }
    if (history.customHighlight != 0f) {
        adjustments.add("高光${String.format("%.1f", history.customHighlight)}")
    }
    if (history.customShadow != 0f) {
        adjustments.add("阴影${String.format("%.1f", history.customShadow)}")
    }
    if (history.customTemperature != 0f) {
        adjustments.add("色温${String.format("%.1f", history.customTemperature)}")
    }
    if (history.customTint != 0f) {
        adjustments.add("色调${String.format("%.1f", history.customTint)}")
    }
    
    return if (adjustments.isEmpty()) {
        ""
    } else {
        adjustments.take(3).joinToString(" | ") + if (adjustments.size > 3) "..." else ""
    }
}
