package com.hy.vangogh.ui.screens.effects

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

enum class EffectCategory {
    BLUR, SHARPEN, NOISE, VINTAGE
}

@Composable
fun EnhancedEffectsToolPanel(
    viewModel: ImageEditViewModel,
    selectedEffectCategory: EffectCategory?,
    onEffectCategorySelected: (EffectCategory?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (selectedEffectCategory == null) {
            Text(
                text = "Effects",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(EffectCategory.values()) { category ->
                    EffectCategoryButton(
                        category = category,
                        onClick = { onEffectCategorySelected(category) }
                    )
                }
            }
        } else {
            EffectControlPanel(
                category = selectedEffectCategory,
                viewModel = viewModel,
                onBack = { onEffectCategorySelected(null) }
            )
        }
    }
}

@Composable
private fun EffectCategoryButton(
    category: EffectCategory,
    onClick: () -> Unit
) {
    val categoryName = when (category) {
        EffectCategory.BLUR -> "Blur"
        EffectCategory.SHARPEN -> "Sharpen"
        EffectCategory.NOISE -> "Noise"
        EffectCategory.VINTAGE -> "Vintage"
    }

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
                text = categoryName.first().toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = categoryName,
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun EffectControlPanel(
    category: EffectCategory,
    viewModel: ImageEditViewModel,
    onBack: () -> Unit
) {
    var effectIntensity by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← back", color = Color.White)
            }

            Text(
                text = when (category) {
                    EffectCategory.BLUR -> "Blur Effect"
                    EffectCategory.SHARPEN -> "Sharpen Effect"
                    EffectCategory.NOISE -> "Noise Effect"
                    EffectCategory.VINTAGE -> "Vintage Effect"
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("强度: ${(effectIntensity * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = effectIntensity,
            onValueChange = {
                effectIntensity = it
                // 这里可以调用相应的特效处理方法
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}