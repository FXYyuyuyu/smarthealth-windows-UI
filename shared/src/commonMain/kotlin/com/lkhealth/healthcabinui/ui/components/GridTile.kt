package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
 *
 * 这里**不再给方块套白色卡片**：旧系统的图标 PNG 本身就把白色圆角底和投影画进了素材里，
 * 外面再包一层 Surface 就成了"白框套白框"，还把图标挤小了一圈。去掉容器后图标直接落在页面底色上，
 * 和旧系统一样由素材自己充当可点区域的视觉边界，同样的方块尺寸能放下明显更大的图标。
 *
 * 尺寸由宫格按窗口大小统一计算后通过 [modifier] 传入，[iconSize]/[labelSize] 随方块大小同步缩放。
 * [completed] 为本次已完成（置灰 + 勾选角标），[enabled] 为暂不可用（如尚无结果时的"查看结果"）。
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
    val clickable = enabled && !completed

    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = clickable,
                onClick = onClick,
            )
            .alpha(if (clickable) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 勾选角标贴在图标自己的右上角：方块通常比图标大一圈，挂在方块角上会飘得离图标很远。
        Box(contentAlignment = Alignment.Center) {
            Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(iconSize))
            if (completed) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "本次已完成",
                    tint = HealthCabinColors.Success,
                    modifier = Modifier.align(Alignment.TopEnd).size(iconSize * 0.24f),
                )
            }
        }
        Spacer(Modifier.height(Spacing.s))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontSize = labelSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
