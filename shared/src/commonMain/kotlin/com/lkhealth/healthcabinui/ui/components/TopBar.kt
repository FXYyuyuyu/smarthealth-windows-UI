package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import kotlinx.coroutines.delay

/**
 * 常驻顶部栏：纯色底色 + logo/标题 + 空闲倒计时（紧跟标题右侧的纯数字，不带单位）+ 用户名（最右）。
 * logo 连续点击 8 次（2 秒内）弹出管理员密码框，复刻旧系统 LicenseServerEntry 的隐藏入口。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCabinTopBar(
    userLabel: String?,
    idleCountdownSeconds: Int?,
    onAdminUnlocked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tapCount by remember { mutableStateOf(0) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tapCount) {
        if (tapCount == 0) return@LaunchedEffect
        delay(2000)
        tapCount = 0
    }

    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HealthCabinColors.Primary),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalHospital,
                    contentDescription = "管理员入口",
                    tint = Color.White,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        tapCount += 1
                        if (tapCount >= 8) {
                            tapCount = 0
                            showPasswordDialog = true
                        }
                    },
                )
                Spacer(Modifier.width(10.dp))
                Text("健康小屋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)

                if (idleCountdownSeconds != null) {
                    Spacer(Modifier.width(24.dp))
                    val urgent = idleCountdownSeconds <= 10
                    val tint = if (urgent) Color(0xFFFFD3D3) else Color.White.copy(alpha = 0.9f)
                    // 旧系统就是纯数字，没有单位/图标（那是内部计时序号的展示习惯，这里沿用同样的极简展示）。
                    Text("$idleCountdownSeconds", style = MaterialTheme.typography.titleMedium, color = tint)
                }
            }
        },
        actions = {
            if (userLabel != null) {
                Text(
                    "用户：$userLabel",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
                Spacer(Modifier.width(20.dp))
            }
        },
    )

    if (showPasswordDialog) {
        AdminPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = {
                showPasswordDialog = false
                onAdminUnlocked()
            },
        )
    }
}

@Composable
private fun AdminPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("管理员验证") },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = false
                },
                label = { Text("请输入管理员密码") },
                singleLine = true,
                isError = error,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                supportingText = { if (error) Text("密码错误") },
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (password == "666666") onConfirm() else error = true
            }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
