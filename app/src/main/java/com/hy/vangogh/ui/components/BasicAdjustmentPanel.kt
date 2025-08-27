package com.hy.vangogh.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hy.vangogh.R
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

/**
 * 基础调节面板组件
 * 包含亮度、对比度、饱和度、色温等基础调节功能
 */
@Composable
fun BasicAdjustmentPanel(
    viewModel: ImageEditViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 亮度调节
        AdjustmentSlider(
            label = stringResource(R.string.brightness),
            value = viewModel.customBrightness,
            onValueChange = viewModel::updateCustomBrightness,
            valueRange = -1f..1f
        )

        // 对比度调节
        AdjustmentSlider(
            label = stringResource(R.string.contrast),
            value = viewModel.customContrast,
            onValueChange = viewModel::updateCustomContrast,
            valueRange = 0.5f..2f
        )

        // 饱和度调节
        AdjustmentSlider(
            label = stringResource(R.string.saturation),
            value = viewModel.customSaturation,
            onValueChange = viewModel::updateCustomSaturation,
            valueRange = 0f..2f
        )

        // 色温调节
        AdjustmentSlider(
            label = stringResource(R.string.temperature),
            value = viewModel.customWarmth,
            onValueChange = viewModel::updateCustomWarmth,
            valueRange = -1f..1f
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 重置按钮
        Button(
            onClick = { viewModel.resetToOriginal() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF333333)
            )
        ) {
            Text(stringResource(R.string.reset), color = Color.White)
        }
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = String.format("%.2f", value),
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
