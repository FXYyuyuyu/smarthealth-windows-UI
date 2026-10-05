package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.ui.theme.Spacing

/** 对应旧系统 InputUC 的数字键盘：4 列 × 3 行（1-9、0、X、清除），"清除"清空整个输入。 */
private val keypadRows = listOf(
    listOf("1", "2", "3", "4"),
    listOf("5", "6", "7", "8"),
    listOf("9", "0", "X", "清除"),
)

@Composable
fun NumericKeypad(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = 18,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        keypadRows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                row.forEach { key ->
                    when (key) {
                        "清除" -> KeypadKey(onClick = { onValueChange("") }) {
                            Text(key, style = MaterialTheme.typography.titleMedium)
                        }
                        else -> KeypadKey(onClick = { if (value.length < maxLength) onValueChange(value + key) }) {
                            Text(key, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interactionSource)
    val containerColor by rememberPressDarken(interactionSource, Color.White, darkenFactor = 0.06f)

    Surface(
        modifier = Modifier
            .size(width = 108.dp, height = 68.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
    ) {
        Box(Modifier.size(width = 108.dp, height = 68.dp), contentAlignment = Alignment.Center) {
            content()
        }
    }
}
