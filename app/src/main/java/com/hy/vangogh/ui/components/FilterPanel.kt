package com.hy.vangogh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hy.vangogh.R
import com.hy.vangogh.data.model.ImageFilter
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

/**
 * 滤镜面板组件
 * 显示所有可用的滤镜选项
 */
@Composable
fun FilterPanel(
    viewModel: ImageEditViewModel,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = modifier
    ) {
        items(ImageFilter.getAllFilters()) { filter ->
            FilterItem(
                filter = filter,
                isSelected = viewModel.currentFilter == filter,
                onClick = { viewModel.applyFilter(filter) }
            )
        }
    }
}

@Composable
private fun FilterItem(
    filter: ImageFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Get localized filter name
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
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSelected) Color(0xFF667eea) else Color(0xFF333333)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = filterName.first().toString(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = filterName,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 12.sp
        )
    }
}
