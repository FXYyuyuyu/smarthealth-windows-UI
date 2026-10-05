package com.lkhealth.healthcabinui.ui.measurement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.device.MeasurementCatalog
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.CenteredContentCard
import com.lkhealth.healthcabinui.ui.components.PartStepper
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.theme.Spacing
import org.jetbrains.compose.resources.painterResource

/**
 * 对应旧系统 PrepareUC：有旧系统"XX测量图示"整屏指导图的项目直接复用原图（标题/示意图/文案已经画在图里）；
 * 没有对应图的项目退回图标+文字说明。进入页面先做设备连接（对应指南里的 connect() 步骤），
 * 连接期间按钮不可点并显示"正在连接设备"提示；连接失败直接转错误页。
 * 指导图按每张图实测过的安全宽高比（`guideAspectRatio`）做 FillWidth+TopCenter 裁切，
 * 只裁掉原图底部旧系统留给按钮的空白画布，不会切到文字/示意图内容；没量过安全比例的图退回 Fit 整图缩放。
 */
@Composable
fun PrepareScreen(deviceType: DeviceType, viewModel: AppViewModel) {
    val spec = remember(deviceType) { MeasurementCatalog.of(deviceType) }
    var connecting by remember(deviceType) { mutableStateOf(true) }

    LaunchedEffect(deviceType) {
        connecting = true
        viewModel.speak("即将测量${spec.title}。${spec.prepareInstruction}")
        viewModel.ensureConnected(deviceType)
            .onSuccess { connecting = false }
            .onFailure { viewModel.onMeasurementFailed(deviceType, it.message ?: "设备连接失败，请联系工作人员") }
    }

    CenteredContentCard(maxWidth = if (spec.guideRes != null) 820.dp else 680.dp) {
        val guideRes = spec.guideRes
        if (guideRes != null) {
            val ratio = spec.guideAspectRatio
            if (ratio != null) {
                // 按实测安全比例裁切：只截掉原图底部的空白画布，图片内容本身不缩小、不裁切。
                Image(
                    painter = painterResource(guideRes),
                    contentDescription = "${spec.title}测量图示",
                    modifier = Modifier.fillMaxWidth()
                        .aspectRatio(ratio)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.TopCenter,
                )
            } else {
                Image(
                    painter = painterResource(guideRes),
                    contentDescription = "${spec.title}测量图示",
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
        } else {
            Spacer(Modifier.height(Spacing.l))
            Box(
                Modifier.size(200.dp).background(spec.accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(spec.iconRes), contentDescription = null, modifier = Modifier.size(120.dp))
            }
            Spacer(Modifier.height(Spacing.xl))
            Text(spec.gridLabel, style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(Spacing.m))
            Text(
                spec.prepareInstruction,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.l))
        }

        if (spec.isMultiPart) {
            Spacer(Modifier.height(Spacing.m))
            PartStepper(parts = spec.parts, currentIndex = 0, completedKeys = emptySet())
        }

        Spacer(Modifier.height(Spacing.m))
        if (connecting) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(Spacing.s))
                Text(
                    "正在连接设备…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Spacing.s))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            SecondaryActionButton(
                text = "退出",
                onClick = { viewModel.backToItemSelection(deviceType) },
                modifier = Modifier.weight(1f),
            )
            PrimaryActionButton(
                text = "开始测量",
                enabled = !connecting,
                onClick = { viewModel.startMeasuring(deviceType) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
