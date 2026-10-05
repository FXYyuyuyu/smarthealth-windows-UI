package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.ui.theme.Spacing

/**
 * 过渡态/表单类页面的通用内容容器：内容直接铺在页面背景上（不再套一层独立卡片+阴影），
 * 贴近旧系统"整屏背景 + 居中内容"的结构。整体垂直居中，宽度尽量吃满（上限较宽），
 * 让指导图/图标等内容尽量放大、减少四周留白；内部自带滚动兜底，内容在较矮的窗口下
 * 也不会把底部按钮挤出可视区域。
 */
@Composable
fun CenteredContentCard(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 680.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xl, vertical = Spacing.l),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(0.86f)
                .widthIn(max = maxWidth)
                .verticalScroll(rememberScrollState())
                .padding(vertical = Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}
