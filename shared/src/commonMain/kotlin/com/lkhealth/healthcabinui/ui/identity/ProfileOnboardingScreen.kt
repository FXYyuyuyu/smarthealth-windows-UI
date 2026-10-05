package com.lkhealth.healthcabinui.ui.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.directory.IdentityMode
import com.lkhealth.healthcabinui.directory.NewUserDraft
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.CenteredContentCard
import com.lkhealth.healthcabinui.ui.components.PageHeader
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * 对应旧系统 CreateUserUC：用户查无档案时的建档表单。
 * 身份证模式自动解析性别/生日，需补手机号+姓名；手机号模式需补姓名+出生日期。
 */
@Composable
fun ProfileOnboardingScreen(uid: String, mode: IdentityMode, viewModel: AppViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(if (mode == IdentityMode.PHONE) uid else "") }
    var birthday by remember { mutableStateOf(if (mode == IdentityMode.ID_CARD) parseBirthdayFromIdCard(uid) else "") }
    var sex by remember { mutableStateOf(if (mode == IdentityMode.ID_CARD) parseSexFromIdCard(uid) else "") }
    var submitting by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() && phone.length == 11 && birthday.length == 10 && sex.isNotBlank()

    CenteredContentCard {
        PageHeader(
            title = "完善建档信息",
            subtitle = when (mode) {
                IdentityMode.ID_CARD -> "身份证号 $uid 查无档案，请补充以下信息完成建档"
                IdentityMode.PHONE -> "手机号 $uid 查无档案，请补充以下信息完成建档"
            },
        )
        Spacer(Modifier.height(Spacing.xl))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("姓名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.m))

        if (mode == IdentityMode.ID_CARD) {
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 11) phone = it },
                label = { Text("手机号") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.m))
            Text(
                "性别：$sex　生日：$birthday（已由身份证号自动识别）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            OutlinedTextField(
                value = birthday,
                onValueChange = { birthday = it },
                label = { Text("出生日期（YYYY-MM-DD）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.m))
            Text("性别", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                SexOption(label = "男", selected = sex == "男", onClick = { sex = "男" })
                SexOption(label = "女", selected = sex == "女", onClick = { sex = "女" })
            }
        }

        Spacer(Modifier.height(Spacing.xl))
        PrimaryActionButton(
            text = "提交建档",
            enabled = isValid,
            loading = submitting,
            modifier = Modifier.width(260.dp),
            onClick = {
                submitting = true
                coroutineScope.launch {
                    viewModel.submitOnboarding(NewUserDraft(uid, mode, name, phone, sex, birthday))
                }
            },
        )
        Spacer(Modifier.height(Spacing.m))
        SecondaryActionButton(text = "返回", onClick = viewModel::goBack, enabled = !submitting)
    }
}

@Composable
private fun SexOption(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        PrimaryActionButton(text = label, onClick = onClick, modifier = Modifier.width(96.dp))
    } else {
        SecondaryActionButton(text = label, onClick = onClick, modifier = Modifier.width(96.dp))
    }
}

private fun parseBirthdayFromIdCard(uid: String): String {
    if (uid.length != 18) return ""
    return "${uid.substring(6, 10)}-${uid.substring(10, 12)}-${uid.substring(12, 14)}"
}

private fun parseSexFromIdCard(uid: String): String {
    if (uid.length != 18) return ""
    val genderDigit = uid[16].digitToIntOrNull() ?: return ""
    return if (genderDigit % 2 == 1) "男" else "女"
}
