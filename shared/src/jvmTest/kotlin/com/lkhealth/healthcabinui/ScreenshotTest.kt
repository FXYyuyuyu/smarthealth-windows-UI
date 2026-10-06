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
import com.lkhealth.healthcabinui.ui.identity.GuestProfileScreen
import com.lkhealth.healthcabinui.ui.items.ItemSelectionScreen
import com.lkhealth.healthcabinui.ui.measurement.PrepareScreen
import com.lkhealth.healthcabinui.ui.measurement.ResultScreen
import com.lkhealth.healthcabinui.ui.report.ReportSummaryScreen
import com.lkhealth.healthcabinui.ui.theme.HealthCabinTheme
import com.lkhealth.healthcabinui.ui.welcome.WelcomeScreen
import kotlinx.coroutines.runBlocking
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

    /**
     * 结果页/报告页：带参考范围判定与健康建议的状态。用 Mock 目录里的"张三（男，36 岁）"登录，
     * 因为体脂率、基础代谢这些指标的参考范围要分性别/年龄档，游客模式下取不到范围。
     */
    @Test
    fun resultsWithReferenceRanges() {
        val viewModel = AppViewModel(MockDeviceServiceApi(), MockUserDirectoryApi()).apply {
            runBlocking { performLookup("110101199001011234") }
            onMeasurementCompleted(
                DeviceType.BLOOD_PRESSURE,
                DeviceServiceSession("s1", DeviceType.BLOOD_PRESSURE, mapOf("systolic" to 152.0, "diastolic" to 95.0, "pulse" to 72.0)),
            )
            onMeasurementCompleted(
                DeviceType.HEIGHT_WEIGHT,
                DeviceServiceSession("s2", DeviceType.HEIGHT_WEIGHT, mapOf("height" to 172.0, "weight" to 85.0, "bmi" to 28.7)),
            )
            onMeasurementCompleted(
                DeviceType.BODY_FAT,
                DeviceServiceSession("s3", DeviceType.BODY_FAT, mapOf("bodyFatPercent" to 28.4, "bmr" to 1280.0)),
            )
        }
        resolutions.forEach { (w, h) ->
            capture("result_blood_pressure_${w}x$h", w, h) {
                KioskFrame(userLabel = "张三") { ResultScreen(DeviceType.BLOOD_PRESSURE, viewModel) }
            }
            capture("report_summary_${w}x$h", w, h) {
                KioskFrame(userLabel = "张三") { ReportSummaryScreen(viewModel) }
            }
        }
    }

    /**
     * 项目宫格在"只买了几个项目"的配置下的样子。
     * 宫格会按可用空间自动选列数并把方块撑到上限，所以项目越少图标越大——
     * 这一组截图就是用来确认这个自适应确实生效、以及上限设得合不合适。
     */
    @Test
    fun itemSelectionReducedConfig() {
        val reduced = listOf(
            DeviceType.BLOOD_PRESSURE,
            DeviceType.TEMPERATURE,
            DeviceType.HEIGHT_WEIGHT,
            DeviceType.BLOOD_OXYGEN,
        )
        val viewModel = AppViewModel(MockDeviceServiceApi(), MockUserDirectoryApi()).apply {
            visibleDeviceTypes = reduced
            startGuest()
            onMeasurementCompleted(DeviceType.TEMPERATURE, DeviceServiceSession("s", DeviceType.TEMPERATURE, emptyMap()))
        }
        resolutions.forEach { (w, h) ->
            capture("item_selection_reduced_${w}x$h", w, h) {
                KioskFrame(userLabel = "游客体验") { ItemSelectionScreen(viewModel) }
            }
        }
    }

    /** 游客模式补填性别/年龄段。 */
    @Test
    fun guestProfile() {
        val viewModel = AppViewModel(MockDeviceServiceApi(), MockUserDirectoryApi()).apply { startGuest() }
        resolutions.forEach { (w, h) ->
            capture("guest_profile_${w}x$h", w, h) {
                KioskFrame(userLabel = "游客体验") { GuestProfileScreen(viewModel) }
            }
            capture("guest_profile_selected_${w}x$h", w, h) {
                KioskFrame(userLabel = "游客体验") { GuestProfileScreen(viewModel, initialSex = "男", initialAgeBandIndex = 4) }
            }
        }
    }

    /** 欢迎页与准备页：主要用来确认主按钮改成实心后，其他页面的按钮对比没有过头。 */
    @Test
    fun welcomeAndPrepare() {
        val viewModel = AppViewModel(MockDeviceServiceApi(), MockUserDirectoryApi())
        resolutions.forEach { (w, h) ->
            capture("welcome_${w}x$h", w, h) {
                KioskFrame(userLabel = null) { WelcomeScreen(viewModel) }
            }
            capture("prepare_blood_pressure_${w}x$h", w, h) {
                KioskFrame(userLabel = "张三") { PrepareScreen(DeviceType.BLOOD_PRESSURE, viewModel) }
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
