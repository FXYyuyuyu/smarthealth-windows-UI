package com.lkhealth.healthcabinui.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HealthCabinColorScheme = lightColorScheme(
    primary = HealthCabinColors.Primary,
    onPrimary = HealthCabinColors.OnPrimary,
    primaryContainer = HealthCabinColors.PrimaryContainer,
    onPrimaryContainer = HealthCabinColors.OnPrimaryContainer,
    secondary = HealthCabinColors.Secondary,
    onSecondary = HealthCabinColors.OnSecondary,
    secondaryContainer = HealthCabinColors.Secondary,
    onSecondaryContainer = HealthCabinColors.OnSecondary,
    background = HealthCabinColors.Background,
    onBackground = HealthCabinColors.OnSurface,
    surface = HealthCabinColors.Surface,
    onSurface = HealthCabinColors.OnSurface,
    surfaceVariant = HealthCabinColors.SurfaceVariant,
    onSurfaceVariant = HealthCabinColors.OnSurfaceMuted,
    error = HealthCabinColors.Error,
    outline = HealthCabinColors.Outline,
)

@Composable
fun HealthCabinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HealthCabinColorScheme,
        typography = healthCabinTypography(),
        shapes = HealthCabinShapes,
        content = content,
    )
}
