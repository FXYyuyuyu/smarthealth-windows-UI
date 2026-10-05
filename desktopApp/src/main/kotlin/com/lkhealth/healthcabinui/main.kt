package com.lkhealth.healthcabinui

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/**
 * 一体机默认以无边框全屏（kiosk）方式运行，与旧系统主窗口铺满主屏一致；
 * 开发调试时传 `-Dhealthcabin.windowed=true`（`gradlew :desktopApp:run` 已自动带上）切回 1280×800 窗口。
 * 全屏模式下可用 Alt+F4 退出。
 */
fun main() = application {
    val windowed = System.getProperty("healthcabin.windowed")?.toBoolean() == true
    val windowState = rememberWindowState(
        placement = if (windowed) WindowPlacement.Floating else WindowPlacement.Fullscreen,
        position = WindowPosition(alignment = Alignment.Center),
        size = DpSize(1280.dp, 800.dp),
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "健康小屋",
        state = windowState,
        undecorated = !windowed,
        resizable = windowed,
    ) {
        App()
    }
}
