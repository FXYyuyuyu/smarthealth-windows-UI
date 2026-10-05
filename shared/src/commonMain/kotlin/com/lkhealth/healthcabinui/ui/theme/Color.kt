package com.lkhealth.healthcabinui.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 低饱和医疗蓝 + 浅灰白辅助色。整体统一收敛在同一明度/纯度区间内，
 * 避免像早期原型那样用高饱和度的 Material 默认色，追求安静、专业的体检机质感。
 */
object HealthCabinColors {
    val Primary = Color(0xFF5B7C99)
    val PrimaryContainer = Color(0xFFE1E9F0)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF33495C)

    val Secondary = Color(0xFFEDEFF2)
    val OnSecondary = Color(0xFF4A5560)
    val SecondaryInk = Color(0xFF98A2AD)

    val Background = Color(0xFFF4F6F8)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFEEF1F4)
    val OnSurface = Color(0xFF2E3540)
    val OnSurfaceMuted = Color(0xFF7A8593)

    val Success = Color(0xFF4F9271)
    val Error = Color(0xFFC2666B)
    val Outline = Color(0xFFDADFE5)

    /**
     * 指标判定用色：偏高用暖色、偏低用冷色、正常用绿色。
     * 这三种颜色在界面上一律与"偏高/偏低/正常"文字标签同时出现，不单靠颜色传达状态——
     * 一体机用户里中老年人占多数，色觉衰退和红绿色弱的比例都不低。
     */
    object Prompt {
        val High = Color(0xFFB5705C)
        val HighContainer = Color(0xFFF6EAE4)
        val Low = Color(0xFF5878A0)
        val LowContainer = Color(0xFFE7EDF4)
        val Normal = Color(0xFF4F9271)
        val NormalContainer = Color(0xFFE6F0EA)
        /** 参考范围条的底色（整条轴）与正常区间底色。 */
        val BarTrack = Color(0xFFE8ECF0)
        val BarNormalZone = Color(0xFFCADFD2)
    }

    /** 每个检测项目的强调色，统一收敛在同一明度/纯度区间内、只在色相上区分。 */
    object ItemAccent {
        val HeightWeight = Color(0xFF4F8C93)
        val BloodPressure = Color(0xFFB06670)
        val BloodOxygen = Color(0xFF5478A0)
        val BloodGlucose = Color(0xFFB08850)
        val BodyFat = Color(0xFF5C9271)
        val Temperature = Color(0xFF8078A3)
        val WaistHip = Color(0xFF4F9088)
        val Ecg = Color(0xFFA2616F)
        val UricAcid = Color(0xFFA07D4F)
        val Cholesterol = Color(0xFF95738F)
        val BloodLipid = Color(0xFF7D80A8)
        val Hemoglobin = Color(0xFFBF7657)
        val Hba1c = Color(0xFF71879C)
        val Vision = Color(0xFF6A94AE)
        val Arteriosclerosis = Color(0xFFB0707A)
        val BoneDensity = Color(0xFF8C8F5E)
        val Breathing = Color(0xFF5E9DA0)
        val Urinalysis = Color(0xFFAD9257)
    }
}
