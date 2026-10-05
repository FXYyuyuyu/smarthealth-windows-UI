package com.lkhealth.healthcabinui.ui.measurement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.device.MeasurementCatalog
import com.lkhealth.healthcabinui.device.format
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.PageHeader
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.ResultValueRow
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.components.SectionCard
import com.lkhealth.healthcabinui.ui.theme.Spacing
import org.jetbrains.compose.resources.painterResource

/**
 * 对应旧系统 ResultUC：展示本次测量数值，"重新测量"回准备页，"确认结果"回项目宫格（或自动进入下一项）。
 * 旧系统结果页左侧是护士插画，但那张图是每个设备各自的整屏模板图（标题/护士/参考值文案都画在一起），
 * 没法只截出护士单独复用；这里左侧改成设备自己的图标，同样起到"左图右值"的视觉平衡。
 */
@Composable
fun ResultScreen(deviceType: DeviceType, viewModel: AppViewModel) {
    val spec = remember(deviceType) { MeasurementCatalog.of(deviceType) }
    val completedResults by viewModel.completedResults.collectAsStateWithLifecycle()
    val session = completedResults[deviceType]

    LaunchedEffect(deviceType, session) {
        if (session == null) return@LaunchedEffect
        val summary = spec.resultFields.joinToString("，") { "${it.label}${it.format(session)}${it.unit}" }
        viewModel.speak("${spec.title}测量完成。$summary")
    }

    Box(Modifier.fillMaxSize().padding(Spacing.xxl), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 900.dp).fillMaxWidth()) {
            PageHeader(title = "${spec.gridLabel}结果", subtitle = "请确认数据后继续")
            Spacer(Modifier.height(Spacing.xl))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(220.dp).background(spec.accent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painter = painterResource(spec.iconRes), contentDescription = null, modifier = Modifier.size(130.dp))
                }
                Spacer(Modifier.width(Spacing.xl))
                SectionCard(modifier = Modifier.weight(1f)) {
                    spec.resultFields.forEachIndexed { index, field ->
                        ResultValueRow(
                            label = field.label,
                            value = field.format(session),
                            unit = field.unit,
                        )
                        if (index != spec.resultFields.lastIndex) {
                            Spacer(Modifier.height(Spacing.m))
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            ) {
                SecondaryActionButton(
                    text = "重新测量",
                    onClick = { viewModel.retryMeasurement(deviceType) },
                    modifier = Modifier.weight(1f),
                )
                PrimaryActionButton(
                    text = "确认结果",
                    onClick = { viewModel.confirmResult(deviceType) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
