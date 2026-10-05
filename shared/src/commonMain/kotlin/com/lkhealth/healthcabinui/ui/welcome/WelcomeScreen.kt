package com.lkhealth.healthcabinui.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.rememberPressDarken
import com.lkhealth.healthcabinui.ui.components.rememberPressScale
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing
import healthcabinui.shared.generated.resources.Res
import healthcabinui.shared.generated.resources.welcome_nurse
import org.jetbrains.compose.resources.painterResource

private const val MOCK_ID_CARD_UID = "110101199001011234"

/** 对应旧系统 WelcomeUC：护士形象+身份证插画直接复用原图（`welcome_nurse.png`，图内已含欢迎语与操作提示）。 */
@Composable
fun WelcomeScreen(viewModel: AppViewModel) {
    val faceLoginEnabled by viewModel.faceLoginEnabled.collectAsStateWithLifecycle()
    val guestModeEnabled by viewModel.guestModeEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.speak("欢迎使用健康小屋，请刷卡或选择登录方式开始检测")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxl, vertical = Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 宽度对齐下方四张入口卡片整行的宽度（4×160dp + 3×24dp 间距 = 712dp）。
        Image(
            painter = painterResource(Res.drawable.welcome_nurse),
            contentDescription = "欢迎您测量，请将身份证对准读卡区或点击输入身份证号/手机号",
            modifier = Modifier.width(712.dp),
        )
        Spacer(Modifier.height(Spacing.l))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
            EntryTile(
                icon = Icons.Filled.CreditCard,
                label = "模拟刷身份证",
                accent = HealthCabinColors.Primary,
                onClick = { viewModel.identifyByUid(MOCK_ID_CARD_UID) },
            )
            EntryTile(
                icon = Icons.Filled.Dialpad,
                label = "手动输入",
                accent = HealthCabinColors.ItemAccent.BloodOxygen,
                onClick = viewModel::startManualEntry,
            )
            if (faceLoginEnabled) {
                EntryTile(
                    icon = Icons.Filled.Face,
                    label = "人脸登录",
                    accent = HealthCabinColors.ItemAccent.Temperature,
                    onClick = viewModel::startFaceLogin,
                )
            }
            EntryTile(
                icon = Icons.Filled.QrCodeScanner,
                label = "微信扫码",
                accent = HealthCabinColors.ItemAccent.BodyFat,
                onClick = viewModel::startScan,
            )
        }

        Spacer(Modifier.height(Spacing.l))

        if (guestModeEnabled) {
            TextButton(onClick = viewModel::startGuest) {
                Icon(Icons.Filled.PersonOutline, contentDescription = null)
                Spacer(Modifier.width(Spacing.xs))
                Text("游客体验", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun EntryTile(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interactionSource)
    val containerColor by rememberPressDarken(interactionSource, MaterialTheme.colorScheme.surface, darkenFactor = 0.05f)

    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(160.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(60.dp).background(accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(Spacing.m))
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
