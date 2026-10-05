package com.lkhealth.healthcabinui.device

import com.lkhealth.healthcabinui.directory.UserProfile
import kotlin.math.roundToInt

/** 按字段配置的小数位数格式化数值；未取到值时显示占位符。 */
fun ResultField.format(session: DeviceServiceSession?): String {
    val raw = session?.values?.get(key) ?: return "--"
    return formatValue(raw)
}

private fun ResultField.formatValue(raw: Double): String =
    if (decimals <= 0) raw.roundToInt().toString() else "%.${decimals}f".format(raw)

/**
 * 一个结果字段连同它的参考范围判定，供结果页/报告页直接渲染。
 *
 * [prompt] 在没有参考范围、或这次没测到数值时一律是 [ResultPrompt.NORMAL]，
 * 配合 [hasReference] 判断是"确认正常"还是"本来就不判定"——
 * 旧系统对心电、视力、骨密度、肺功能这些项目就是只打印数值、不打印参考值。
 */
data class EvaluatedField(
    /** 字段定义。刻意不叫 `field`——那个名字在属性 getter 里是指向 backing field 的保留标识符。 */
    val definition: ResultField,
    val value: Double?,
    val display: String,
    val range: ReferenceRange?,
    val prompt: ResultPrompt,
    val advice: String?,
) {
    val label: String get() = definition.label
    val unit: String get() = definition.unit
    val hasReference: Boolean get() = range != null && value != null
    val isAbnormal: Boolean get() = prompt != ResultPrompt.NORMAL

    /** 参考范围的展示文案，单侧不设限时写成 `≤x` / `≥x`，对应旧系统把不设限的那一侧存成 0 的做法。 */
    val rangeText: String?
        get() {
            val r = range ?: return null
            return when {
                r.hasLowerBound && r.hasUpperBound -> "${definition.formatValue(r.min)} ~ ${definition.formatValue(r.max)}"
                r.hasUpperBound -> "≤ ${definition.formatValue(r.max)}"
                r.hasLowerBound -> "≥ ${definition.formatValue(r.min)}"
                else -> null
            }
        }
}

/**
 * 对本次测量结果逐字段套用参考范围，得到可直接渲染的列表。
 *
 * 参考范围要用到当前用户的性别和年龄（体脂率、基础代谢、尿酸、血红蛋白、腰臀比都分性别/年龄档），
 * 游客模式下 [profile] 为 null，这些项目就取不到范围、只显示数值——与旧系统"没建档就没有判定依据"一致。
 */
fun MeasurementSpec.evaluate(
    session: DeviceServiceSession?,
    profile: UserProfile?,
): List<EvaluatedField> {
    val values = session?.values ?: emptyMap()
    val ctx = RangeContext(sex = profile?.sex, age = profile?.age, values = values)
    return resultFields.map { field ->
        val value = values[field.key]
        val range = ReferenceRanges.of(deviceType, field.key, ctx)
        val prompt = if (value != null && range != null) range.evaluate(value) else ResultPrompt.NORMAL
        EvaluatedField(
            definition = field,
            value = value,
            display = field.format(session),
            range = range,
            prompt = prompt,
            advice = HealthAdvice.of(deviceType, field.key, prompt, value),
        )
    }
}

/** 本次测量里所有异常项要显示的建议，按文案去重——血压的收缩压/舒张压共用同一段建议。 */
fun List<EvaluatedField>.advices(): List<String> =
    mapNotNull { it.advice }.distinct()
