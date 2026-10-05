package com.lkhealth.healthcabinui.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * 统一的"轻微下压 + 变暗"点击反馈：按下时缩小到 [pressedScale]、颜色略微变暗，
 * 松开时柔和回弹——不使用放大/弹跳效果，动画时长短且平滑，避免生硬的跳变。
 */
@Composable
fun rememberPressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.97f,
): State<Float> {
    val isPressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressScale",
    )
}

@Composable
fun rememberPressDarken(
    interactionSource: InteractionSource,
    baseColor: Color,
    darkenFactor: Float = 0.12f,
): State<Color> {
    val isPressed by interactionSource.collectIsPressedAsState()
    val darkened = lerp(baseColor, Color.Black, darkenFactor)
    return animateColorAsState(
        targetValue = if (isPressed) darkened else baseColor,
        animationSpec = tween(durationMillis = 120),
        label = "pressDarken",
    )
}
