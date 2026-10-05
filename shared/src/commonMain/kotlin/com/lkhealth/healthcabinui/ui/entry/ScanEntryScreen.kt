package com.lkhealth.healthcabinui.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.CenteredContentCard
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlin.random.Random

/** 对应旧系统 BarCodeUC：模拟微信扫码，扫码成功后转入身份查询。 */
@Composable
fun ScanEntryScreen(viewModel: AppViewModel) {
    LaunchedEffect(Unit) {
        delay(1500)
        val mockScannedPhone = "158${Random.nextInt(10_000_000, 99_999_999)}"
        viewModel.identifyByUid(mockScannedPhone)
    }

    CenteredContentCard {
        Box(
            Modifier.size(200.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.QrCode2,
                contentDescription = null,
                modifier = Modifier.size(140.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(Spacing.l))
        Text("请使用微信扫描屏幕二维码", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(Spacing.s))
        Text(
            "扫码中，请稍候...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xl))
        SecondaryActionButton(text = "取消", onClick = viewModel::goBack)
    }
}
