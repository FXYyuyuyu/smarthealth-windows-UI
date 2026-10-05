package com.lkhealth.healthcabinui.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.PageHeader
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.components.SectionCard
import com.lkhealth.healthcabinui.ui.theme.Spacing

/** 对应旧系统 SetDeviceUC/SetModeUC：开关型设置合并到一处，关机/重启在示例环境中保留入口但不执行真实操作。 */
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val printEnabled by viewModel.printEnabled.collectAsStateWithLifecycle()
    val faceLoginEnabled by viewModel.faceLoginEnabled.collectAsStateWithLifecycle()
    val guestModeEnabled by viewModel.guestModeEnabled.collectAsStateWithLifecycle()
    val voiceEnabled by viewModel.voiceEnabled.collectAsStateWithLifecycle()
    var showPowerNotice by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(Spacing.xxl)) {
        PageHeader(title = "系统设置", subtitle = "设备参数由底层服务提供，这里是前端可切换的模式开关")
        Spacer(Modifier.height(Spacing.l))

        // 开关项可能超出较矮窗口的高度，单独滚动，确保底部"返回"按钮始终可见可点。
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                SettingToggleRow("打印功能", "检测报告页展示\"发起打印\"按钮", printEnabled, viewModel::setPrintEnabled)
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.m))
                SettingToggleRow("语音播报", "在测量/结果等关键步骤播放语音指导", voiceEnabled, viewModel::setVoiceEnabled)
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.m))
                SettingToggleRow("人脸登录入口", "欢迎页展示\"人脸登录\"入口", faceLoginEnabled, viewModel::setFaceLoginEnabled)
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.m))
                SettingToggleRow("游客体验入口", "欢迎页展示\"游客体验\"入口", guestModeEnabled, viewModel::setGuestModeEnabled)
            }

            Spacer(Modifier.height(Spacing.l))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                SecondaryActionButton(text = "关机", onClick = { showPowerNotice = true })
                SecondaryActionButton(text = "重启", onClick = { showPowerNotice = true })
            }
        }

        Spacer(Modifier.height(Spacing.l))
        PrimaryActionButton(text = "返回", onClick = viewModel::goBack, modifier = Modifier.fillMaxWidth())
    }

    if (showPowerNotice) {
        AlertDialog(
            onDismissRequest = { showPowerNotice = false },
            title = { Text("暂不可用") },
            text = { Text("关机/重启需要接入真实设备服务后才能生效，当前为 UI 演示环境。") },
            confirmButton = {
                TextButton(onClick = { showPowerNotice = false }) { Text("知道了") }
            },
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
