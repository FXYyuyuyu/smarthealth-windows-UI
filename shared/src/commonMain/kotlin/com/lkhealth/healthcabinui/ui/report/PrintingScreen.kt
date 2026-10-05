package com.lkhealth.healthcabinui.ui.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.LoadingScreen
import kotlinx.coroutines.delay

/** 对应旧系统 PrintMeasuringUC/A4PrintMeasuringUC/PrintMeasuringBLEUC：打印中过渡态。 */
@Composable
fun PrintingScreen(viewModel: AppViewModel) {
    LaunchedEffect(Unit) {
        viewModel.speak("正在打印报告，请稍等")
        delay(1800)
        viewModel.onPrintingFinished()
    }

    LoadingScreen(
        title = "正在打印报告中···",
        subtitle = "请稍候，打印完成后将自动返回，谢谢！",
    )
}
