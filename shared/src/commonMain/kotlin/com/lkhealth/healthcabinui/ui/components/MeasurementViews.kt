package com.lkhealth.healthcabinui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.device.MeasurementPart
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing

@Composable
fun MeasuringProgressRing(
    percentage: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier.size(220.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.size(220.dp),
            strokeWidth = 12.dp,
        )
        Text("$percentage%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
    }
}

/** 分段测量的步骤指示器，例如腰臀比先腰围后臀围。 */
@Composable
fun PartStepper(
    parts: List<MeasurementPart>,
    currentIndex: Int,
    completedKeys: Set<String>,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xl)) {
        parts.forEachIndexed { index, part ->
            val isActive = index == currentIndex
            val isDone = part.key in completedKeys
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(40.dp).background(
                        when {
                            isDone -> HealthCabinColors.Success
                            isActive -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        CircleShape,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isDone) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                    } else {
                        Text(
                            "${index + 1}",
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.xs))
                Text(part.label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
