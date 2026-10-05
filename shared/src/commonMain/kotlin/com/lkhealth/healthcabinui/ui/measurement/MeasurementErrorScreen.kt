package com.lkhealth.healthcabinui.ui.measurement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.device.MeasurementCatalog
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.CenteredContentCard
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing
import kotlinx.coroutines.delay

private const val AUTO_RETURN_MILLIS = 10_000L

/** 对应旧系统 MeauingErr：设备出错时的提示与重试/退出选择，10 秒无操作自动回到项目宫格。 */
@Composable
fun MeasurementErrorScreen(deviceType: DeviceType, message: String, viewModel: AppViewModel) {
    val spec = remember(deviceType) { MeasurementCatalog.of(deviceType) }
    val friendlyMessage = message.ifBlank { "设备连接失败，请联系工作人员" }

    LaunchedEffect(deviceType, friendlyMessage) {
        viewModel.speak("${spec.title}测量失败，$friendlyMessage")
        delay(AUTO_RETURN_MILLIS)
        viewModel.backToItemSelection(deviceType)
    }

    CenteredContentCard {
        Spacer(Modifier.height(Spacing.l))
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = HealthCabinColors.Error,
            modifier = Modifier.size(110.dp),
        )
        Spacer(Modifier.height(Spacing.xl))
        Text("${spec.title}测量失败", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(Spacing.m))
        Text(
            friendlyMessage,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xxl))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            SecondaryActionButton(
                text = "返回项目列表",
                onClick = { viewModel.backToItemSelection(deviceType) },
                modifier = Modifier.weight(1f),
            )
            PrimaryActionButton(
                text = "重新测量",
                onClick = { viewModel.retryMeasurement(deviceType) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
