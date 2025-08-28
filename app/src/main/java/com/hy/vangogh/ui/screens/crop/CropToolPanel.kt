package com.hy.vangogh.ui.screens.crop

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
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

@Composable
fun CropToolPanel(
    viewModel: ImageEditViewModel
) {
    val cropRatios = listOf(
        "Free" to null,
        "1:1" to 1f,
        "4:3" to 4f/3f,
        "3:4" to 3f/4f,
        "16:9" to 16f/9f,
        "9:16" to 9f/16f
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Crop",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(cropRatios) { (name, ratio) ->
                CropRatioButton(
                    name = name,
                    ratio = ratio,
                    isSelected = viewModel.cropRatio == ratio,
                    onClick = { viewModel.updateCropRatio(ratio) }
                )
            }
        }
    }
}

@Composable
private fun CropRatioButton(
    name: String,
    ratio: Float?,
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
