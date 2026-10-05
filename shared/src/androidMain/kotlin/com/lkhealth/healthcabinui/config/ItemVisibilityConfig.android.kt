package com.lkhealth.healthcabinui.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

/** 安卓端配置文件位置：应用私有目录下的 [ItemVisibilityConfig.CONFIG_FILE_NAME]。 */
@Composable
actual fun rememberItemConfigText(): String? {
    val context = LocalContext.current.applicationContext
    return remember {
        val file = File(context.filesDir, ItemVisibilityConfig.CONFIG_FILE_NAME)
        if (!file.exists()) {
            runCatching { file.writeText(ItemVisibilityConfig.defaultTemplate()) }
        }
        runCatching { file.readText() }.getOrNull()
    }
}
