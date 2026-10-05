package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.device.EvaluatedField
import com.lkhealth.healthcabinui.device.ResultPrompt
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing

/** 判定结果对应的前景色/底色。 */
private fun ResultPrompt.colors(): Pair<Color, Color> = when (this) {
    ResultPrompt.HIGH -> HealthCabinColors.Prompt.High to HealthCabinColors.Prompt.HighContainer
    ResultPrompt.LOW -> HealthCabinColors.Prompt.Low to HealthCabinColors.Prompt.LowContainer
    ResultPrompt.NORMAL -> HealthCabinColors.Prompt.Normal to HealthCabinColors.Prompt.NormalContainer
}

/** "偏高 / 偏低 / 正常"标签。箭头和文字一起出现，不让颜色成为唯一的状态线索。 */
@Composable
fun PromptBadge(prompt: ResultPrompt, modifier: Modifier = Modifier) {
    val (fg, bg) = prompt.colors()
    val glyph = when (prompt) {
        ResultPrompt.HIGH -> "↑ "
        ResultPrompt.LOW -> "↓ "
        ResultPrompt.NORMAL -> ""
    }
    Text(
        text = "$glyph${prompt.label}",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = modifier
            .background(bg, RoundedCornerShape(percent = 50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

/**
 * 一行结果：左边项目名，右边数值 + 单位 + 判定标签，下面一条参考范围轴。
 *
 * 旧系统只在打印报告里以"数值 + 参考值 90--139"的纯文字形式呈现，一体机前的用户看不到。
 * 这里把同一份数据搬到屏幕上，并且额外画一条轴——对着屏幕的多是中老年人，
 * 「152 比 90~139 的上限高出一点」比单看两个数字好理解得多。
 */
@Composable
fun ResultValueRow(
    evaluated: EvaluatedField,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 参考范围跟在项目名下面，而不是另起一整行——一屏要放下三四个指标加建议，省下的这几行很关键。
            Column {
                Text(
                    evaluated.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                evaluated.rangeText?.let { range ->
                    Text(
                        "参考范围 $range${evaluated.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    evaluated.display,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (evaluated.isAbnormal) evaluated.prompt.colors().first else MaterialTheme.colorScheme.onSurface,
                )
                if (evaluated.unit.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        evaluated.unit,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (evaluated.hasReference) {
                    Spacer(Modifier.width(Spacing.m))
                    PromptBadge(evaluated.prompt)
                }
            }
        }

        if (evaluated.hasReference) {
            Spacer(Modifier.height(Spacing.s))
            ReferenceRangeBar(evaluated, Modifier.fillMaxWidth().height(10.dp))
        }
    }
}

/**
 * 参考范围轴：整条是取值轴，中间浅绿一段是正常区间，圆点是本次测量值。
 *
 * 轴的两端在正常区间外各留 60% 区间宽度的余量，这样正常值的圆点落在中段、
 * 偏高偏低的圆点明显靠边。单侧不设限的指标（如高密度脂蛋白只有下限）
 * 把不设限的那一端当成轴的端点，正常区间一直铺到底。
 */
@Composable
private fun ReferenceRangeBar(
    evaluated: EvaluatedField,
    modifier: Modifier = Modifier,
) {
    val range = evaluated.range ?: return
    val value = evaluated.value ?: return
    val markerColor = evaluated.prompt.colors().first

    val (axisMin, axisMax) = when {
        range.hasLowerBound && range.hasUpperBound -> {
            val pad = (range.max - range.min) * 0.6
            (range.min - pad) to (range.max + pad)
        }
        range.hasUpperBound -> 0.0 to range.max * 1.6
        else -> range.min * 0.5 to range.min * 1.8
    }
    val axisSpan = (axisMax - axisMin).takeIf { it > 0 } ?: return

    fun fraction(v: Double) = ((v - axisMin) / axisSpan).coerceIn(0.0, 1.0).toFloat()

    val zoneStart = if (range.hasLowerBound) fraction(range.min) else 0f
    val zoneEnd = if (range.hasUpperBound) fraction(range.max) else 1f
    val markerFraction = fraction(value)

    Canvas(modifier) {
        val radius = size.height / 2f
        drawRoundRect(
            color = HealthCabinColors.Prompt.BarTrack,
            cornerRadius = CornerRadius(radius, radius),
        )
        drawRoundRect(
            color = HealthCabinColors.Prompt.BarNormalZone,
            topLeft = Offset(zoneStart * size.width, 0f),
            size = Size((zoneEnd - zoneStart) * size.width, size.height),
            cornerRadius = CornerRadius(radius, radius),
        )
        // 圆点比轴高一些，所以把圆心夹在左右各留一个半径的范围内，避免画到轴外被裁掉。
        val markerRadius = size.height * 0.9f
        val centerX = (markerFraction * size.width).coerceIn(markerRadius, size.width - markerRadius)
        drawCircle(color = Color.White, radius = markerRadius, center = Offset(centerX, radius))
        drawCircle(color = markerColor, radius = markerRadius * 0.62f, center = Offset(centerX, radius))
    }
}

/**
 * 健康建议卡片，对应旧系统 `PrintModel.ItemSuggest`（旧系统只打在纸质报告上）。
 *
 * 这是一段固定文案的生活方式建议，不是诊断；底部统一加一句引导就医的提示，
 * 异常值不在屏幕上给出结论性判断。
 */
@Composable
fun HealthAdviceCard(
    advices: List<String>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    if (advices.isEmpty()) return
    SectionCard(modifier = modifier.fillMaxWidth()) {
        Text(
            "健康建议",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HealthCabinColors.Prompt.High,
        )
        advices.forEach { advice ->
            Spacer(Modifier.height(Spacing.s))
            Text(
                // 每段建议的首行就是"高血压健康建议："这样的结论句，紧凑模式只保留这一句。
                if (compact) advice.substringBefore('\n').trimEnd('：', ':') else advice,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(Spacing.m))
        Text(
            if (compact) {
                "完整的饮食、运动建议请在\"查看结果\"的检测报告中查看。以上内容仅供参考，不能作为诊断依据。"
            } else {
                "以上建议仅供参考，不能作为诊断依据。指标异常请携带本次报告咨询医生。"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
