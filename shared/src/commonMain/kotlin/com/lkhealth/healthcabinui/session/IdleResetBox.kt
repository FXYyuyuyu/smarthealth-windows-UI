package com.lkhealth.healthcabinui.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/** [rememberIdleCountdown] 的返回值：剩余秒数（未启用计时时为 null）+ 上报交互的回调。 */
class IdleCountdownState(
    val remainingSeconds: Int?,
    val onInteraction: () -> Unit,
)

/**
 * 复刻旧系统"长时间无操作自动回到欢迎页"的空闲计时器，并把剩余秒数暴露给顶部栏显示。
 * [enabled] 为 false 时（测量中/打印中等硬件动作进行时）不计时，避免中途打断。
 */
@Composable
fun rememberIdleCountdown(
    enabled: Boolean,
    onIdle: () -> Unit,
    timeoutSeconds: Int = 120,
): IdleCountdownState {
    var interactionTick by remember { mutableStateOf(0) }
    var remainingSeconds by remember { mutableStateOf(timeoutSeconds) }

    LaunchedEffect(enabled, interactionTick) {
        if (!enabled) return@LaunchedEffect
        remainingSeconds = timeoutSeconds
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        }
        onIdle()
    }

    return IdleCountdownState(
        remainingSeconds = if (enabled) remainingSeconds else null,
        onInteraction = { interactionTick += 1 },
    )
}

/**
 * 监听真实的按下/点击手势并上报给 [onInteraction]，用于驱动 [rememberIdleCountdown] 的重置。
 *
 * 之前用 `awaitPointerEvent(PointerEventPass.Initial)` 监听"任意指针事件"，
 * 结果桌面端鼠标悬停移动也会触发重置（倒计时几乎回不到个位数），
 * Android 端则会被连续触发导致倒计时卡住不动。改成只在每次手势的"按下"这一刻触发一次，
 * 不会被悬停/惯性等非真实交互事件影响，两端表现一致。
 */
@Composable
fun IdleInteractionBox(
    onInteraction: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                onInteraction()
            }
        },
    ) {
        content()
    }
}
