package com.lkhealth.healthcabinui.ui.identity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.PageHeader
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.components.rememberPressScale
import com.lkhealth.healthcabinui.ui.theme.Spacing

/**
 * 年龄段。档位边界取自参考范围里两套分档的并集——
 * 体脂率按 18-39 / 40-55 / 56+ 分，基础代谢按 18-29 / 30-49 / 50-69 / 70+ 分，
 * 合起来正好是下面这六档。[representativeAge] 是传给判定逻辑的代表年龄，
 * 取值保证在两套分档里都落进对应的那一档。
 */
private data class AgeBand(val label: String, val representativeAge: Int)

private val AgeBands = listOf(
    AgeBand("18-29 岁", 25),
    AgeBand("30-39 岁", 35),
    AgeBand("40-49 岁", 45),
    AgeBand("50-55 岁", 52),
    AgeBand("56-69 岁", 62),
    AgeBand("70 岁以上", 75),
)

/**
 * 游客模式补填性别 / 年龄段。
 *
 * 做成整屏的一步，而不是弹窗：这一屏其余页面全是整屏向导式的，弹窗在一体机的大屏上既显得小、
 * 按钮也小，老人还容易点到遮罩把它关掉。整屏可以把六个年龄段都做成大按钮，一次点两下就走完。
 *
 * 允许跳过——只是没有性别年龄的话，分性别/年龄档的那几项只显示数值、不给"偏高/偏低"。
 */
@Composable
fun GuestProfileScreen(
    viewModel: AppViewModel,
    /** 预置选中项，只给截图测试用来渲染"已选中"的样子；正常进入这一页时两项都是空的。 */
    initialSex: String? = null,
    initialAgeBandIndex: Int? = null,
) {
    var sex by remember { mutableStateOf(initialSex) }
    var band by remember { mutableStateOf(initialAgeBandIndex?.let(AgeBands::getOrNull)) }

    LaunchedEffect(Unit) {
        viewModel.speak("请选择您的性别和年龄段，以便给出参考范围")
    }

    Box(Modifier.fillMaxSize().padding(horizontal = Spacing.xxl, vertical = Spacing.xl), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 980.dp).fillMaxSize()) {
            PageHeader(
                title = "请选择性别和年龄段",
                subtitle = "体脂率、基础代谢等项目的参考范围按性别和年龄区分，填写后才能判断是否偏高偏低",
            )
            Spacer(Modifier.height(Spacing.xl))

            Text("性别", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Spacing.m))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                listOf("男", "女").forEach { option ->
                    ChoiceButton(
                        text = option,
                        selected = sex == option,
                        onClick = { sex = option },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))
            Text("年龄段", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Spacing.m))
            AgeBands.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    row.forEach { option ->
                        ChoiceButton(
                            text = option.label,
                            selected = band == option,
                            onClick = { band = option },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.m))
            }

            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                SecondaryActionButton(
                    text = "跳过",
                    onClick = { viewModel.enterAsGuest(null, null) },
                    modifier = Modifier.weight(1f),
                )
                PrimaryActionButton(
                    text = "开始检测",
                    enabled = sex != null && band != null,
                    onClick = { viewModel.enterAsGuest(sex, band?.representativeAge) },
                    modifier = Modifier.weight(2f),
                )
            }
        }
    }
}

/** 大号单选按钮：选中用描边加粗 + 底色变化 + 文字加粗三重表示，不只靠颜色。 */
@Composable
private fun ChoiceButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interactionSource)

    Surface(
        modifier = modifier
            .height(80.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 3.dp else 1.5.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}
