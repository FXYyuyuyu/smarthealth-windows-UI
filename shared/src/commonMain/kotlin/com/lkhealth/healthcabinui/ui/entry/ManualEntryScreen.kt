package com.lkhealth.healthcabinui.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.CenteredContentCard
import com.lkhealth.healthcabinui.ui.components.NumericKeypad
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing

/**
 * 对应旧系统 InputUC：蓝色面板里"标签+输入框"在上、数字键盘在下，
 * "确定/取消"放在面板外面——和参考截图里的弹窗式布局保持一致。
 */
@Composable
fun ManualEntryScreen(viewModel: AppViewModel) {
    var input by remember { mutableStateOf("") }
    val isValid = input.length == 11 || input.length == 18

    CenteredContentCard(maxWidth = 620.dp) {
        Surface(
            color = HealthCabinColors.Primary,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(Spacing.xl)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "身份证/手机号码：",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Spacer(Modifier.width(Spacing.m))
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) {
                        Box(Modifier.padding(horizontal = Spacing.m), contentAlignment = Alignment.CenterStart) {
                            Text(
                                input.ifEmpty { " " },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.l))
                NumericKeypad(value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(Spacing.l))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            PrimaryActionButton(
                text = "确定",
                enabled = isValid,
                onClick = { viewModel.identifyByUid(input) },
                modifier = Modifier.weight(1f),
            )
            SecondaryActionButton(text = "取消", onClick = viewModel::goBack, modifier = Modifier.weight(1f))
        }
    }
}
