package com.lkhealth.healthcabinui.ui.identity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.ui.components.LoadingScreen

/** 对应旧系统 UploadDataUC 的"正在上传数据中"过渡态：调用身份查询，根据返回结果分流到不同页面。 */
@Composable
fun IdentityLookupScreen(uid: String, viewModel: AppViewModel) {
    LaunchedEffect(uid) {
        viewModel.speak("正在核验身份，请稍等")
        viewModel.performLookup(uid)
    }

    LoadingScreen(
        title = "正在核验身份中···",
        subtitle = "请稍等，谢谢！",
        modifier = Modifier,
    )
}
