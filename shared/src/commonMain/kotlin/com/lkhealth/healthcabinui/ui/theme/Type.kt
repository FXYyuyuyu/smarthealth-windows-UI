package com.lkhealth.healthcabinui.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import healthcabinui.shared.generated.resources.Res
import healthcabinui.shared.generated.resources.noto_sans_sc_bold
import healthcabinui.shared.generated.resources.noto_sans_sc_medium
import healthcabinui.shared.generated.resources.noto_sans_sc_regular
import org.jetbrains.compose.resources.Font

/**
 * 随程序打包 Noto Sans SC（GB2312 子集 + 项目文案用字），保证桌面端与各厂商安卓设备字形、字宽一致；
 * 子集外的生僻字（如少数用户姓名）由系统字体自动回落。SemiBold 会就近匹配到 Bold。
 */
@Composable
private fun notoSansSc(): FontFamily = FontFamily(
    Font(Res.font.noto_sans_sc_regular, FontWeight.Normal),
    Font(Res.font.noto_sans_sc_medium, FontWeight.Medium),
    Font(Res.font.noto_sans_sc_bold, FontWeight.Bold),
)

/** 触屏一体机通常有一定观看距离，字号整体比默认 Material 类型阶梯更大。 */
@Composable
fun healthCabinTypography(): Typography {
    val family = notoSansSc()
    return remember(family) {
        fun style(size: Int, weight: FontWeight, line: Int) =
            TextStyle(fontFamily = family, fontSize = size.sp, fontWeight = weight, lineHeight = line.sp)
        Typography(
            displayMedium = style(44, FontWeight.Bold, 52),
            headlineLarge = style(32, FontWeight.Bold, 40),
            headlineMedium = style(26, FontWeight.SemiBold, 34),
            headlineSmall = style(22, FontWeight.SemiBold, 30),
            titleLarge = style(22, FontWeight.SemiBold, 28),
            titleMedium = style(18, FontWeight.Medium, 24),
            bodyLarge = style(18, FontWeight.Normal, 26),
            bodyMedium = style(16, FontWeight.Normal, 22),
            bodySmall = style(14, FontWeight.Normal, 20),
            labelLarge = style(16, FontWeight.Medium, 20),
            labelMedium = style(14, FontWeight.Medium, 18),
        )
    }
}
