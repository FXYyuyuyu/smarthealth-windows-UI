package com.lkhealth.healthcabinui.device

/** 单项指标的判定结果，对应旧系统 `PromptEnum`（nor / high / low）。 */
enum class ResultPrompt(val label: String) {
    NORMAL("正常"),
    HIGH("偏高"),
    LOW("偏低"),
}

/**
 * 参考范围，对应旧系统 `PrintModel` 的 `minRange` / `maxRange`。
 *
 * 旧系统用 `0` 表示"该侧不设限"——例如高密度脂蛋白只有下限（`highest = 0`）、
 * 腰臀比只有上限（`lowest = 0`）。这里保留同一约定，判定逻辑见 [evaluate]。
 */
data class ReferenceRange(val min: Double, val max: Double) {

    val hasUpperBound: Boolean get() = max > 0
    val hasLowerBound: Boolean get() = min > 0

    /**
     * 完全沿用旧系统 `ItemPromptHelp.IsItemToPrompt` 的判定语义，包括它的两个边界约定：
     * 只有上限为正数时才可能判"偏高"（上限 0 代表不设上限），
     * 只有测量值为正数时才可能判"偏低"（0 视为没测到，而不是偏低）。
     */
    fun evaluate(value: Double): ResultPrompt = when {
        value > max && max > 0 -> ResultPrompt.HIGH
        value < min && value > 0 -> ResultPrompt.LOW
        else -> ResultPrompt.NORMAL
    }
}

/**
 * 求参考范围需要的上下文。旧系统各 `*Result.cs` 直接从缓存里读当前用户的性别/出生日期，
 * 体重的参考范围还要用到同一次测量里的身高，所以这里把这几样一起传进来。
 */
data class RangeContext(
    val sex: String?,
    val age: Int?,
    /** 本次测量已得到的数值，供依赖其他字段的范围使用（目前只有体重依赖身高）。 */
    val values: Map<String, Double> = emptyMap(),
) {
    val isMale: Boolean get() = sex == "男"
}

/**
 * 各检测项目的参考范围表，逐条对应旧系统 `Core/HealthCabin.PrintPro/ResultMethod/ 下各 Result.cs`
 * 里硬编码的 `highest` / `lowest`。
 *
 * 旧系统没给出数值范围的项目（心电、视力、骨密度、肺功能，以及动脉硬化的 AVI——
 * 它的阈值来自设备自身配置，UI 层拿不到）这里一律返回 `null`，页面就只显示数值、不显示判定，
 * 刻意不在 UI 层自行发明医学阈值。
 */
object ReferenceRanges {

    fun of(deviceType: DeviceType, fieldKey: String, ctx: RangeContext): ReferenceRange? =
        when (deviceType) {
            DeviceType.HEIGHT_WEIGHT -> when (fieldKey) {
                // 旧系统 HWResult.weightRange：标准体重 (身高 - 105) 的 ±10%，身高缺失时不判定。
                "weight" -> ctx.values["height"]
                    ?.takeIf { it > 0 }
                    ?.let { height -> ReferenceRange((height - 105) * 0.9, (height - 105) * 1.1) }
                "bmi" -> ReferenceRange(18.5, 23.9)
                else -> null
            }

            DeviceType.BLOOD_PRESSURE -> when (fieldKey) {
                "systolic" -> ReferenceRange(90.0, 139.0)
                "diastolic" -> ReferenceRange(60.0, 89.0)
                "pulse" -> ReferenceRange(60.0, 100.0)
                else -> null
            }

            DeviceType.BLOOD_OXYGEN -> when (fieldKey) {
                "spo2" -> ReferenceRange(95.0, 100.0)
                "pulse" -> ReferenceRange(60.0, 100.0)
                else -> null
            }

            // 旧系统 GLUResult 按"餐后小时数"分空腹 3.9–6.1、餐后 1 小时 3.9–11.1、餐后 2 小时及以上 3.9–7.8
            // 三档。当前 UI 还没有采集餐后小时数的入口，先按空腹档（也是最严的一档）判定。
            DeviceType.BLOOD_GLUCOSE -> when (fieldKey) {
                "glucose" -> ReferenceRange(3.9, 6.1)
                else -> null
            }

            DeviceType.BODY_FAT -> when (fieldKey) {
                "bodyFatPercent" -> bodyFatRange(ctx)
                "bmr" -> bmrRange(ctx)
                else -> null
            }

            DeviceType.TEMPERATURE -> when (fieldKey) {
                "temperature" -> ReferenceRange(35.8, 37.8)
                else -> null
            }

            // 旧系统 WHRResult 只给腰臀比设上限，腰围/臀围本身不判定。
            DeviceType.WAIST_HIP -> when (fieldKey) {
                "waistHipRatio" -> if (ctx.isMale) ReferenceRange(0.0, 0.9) else ReferenceRange(0.0, 0.85)
                else -> null
            }

            DeviceType.URIC_ACID -> when (fieldKey) {
                "uricAcid" -> if (ctx.isMale) ReferenceRange(0.149, 0.416) else ReferenceRange(0.089, 0.357)
                else -> null
            }

            DeviceType.CHOLESTEROL -> when (fieldKey) {
                "chol" -> ReferenceRange(0.0, 5.18)
                else -> null
            }

            DeviceType.BLOOD_LIPID -> when (fieldKey) {
                "chol" -> ReferenceRange(0.0, 5.18)
                "tg" -> ReferenceRange(0.0, 1.7)
                // 高密度脂蛋白越高越好，旧系统 BLResult.hdlRange 把上限留成 0，只判"偏低"。
                "hdl" -> ReferenceRange(1.04, 0.0)
                "ldl" -> ReferenceRange(0.0, 3.37)
                else -> null
            }

            DeviceType.HEMOGLOBIN -> when (fieldKey) {
                "hb" -> if (ctx.isMale) ReferenceRange(12.0, 16.0) else ReferenceRange(11.0, 15.0)
                else -> null
            }

            DeviceType.HBA1C -> when (fieldKey) {
                "ngsp" -> ReferenceRange(4.0, 6.5)
                else -> null
            }

            DeviceType.ARTERIOSCLEROSIS -> when (fieldKey) {
                "vpSystolic" -> ReferenceRange(90.0, 139.0)
                "vpDiastolic" -> ReferenceRange(60.0, 89.0)
                "vpPulse" -> ReferenceRange(60.0, 100.0)
                else -> null
            }

            DeviceType.URINALYSIS -> when (fieldKey) {
                "ph" -> ReferenceRange(5.0, 8.5)
                "sg" -> ReferenceRange(1.000, 1.030)
                else -> null
            }

            DeviceType.ECG,
            DeviceType.VISION,
            DeviceType.BONE_DENSITY,
            DeviceType.BREATHING,
            DeviceType.CAMERA -> null
        }

    /** 旧系统 FATResult.fatRange：体脂率按年龄段 + 性别分档。 */
    private fun bodyFatRange(ctx: RangeContext): ReferenceRange? {
        val age = ctx.age ?: return null
        return when {
            age in 18..39 -> if (ctx.isMale) ReferenceRange(18.0, 23.0) else ReferenceRange(21.0, 25.0)
            age in 40..55 -> if (ctx.isMale) ReferenceRange(19.0, 24.0) else ReferenceRange(22.0, 26.0)
            age >= 56 -> if (ctx.isMale) ReferenceRange(20.0, 25.0) else ReferenceRange(23.0, 27.0)
            else -> null
        }
    }

    /** 旧系统 FATResult.bmrRange：基础代谢率按年龄段 + 性别分档。 */
    private fun bmrRange(ctx: RangeContext): ReferenceRange? {
        val age = ctx.age ?: return null
        return when {
            age in 18..29 -> if (ctx.isMale) ReferenceRange(1395.0, 1705.0) else ReferenceRange(1089.0, 1331.0)
            age in 30..49 -> if (ctx.isMale) ReferenceRange(1350.0, 1650.0) else ReferenceRange(1053.0, 1287.0)
            age in 50..69 -> if (ctx.isMale) ReferenceRange(1215.0, 1485.0) else ReferenceRange(999.0, 1221.0)
            age >= 70 -> if (ctx.isMale) ReferenceRange(1098.0, 1342.0) else ReferenceRange(909.0, 1111.0)
            else -> null
        }
    }
}
