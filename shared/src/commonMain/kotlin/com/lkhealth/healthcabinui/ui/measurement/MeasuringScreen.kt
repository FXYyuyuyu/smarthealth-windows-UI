package com.lkhealth.healthcabinui.ui.measurement

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.device.MeasurementCatalog
import com.lkhealth.healthcabinui.device.SessionProgress
import com.lkhealth.healthcabinui.device.SessionStatus
import com.lkhealth.healthcabinui.device.SourceType
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.CenteredContentCard
import com.lkhealth.healthcabinui.ui.components.MeasuringProgressRing
import com.lkhealth.healthcabinui.ui.components.PartStepper
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.theme.Spacing

/**
 * 对应旧系统 MeauingUC：收集 [com.lkhealth.healthcabinui.device.DeviceServiceApi.measure] 的进度流，
 * 依据 SessionStatus 分流到结果页/错误页/取消回准备页。
 */
@Composable
fun MeasuringScreen(deviceType: DeviceType, viewModel: AppViewModel) {
    val spec = remember(deviceType) { MeasurementCatalog.of(deviceType) }
    var progress by remember(deviceType) { mutableStateOf<SessionProgress?>(null) }
    var sessionId by remember(deviceType) { mutableStateOf<String?>(null) }

    LaunchedEffect(deviceType) {
        viewModel.speak("开始测量${spec.title}，请保持不动")
        var lastSpokenStep: String? = null
        viewModel.deviceService.measure(deviceType, SourceType.UI).collect { p ->
            progress = p
            sessionId = p.sessionId
            // 只在阶段文案变化时播报（例如血压的充气/测量/放气），避免每个进度百分比都重复念同一句话。
            if (p.status == SessionStatus.IN_PROGRESS && p.currentStep != null && p.currentStep != lastSpokenStep) {
                lastSpokenStep = p.currentStep
                viewModel.speak(p.currentStep)
            }
            when (p.status) {
                SessionStatus.COMPLETED -> {
                    val session = viewModel.deviceService.getActiveSession()
                    if (session != null) {
                        viewModel.onMeasurementCompleted(deviceType, session)
                    } else {
                        viewModel.onMeasurementFailed(deviceType, "未获取到测量结果，请重试")
                    }
                }
                SessionStatus.FAILED, SessionStatus.ERROR -> {
                    viewModel.onMeasurementFailed(deviceType, p.error?.message ?: "测量失败，请重试")
                }
                SessionStatus.CANCELLED -> {
                    viewModel.retryMeasurement(deviceType)
                }
                else -> Unit
            }
        }
    }

    val currentPartIndex = progress?.partIndex ?: 0

    CenteredContentCard {
        Spacer(Modifier.height(Spacing.l))
        Text("正在测量${spec.title}", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(Spacing.xxl))

        MeasuringProgressRing(percentage = progress?.percentage ?: 0)

        if (spec.isMultiPart) {
            Spacer(Modifier.height(Spacing.xl))
            PartStepper(
                parts = spec.parts,
                currentIndex = currentPartIndex,
                completedKeys = spec.parts.take(currentPartIndex).map { it.key }.toSet(),
            )
        }

        Spacer(Modifier.height(Spacing.l))
        Text(
            progress?.currentStep ?: "正在连接设备...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "请放松心情，谢谢！",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.xxl))
        SecondaryActionButton(
            text = "取消测量",
            onClick = {
                sessionId?.let { viewModel.deviceService.cancelMeasurement(it, SourceType.UI) }
            },
        )
    }
}
