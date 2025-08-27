package com.hy.vangogh.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hy.vangogh.R
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

@Composable
fun AdvancedAdjustmentPanel(
    viewModel: ImageEditViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.advanced_adjustments),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        // 基础调节组
        AdjustmentGroup(title = stringResource(R.string.basic_adjustments)) {
            AdjustmentSlider(
                label = stringResource(R.string.brightness),
                value = viewModel.customBrightness,
                onValueChange = viewModel::updateCustomBrightness,
                valueRange = -1f..1f,
                defaultValue = 0f
            )
            
            AdjustmentSlider(
                label = stringResource(R.string.exposure),
                value = viewModel.customExposure,
                onValueChange = viewModel::updateCustomExposure,
                valueRange = -2f..2f,
                defaultValue = 0f
            )
            
            AdjustmentSlider(
                label = stringResource(R.string.contrast),
                value = viewModel.customContrast,
                onValueChange = viewModel::updateCustomContrast,
                valueRange = 0f..2f,
                defaultValue = 1f
            )
        }
        
        // 色彩调节组
        AdjustmentGroup(title = stringResource(R.string.color_adjustments)) {
            AdjustmentSlider(
                label = stringResource(R.string.saturation),
                value = viewModel.customSaturation,
                onValueChange = viewModel::updateCustomSaturation,
                valueRange = 0f..2f,
                defaultValue = 1f
            )
            
            AdjustmentSlider(
                label = stringResource(R.string.temperature),
                value = viewModel.customTemperature,
                onValueChange = viewModel::updateCustomTemperature,
                valueRange = -1f..1f,
                defaultValue = 0f
            )
            
            AdjustmentSlider(
                label = stringResource(R.string.tint),
                value = viewModel.customTint,
                onValueChange = viewModel::updateCustomTint,
                valueRange = -1f..1f,
                defaultValue = 0f
            )
        }
        
        // 光影调节组
        AdjustmentGroup(title = stringResource(R.string.light_shadow_adjustments)) {
            AdjustmentSlider(
                label = stringResource(R.string.highlight),
                value = viewModel.customHighlight,
                onValueChange = viewModel::updateCustomHighlight,
                valueRange = -1f..1f,
                defaultValue = 0f
            )
            
            AdjustmentSlider(
                label = stringResource(R.string.shadow),
                value = viewModel.customShadow,
                onValueChange = viewModel::updateCustomShadow,
                valueRange = -1f..1f,
                defaultValue = 0f
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 重置按钮
        Button(
            onClick = { viewModel.resetToOriginal() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF333333)
            )
        ) {
            Text(stringResource(R.string.reset_all_adjustments), color = Color.White)
        }
    }
}

@Composable
private fun AdjustmentGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            content()
        }
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    defaultValue: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp
            )
            
            Row {
                Text(
                    text = String.format("%.2f", value),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                
                if (value != defaultValue) {
                    TextButton(
                        onClick = { onValueChange(defaultValue) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.reset),
                            color = Color(0xFF667eea),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF667eea),
                activeTrackColor = Color(0xFF667eea),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}
