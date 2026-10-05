package com.lkhealth.healthcabinui.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File

/**
 * 桌面端配置文件位置：程序运行目录下的 [ItemVisibilityConfig.CONFIG_FILE_NAME]，
 * 对应旧系统 ItemConfig.xml 与安装目录同级、方便管理员直接编辑的习惯。
 */
@Composable
actual fun rememberItemConfigText(): String? = remember {
    val file = File(ItemVisibilityConfig.CONFIG_FILE_NAME)
    if (!file.exists()) {
        runCatching { file.writeText(ItemVisibilityConfig.defaultTemplate()) }
    }
    runCatching { file.readText() }.getOrNull()
}
