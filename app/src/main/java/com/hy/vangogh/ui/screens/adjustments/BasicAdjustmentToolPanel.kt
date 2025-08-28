package com.hy.vangogh.ui.screens.adjustments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

@Composable
fun BasicAdjustmentToolPanel(
    viewModel: ImageEditViewModel,
    selectedAdjustment: String?,
    onAdjustmentSelected: (String?) -> Unit
) {
    var brightness by remember { mutableStateOf(viewModel.customBrightness) }
    var contrast by remember { mutableStateOf(viewModel.customContrast) }
    var saturation by remember { mutableStateOf(viewModel.customSaturation) }
    var warmth by remember { mutableStateOf(viewModel.customWarmth) }
    var exposure by remember { mutableStateOf(viewModel.customExposure) }
    var highlight by remember { mutableStateOf(viewModel.customHighlight) }
    var shadow by remember { mutableStateOf(viewModel.customShadow) }
    var temperature by remember { mutableStateOf(viewModel.customTemperature) }
    var tint by remember { mutableStateOf(viewModel.customTint) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Basic Adjustments",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Brightness
        Text("Brightness: ${(brightness * 100).toInt()}", color = Color.White, fontSize = 14.sp)
        Slider(
            value = brightness,
            onValueChange = {
                brightness = it
                viewModel.updateBrightness(it)
            },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Contrast
        Text("Contrast: ${(contrast * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = contrast,
            onValueChange = {
                contrast = it
                viewModel.updateContrast(it)
            },
            valueRange = 0f..2f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Saturation
        Text("Saturation: ${(saturation * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = saturation,
            onValueChange = {
                saturation = it
                viewModel.updateSaturation(it)
            },
            valueRange = 0f..2f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Warmth
        Text("Warmth: ${(warmth * 100).toInt()}", color = Color.White, fontSize = 14.sp)
        Slider(
            value = warmth,
            onValueChange = {
                warmth = it
                viewModel.updateWarmth(it)
            },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Exposure
        Text("Exposure: ${(exposure * 100).toInt()}", color = Color.White, fontSize = 14.sp)
        Slider(
            value = exposure,
            onValueChange = {
                exposure = it
                viewModel.updateExposure(it)
            },
            valueRange = -2f..2f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}