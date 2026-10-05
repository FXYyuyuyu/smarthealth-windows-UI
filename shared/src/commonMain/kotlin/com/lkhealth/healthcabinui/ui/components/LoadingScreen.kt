package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing

/**
 * 共用的全屏等待态：蓝色圆角面板（转圈 + 白字文案）+ 面板底部一条浅色装饰条，
 * 对应旧系统 MeauingFUC/UploadDataUC 的"蓝色对话框 + Measuring.gif + 文案"设计。
 */
@Composable
fun LoadingScreen(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    CenteredContentCard(modifier, maxWidth = 620.dp) {
        Surface(
            color = HealthCabinColors.Primary,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 5.dp,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(Spacing.l))
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                    )
                }
                Box(
                    Modifier.fillMaxWidth().height(28.dp).background(MaterialTheme.colorScheme.background),
                )
            }
        }
    }
}
