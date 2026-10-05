package com.lkhealth.healthcabinui.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.CenteredContentCard
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing

/** 对应旧系统 ErrUC：身份查询/建档等流程出错时的通用错误页。 */
@Composable
fun GenericErrorScreen(message: String, viewModel: AppViewModel) {
    LaunchedEffect(message) {
        viewModel.speak("出错了，$message")
    }

    CenteredContentCard {
        Icon(
            Icons.Filled.ReportProblem,
            contentDescription = null,
            tint = HealthCabinColors.Error,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(Spacing.l))
        Text("出错了", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(Spacing.s))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xxl))
        PrimaryActionButton(text = "返回欢迎页", onClick = viewModel::backToWelcome, modifier = Modifier.width(240.dp))
    }
}
