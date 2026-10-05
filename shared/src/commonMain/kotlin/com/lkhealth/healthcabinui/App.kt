package com.lkhealth.healthcabinui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lkhealth.healthcabinui.config.ItemVisibilityConfig
import com.lkhealth.healthcabinui.config.rememberItemConfigText
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.session.IdleInteractionBox
import com.lkhealth.healthcabinui.session.Route
import com.lkhealth.healthcabinui.session.appModule
import com.lkhealth.healthcabinui.session.rememberIdleCountdown
import com.lkhealth.healthcabinui.ui.common.GenericErrorScreen
import com.lkhealth.healthcabinui.ui.components.HealthCabinTopBar
import com.lkhealth.healthcabinui.ui.entry.ManualEntryScreen
import com.lkhealth.healthcabinui.ui.entry.ScanEntryScreen
import com.lkhealth.healthcabinui.ui.face.FaceCaptureScreen
import com.lkhealth.healthcabinui.ui.identity.IdentityLookupScreen
import com.lkhealth.healthcabinui.ui.identity.ProfileOnboardingScreen
import com.lkhealth.healthcabinui.ui.items.ItemSelectionScreen
import com.lkhealth.healthcabinui.ui.measurement.MeasurementErrorScreen
import com.lkhealth.healthcabinui.ui.measurement.MeasuringScreen
import com.lkhealth.healthcabinui.ui.measurement.PrepareScreen
import com.lkhealth.healthcabinui.ui.measurement.ResultScreen
import com.lkhealth.healthcabinui.ui.report.PrintingScreen
import com.lkhealth.healthcabinui.ui.report.ReportSummaryScreen
import com.lkhealth.healthcabinui.ui.settings.SettingsScreen
import com.lkhealth.healthcabinui.ui.theme.HealthCabinTheme
import com.lkhealth.healthcabinui.ui.welcome.WelcomeScreen
import com.lkhealth.healthcabinui.voice.rememberVoiceGuide
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    @Suppress("DEPRECATION")
    KoinApplication(application = { modules(appModule) }) {
        HealthCabinTheme {
            HealthCabinApp(viewModel = koinInject())
        }
    }
}

@Composable
private fun HealthCabinApp(viewModel: AppViewModel) {
    val voiceGuide = rememberVoiceGuide()
    LaunchedEffect(voiceGuide) { viewModel.voiceGuide = voiceGuide }

    val itemConfigText = rememberItemConfigText()
    LaunchedEffect(itemConfigText) { viewModel.visibleDeviceTypes = ItemVisibilityConfig.resolve(itemConfigText) }

    val backStack by viewModel.navigator.backStack.collectAsStateWithLifecycle()
    val route = backStack.last()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isGuest by viewModel.isGuest.collectAsStateWithLifecycle()

    // 测量中/打印中不参与空闲计时，避免中途打断硬件动作；欢迎页本身就是空闲态的落点。
    val idleResetEnabled = route != Route.Welcome && route !is Route.Measuring && route != Route.Printing
    val idleCountdown = rememberIdleCountdown(enabled = idleResetEnabled, onIdle = viewModel::idleTimeout)

    Scaffold(
        topBar = {
            // 顶部栏在所有页面（含欢迎页）保持一致，logo/标题固定左上角，符合旧系统的常驻导航条布局。
            HealthCabinTopBar(
                userLabel = when {
                    isGuest -> "游客体验"
                    currentUser != null -> currentUser!!.name
                    else -> null
                },
                idleCountdownSeconds = idleCountdown.remainingSeconds,
                onAdminUnlocked = viewModel::onAdminUnlocked,
            )
        },
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            IdleInteractionBox(onInteraction = idleCountdown.onInteraction) {
                // 柔和的纯淡入淡出，不带缩放/弹跳，也不做尺寸形变动画，避免切页时的跳动感。
                AnimatedContent(
                    targetState = route,
                    label = "route",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(260)) togetherWith fadeOut(animationSpec = tween(160))
                    },
                ) { currentRoute ->
                    RouteContent(currentRoute, viewModel)
                }
            }
        }
    }
}

@Composable
private fun RouteContent(route: Route, viewModel: AppViewModel) {
    when (route) {
        Route.Welcome -> WelcomeScreen(viewModel)
        Route.ManualEntry -> ManualEntryScreen(viewModel)
        Route.ScanEntry -> ScanEntryScreen(viewModel)
        is Route.FaceCapture -> FaceCaptureScreen(route.mode, route.uid, viewModel)
        is Route.IdentityLookup -> IdentityLookupScreen(route.uid, viewModel)
        is Route.ProfileOnboarding -> ProfileOnboardingScreen(route.uid, route.mode, viewModel)
        Route.ItemSelection -> ItemSelectionScreen(viewModel)
        is Route.Prepare -> PrepareScreen(route.deviceType, viewModel)
        is Route.Measuring -> MeasuringScreen(route.deviceType, viewModel)
        is Route.MeasurementResult -> ResultScreen(route.deviceType, viewModel)
        is Route.MeasurementError -> MeasurementErrorScreen(route.deviceType, route.message, viewModel)
        Route.ReportSummary -> ReportSummaryScreen(viewModel)
        Route.Printing -> PrintingScreen(viewModel)
        Route.Settings -> SettingsScreen(viewModel)
        is Route.ErrorScreen -> GenericErrorScreen(route.message, viewModel)
    }
}
