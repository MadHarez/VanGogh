package com.hy.vangogh.ui.screens.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hy.vangogh.data.model.ImageFilter
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

@Composable
fun FilterToolPanel(
    viewModel: ImageEditViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filters",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(ImageFilter.getAllFilters()) { filter ->
                FilterButton(
                    filter = filter,
                    isSelected = viewModel.currentFilter == filter,
                    onClick = { viewModel.applyFilter(filter) }
                )
            }
        }
    }
}

@Composable
private fun FilterButton(
    filter: ImageFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val filterName = when (filter) {
        ImageFilter.NONE -> "Original"
        ImageFilter.VINTAGE -> "Vintage"
        ImageFilter.VIVID -> "Vivid"
        ImageFilter.MONO -> "Mono"
        ImageFilter.WARM -> "Warm"
        ImageFilter.COOL -> "Cool"
        ImageFilter.DRAMATIC -> "Dramatic"
        ImageFilter.SOFT -> "Soft"
        ImageFilter.SHARP -> "Sharp"
        else -> "Unknown"
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
                text = filterName.first().toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = filterName,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp
        )
    }
}
