package com.lkhealth.healthcabinui.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lkhealth.healthcabinui.device.MeasurementCatalog
import com.lkhealth.healthcabinui.device.format
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.PageHeader
import com.lkhealth.healthcabinui.ui.components.PrimaryActionButton
import com.lkhealth.healthcabinui.ui.components.ResultValueRow
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.components.SectionCard
import com.lkhealth.healthcabinui.ui.theme.Spacing

/** 对应旧系统 PrintUC：按分类汇总本次会话的所有检测结果，可发起打印。 */
@Composable
fun ReportSummaryScreen(viewModel: AppViewModel) {
    val completedResults by viewModel.completedResults.collectAsStateWithLifecycle()
    val printEnabled by viewModel.printEnabled.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val grouped = remember(completedResults) {
        val order = MeasurementCatalog.specs.map { it.deviceType }
        completedResults.keys
            .sortedBy { order.indexOf(it) }
            .groupBy { MeasurementCatalog.of(it).category }
    }

    Column(Modifier.fillMaxSize().padding(Spacing.xxl)) {
        PageHeader(
            title = "检测报告",
            subtitle = "${currentUser?.name ?: "游客"} · 本次共完成 ${completedResults.size} 项检测",
        )
        Spacer(Modifier.height(Spacing.l))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (grouped.isEmpty()) {
                Text(
                    "暂无已完成的检测项目",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            grouped.forEach { (category, deviceTypes) ->
                Text(category.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Spacing.s))
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    deviceTypes.forEachIndexed { index, deviceType ->
                        val spec = MeasurementCatalog.of(deviceType)
                        val session = completedResults[deviceType]
                        Text(spec.gridLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(Spacing.xs))
                        spec.resultFields.forEach { field ->
                            ResultValueRow(label = field.label, value = field.format(session), unit = field.unit)
                        }
                        if (index != deviceTypes.lastIndex) {
                            Spacer(Modifier.height(Spacing.m))
                            HorizontalDivider()
                            Spacer(Modifier.height(Spacing.m))
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.l))
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SecondaryActionButton(text = "返回", onClick = viewModel::backToItemSelection, modifier = Modifier.weight(1f))
            PrimaryActionButton(
                text = "发起打印",
                enabled = printEnabled && completedResults.isNotEmpty(),
                onClick = viewModel::startPrinting,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
