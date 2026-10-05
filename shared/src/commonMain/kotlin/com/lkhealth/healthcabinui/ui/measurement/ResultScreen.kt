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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.lkhealth.healthcabinui.device.advices
import com.lkhealth.healthcabinui.device.evaluate
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.HealthAdviceCard
import com.lkhealth.healthcabinui.ui.components.PageHeader
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.ResultValueRow
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.components.SectionCard
import com.lkhealth.healthcabinui.ui.theme.Spacing
import org.jetbrains.compose.resources.painterResource

/**
 * 对应旧系统 ResultUC：展示本次测量数值，"重新测量"回准备页，"确认结果"回项目宫格。
 * 旧系统结果页左侧是护士插画，但那张图是每个设备各自的整屏模板图（标题/护士/参考值文案都画在一起），
 * 没法只截出护士单独复用；这里左侧改成设备自己的图标，同样起到"左图右值"的视觉平衡。
 *
 * 数值旁的参考范围与"偏高/偏低"判定、以及下方的健康建议，复刻的是旧系统
 * `PrintModel` 的 minRange/maxRange/ItemPrompt/ItemSuggest——旧系统只把这些打在纸质报告上，
 * 屏幕上看不到，这里补到屏幕上。
 */
@Composable
fun ResultScreen(deviceType: DeviceType, viewModel: AppViewModel) {
    val spec = remember(deviceType) { MeasurementCatalog.of(deviceType) }
    val completedResults by viewModel.completedResults.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val session = completedResults[deviceType]

    val evaluated = remember(spec, session, currentUser) { spec.evaluate(session, currentUser) }
    val advices = remember(evaluated) { evaluated.advices() }
    val abnormalCount = evaluated.count { it.isAbnormal }

    LaunchedEffect(deviceType, evaluated) {
        if (session == null) return@LaunchedEffect
        // 异常项顺带把"偏高/偏低"念出来——测完的人常常还没走到屏幕正前方。
        val summary = evaluated.joinToString("，") { field ->
            val suffix = if (field.isAbnormal) "，${field.prompt.label}" else ""
            "${field.label}${field.display}${field.unit}$suffix"
        }
        viewModel.speak("${spec.title}测量完成。$summary")
    }

    Box(Modifier.fillMaxSize().padding(horizontal = Spacing.xxl, vertical = Spacing.xl), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 900.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).background(spec.accent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painter = painterResource(spec.iconRes), contentDescription = null, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.width(Spacing.m))
                PageHeader(
                    title = "${spec.gridLabel}结果",
                    subtitle = if (abnormalCount > 0) "有 $abnormalCount 项指标超出参考范围，请确认数据后继续" else "各项指标均在参考范围内，请确认数据后继续",
                )
            }
            Spacer(Modifier.height(Spacing.m))

            // 数值卡整幅展开——数值本身是这一屏的主角，之前把设备图标并排放在左边会把它挤成半幅。
            // 图标缩小后移到标题旁，仍然交代"这是哪一项"，但不再和数字抢地方。
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    evaluated.forEachIndexed { index, field ->
                        ResultValueRow(field)
                        if (index != evaluated.lastIndex) {
                            Spacer(Modifier.height(Spacing.m))
                        }
                    }
                }

                if (advices.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.l))
                    HealthAdviceCard(advices, compact = true)
                }
                // 底部留白，避免最后一段建议正好贴在按钮条下缘、看着像被截断。
                Spacer(Modifier.height(Spacing.l))
            }

            Spacer(Modifier.height(Spacing.xl))
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
