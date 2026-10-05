package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * 项目宫格方块，对应旧系统各模块的 LogoUC：只有"大图标 + 项目名"，图标直接用旧系统素材。
 * 尺寸由宫格按窗口大小统一计算后通过 [modifier] 传入，[iconSize]/[labelSize] 随方块大小同步缩放。
 * [dimmed] 用于本次已完成（额外显示勾选角标）或暂不可用（如尚无结果时的"查看结果"）两种置灰状态。
 */
@Composable
fun GridTile(
    iconRes: DrawableResource,
    label: String,
    iconSize: Dp,
    labelSize: TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    completed: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interactionSource)
    val baseColor = if (completed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    val containerColor by rememberPressDarken(interactionSource, baseColor, darkenFactor = 0.05f)
    val clickable = enabled && !completed

    Surface(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = clickable,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        shadowElevation = if (clickable) 1.dp else 0.dp,
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(Spacing.s).alpha(if (clickable) 1f else 0.45f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(iconSize))
                Spacer(Modifier.height(Spacing.s))
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = labelSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            if (completed) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "本次已完成",
                    tint = HealthCabinColors.Success,
                    modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.s).size(28.dp),
                )
            }
        }
    }
}
