package com.hy.vangogh.ui.screens

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
                items(EffectCategory.values()) { category ->
                    EffectCategoryButton(
                        category = category,
                        onClick = { onEffectCategorySelected(category) }
                    )
                }
            }
        } else {
            EffectCategoryPanel(
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
    val label = when (category) {
        EffectCategory.BLUR -> "Blur"
        EffectCategory.TEXTURE -> "Texture"
        EffectCategory.GRAIN -> "Grain"
        EffectCategory.NOISE_REDUCTION -> "Noise Reduction"
        EffectCategory.ARTISTIC -> "Artistic"
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
                .background(Color(0xFF333333), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.first().toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun EffectCategoryPanel(
    category: EffectCategory,
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
                text = when (category) {
                    EffectCategory.BLUR -> "Blur Effects"
                    EffectCategory.TEXTURE -> "Texture Effects"
                    EffectCategory.GRAIN -> "Grain Effects"
                    EffectCategory.NOISE_REDUCTION -> "Noise Reduction"
                    EffectCategory.ARTISTIC -> "Artistic Effects"
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (category) {
            EffectCategory.BLUR -> BlurEffectPanel(viewModel)
            EffectCategory.TEXTURE -> TextureEffectPanel(viewModel)
            EffectCategory.GRAIN -> GrainEffectPanel(viewModel)
            EffectCategory.NOISE_REDUCTION -> NoiseReductionPanel(viewModel)
            EffectCategory.ARTISTIC -> ArtisticEffectPanel(viewModel)
        }
    }
}

@Composable
private fun BlurEffectPanel(viewModel: ImageEditViewModel) {
    var blurRadius by remember { mutableStateOf(5f) }
    var focusX by remember { mutableStateOf(0.5f) }
    var focusY by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(listOf("Gaussian Blur", "Background Blur", "Motion Blur", "Radial Blur")) { type ->
                Button(
                    onClick = {
                        // Apply different blur types
                        when (type) {
                            "Gaussian Blur" -> {
                                // viewModel.applyGaussianBlur(blurRadius.toInt())
                            }
                            "Background Blur" -> {
                                // viewModel.applyBackgroundBlur(blurRadius.toInt(), focusX, focusY)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF333333)
                    )
                ) {
                    Text(type, color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Text("Blur Intensity: ${blurRadius.toInt()}", color = Color.White, fontSize = 14.sp)
        Slider(
            value = blurRadius,
            onValueChange = { blurRadius = it },
            valueRange = 1f..25f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
private fun TextureEffectPanel(viewModel: ImageEditViewModel) {
    var textureIntensity by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(listOf("Paper", "Canvas", "Fabric", "Metal", "Wood")) { texture ->
                Button(
                    onClick = {
                        // viewModel.applyTexture(texture, textureIntensity)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF333333)
                    )
                ) {
                    Text(texture, color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Text("Texture Intensity: ${(textureIntensity * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = textureIntensity,
            onValueChange = { textureIntensity = it },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
private fun GrainEffectPanel(viewModel: ImageEditViewModel) {
    var grainIntensity by remember { mutableStateOf(0f) }
    var grainSize by remember { mutableStateOf(2f) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Grain Intensity: ${(grainIntensity * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = grainIntensity,
            onValueChange = {
                grainIntensity = it
                // viewModel.updateGrain(grainIntensity, grainSize.toInt())
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Grain Size: ${grainSize.toInt()}", color = Color.White, fontSize = 14.sp)
        Slider(
            value = grainSize,
            onValueChange = {
                grainSize = it
                // viewModel.updateGrain(grainIntensity, grainSize.toInt())
            },
            valueRange = 1f..10f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listOf("Film", "Digital", "Vintage", "Fine Grain")) { type ->
                Button(
                    onClick = {
                        // viewModel.applyGrainType(type, grainIntensity, grainSize.toInt())
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF333333)
                    )
                ) {
                    Text(type, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun NoiseReductionPanel(viewModel: ImageEditViewModel) {
    var reductionStrength by remember { mutableStateOf(0.5f) }
    var preserveDetails by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Noise Reduction Strength: ${(reductionStrength * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
        Slider(
            value = reductionStrength,
            onValueChange = {
                reductionStrength = it
                // viewModel.updateNoiseReduction(reductionStrength, preserveDetails)
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = preserveDetails,
                onCheckedChange = {
                    preserveDetails = it
                    // viewModel.updateNoiseReduction(reductionStrength, preserveDetails)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50),
                    uncheckedColor = Color.Gray
                )
            )
            Text("Preserve Details", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ArtisticEffectPanel(viewModel: ImageEditViewModel) {
    val effects = listOf("Oil Painting", "Watercolor", "Sketch", "Cartoon", "Emboss", "Mosaic")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(effects) { effect ->
            Button(
                onClick = {
                    // viewModel.applyArtisticEffect(effect)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333)
                )
            ) {
                Text(effect, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}