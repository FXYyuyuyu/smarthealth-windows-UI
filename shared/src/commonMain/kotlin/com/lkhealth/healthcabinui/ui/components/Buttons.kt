package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 旧系统里"开始测量"/"退出操作"/"确认结果"这些按钮几乎是同一种浅色圆角药丸样式，
 * 没有强烈的"实心色块 vs 描边"对比，靠文字本身表达含义。这里统一成同一种浅底样式，
 * 只用文字颜色/字重做很轻的主次提示，贴近参考截图的观感。
 */
private val ButtonHeight = 56.dp
private val ButtonShape = RoundedCornerShape(percent = 50)

/**
 * 主/次按钮均自绘按压反馈（轻微下压 + 变暗），不用 Material 默认的水波纹 + 弹起动效，
 * 手感更安静克制，符合"反馈柔和、不弹跳"的要求。
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interactionSource)
    val containerColor by rememberPressDarken(interactionSource, MaterialTheme.colorScheme.surface, darkenFactor = 0.06f)
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier
            .height(ButtonHeight)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick,
            ),
        shape = ButtonShape,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.4f else 0.15f)),
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = contentColor,
                    )
                    Spacer(Modifier.width(10.dp))
                }
                icon != null -> {
                    Icon(icon, contentDescription = null, tint = contentColor)
                    Spacer(Modifier.width(8.dp))
                }
            }
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interactionSource)
    val baseColor = MaterialTheme.colorScheme.surface
    val containerColor by rememberPressDarken(interactionSource, baseColor, darkenFactor = 0.06f)
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier
            .height(ButtonHeight)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        shape = ButtonShape,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.titleMedium, color = contentColor)
        }
    }
}
