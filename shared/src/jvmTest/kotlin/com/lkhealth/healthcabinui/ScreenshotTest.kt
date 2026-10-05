package com.lkhealth.healthcabinui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import com.lkhealth.healthcabinui.device.DeviceServiceSession
import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.device.MockDeviceServiceApi
import com.lkhealth.healthcabinui.directory.MockUserDirectoryApi
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.HealthCabinTopBar
import com.lkhealth.healthcabinui.ui.items.ItemSelectionScreen
import com.lkhealth.healthcabinui.ui.theme.HealthCabinTheme
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test

/**
 * 按一体机常见分辨率离屏渲染关键页面并输出 PNG（build/screenshots），用于肉眼检查裁切、溢出、错位。
 * 目前只落盘不做像素比对；后续可在此基础上加入基准图对比做回归。
 */
class ScreenshotTest {

    private val resolutions = listOf(1280 to 800, 1024 to 768, 1366 to 768)

    @Test
    fun itemSelection() {
        val viewModel = AppViewModel(MockDeviceServiceApi(), MockUserDirectoryApi()).apply {
            startGuest()
            onMeasurementCompleted(DeviceType.BLOOD_PRESSURE, DeviceServiceSession("s", DeviceType.BLOOD_PRESSURE, emptyMap()))
        }
        resolutions.forEach { (w, h) ->
            capture("item_selection_${w}x$h", w, h) {
                KioskFrame(userLabel = "游客体验") { ItemSelectionScreen(viewModel) }
            }
        }
    }

    @Composable
    private fun KioskFrame(userLabel: String?, content: @Composable () -> Unit) {
        HealthCabinTheme {
            Scaffold(topBar = { HealthCabinTopBar(userLabel = userLabel, idleCountdownSeconds = 115, onAdminUnlocked = {}) }) { padding ->
                Surface(Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
                    content()
                }
            }
        }
    }

    private fun capture(name: String, width: Int, height: Int, content: @Composable () -> Unit) {
        ImageComposeScene(width, height, density = Density(1f), content = content).use { scene ->
            // 多渲染几帧，让字体/图片资源加载完成、布局稳定。
            repeat(5) { scene.render(it * 16_000_000L) }
            val bytes = scene.render(100_000_000L).encodeToData(EncodedImageFormat.PNG)!!.bytes
            File("build/screenshots").apply { mkdirs() }.resolve("$name.png").writeBytes(bytes)
        }
    }
}
