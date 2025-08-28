package com.hy.vangogh.ui.screens.adjustments

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hy.vangogh.R
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

// Advanced adjustment categories
enum class AdvancedAdjustment {
    HSL, CURVES, FADE, TONE
}

@Composable
fun AdvancedAdjustmentToolPanel(
    viewModel: ImageEditViewModel,
    selectedAdvancedAdjustment: AdvancedAdjustment?,
    onAdvancedAdjustmentSelected: (AdvancedAdjustment?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (selectedAdvancedAdjustment == null) {
            Text(
                text = "Advanced Adjustments",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(AdvancedAdjustment.values()) { adjustment ->
                    AdvancedAdjustmentButton(
                        adjustment = adjustment,
                        onClick = { onAdvancedAdjustmentSelected(adjustment) }
                    )
                }
            }
        } else {
            AdvancedAdjustmentControlPanel(
                adjustment = selectedAdvancedAdjustment,
                viewModel = viewModel,
                onBack = { onAdvancedAdjustmentSelected(null) }
            )
        }
    }
}

@Composable
private fun AdvancedAdjustmentButton(
    adjustment: AdvancedAdjustment,
    onClick: () -> Unit
) {
    val label = when (adjustment) {
        AdvancedAdjustment.HSL -> "HSL"
        AdvancedAdjustment.CURVES -> "Curves"
        AdvancedAdjustment.FADE -> "Fade"
        AdvancedAdjustment.TONE -> "Tone"
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
private fun AdvancedAdjustmentControlPanel(
    adjustment: AdvancedAdjustment,
    viewModel: ImageEditViewModel,
    onBack: () -> Unit
) {
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
                text = when (adjustment) {
                    AdvancedAdjustment.HSL -> "HSL Adjustment"
                    AdvancedAdjustment.CURVES -> "Curves"
                    AdvancedAdjustment.FADE -> "Fade Effect"
                    AdvancedAdjustment.TONE -> "Tone Adjustment"
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (adjustment) {
            AdvancedAdjustment.HSL -> HSLControlPanel(viewModel)
            AdvancedAdjustment.CURVES -> CurveControlPanel(viewModel)
            AdvancedAdjustment.FADE -> FadeControlPanel(viewModel)
            else -> {
                Text(
                    text = "Feature under development...",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun HSLControlPanel(viewModel: ImageEditViewModel) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var lightness by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Hue: ${hue.toInt()}°", color = Color.White, fontSize = 14.sp)
        Slider(
            value = hue,
            onValueChange = { 
                hue = it
                viewModel.applyHSLAdjustment(hue, saturation, lightness)
            },
            valueRange = -180f..180f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Saturation: ${(saturation * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = saturation,
            onValueChange = { 
                saturation = it
                viewModel.applyHSLAdjustment(hue, saturation, lightness)
            },
            valueRange = 0f..2f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Lightness: ${(lightness * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = lightness,
            onValueChange = { 
                lightness = it
                viewModel.applyHSLAdjustment(hue, saturation, lightness)
            },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
private fun CurveControlPanel(viewModel: ImageEditViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Curves Presets",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listOf("Increased Contrast", "Soft Contrast", "Film Style", "Vintage")) { preset ->
                Button(
                    onClick = { viewModel.applyCurvesAdjustment(preset) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF333333)
                    )
                ) {
                    Text(preset, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun FadeControlPanel(viewModel: ImageEditViewModel) {
    var fadeIntensity by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Fade Intensity: ${(fadeIntensity * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = fadeIntensity,
            onValueChange = { 
                fadeIntensity = it
                viewModel.applyFadeEffect(fadeIntensity)
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