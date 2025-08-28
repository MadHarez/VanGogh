package com.hy.vangogh.ui.screens.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun TextToolPanel(
    viewModel: ImageEditViewModel
) {
    var textInput by remember { mutableStateOf("") }
    var textColor by remember { mutableStateOf(Color.White) }
    var fontSize by remember { mutableStateOf(24f) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Text",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 文字输入
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("输入文字", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 字体大小
        Text("Text Size: ${fontSize.toInt()}px", color = Color.White, fontSize = 14.sp)
        Slider(
            value = fontSize,
            onValueChange = { fontSize = it },
            valueRange = 12f..72f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4CAF50),
                activeTrackColor = Color(0xFF4CAF50),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 添加文字按钮
        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    viewModel.addTextOverlay(
                        text = textInput,
                        color = textColor,
                        fontSize = fontSize,
                        x = 100f,
                        y = 100f
                    )
                    textInput = ""
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("add Text", color = Color.White)
        }
    }
}
