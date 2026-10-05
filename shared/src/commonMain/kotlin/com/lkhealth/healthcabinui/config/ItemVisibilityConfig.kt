package com.lkhealth.healthcabinui.config

import androidx.compose.runtime.Composable
import com.lkhealth.healthcabinui.device.DeviceType
import com.lkhealth.healthcabinui.device.MeasurementCatalog

/**
 * 项目宫格显示配置：对应旧系统手动编辑 `ItemConfig.xml` 增删检测项目的思路——
 * 用一份纯文本配置文件列出要展示的设备代码（每行一个，代码后可跟 `#` 注释；整行以 `#` 开头则视为禁用该项），
 * 文件里出现的顺序即项目宫格的排列顺序。文件缺失、为空、或解析不出任何有效代码时，
 * 回退到内置目录（[MeasurementCatalog]）的全部项目、默认顺序。
 *
 * 桌面端/安卓端分别通过 [rememberItemConfigText] 读取平台对应位置的配置文件，
 * 修改后需重启程序生效，与旧系统"编辑 ItemConfig.xml 后重启主程序"的行为一致。
 */
object ItemVisibilityConfig {

    const val CONFIG_FILE_NAME = "item_config.txt"

    fun resolve(configText: String?): List<DeviceType> {
        val defaultOrder = MeasurementCatalog.specs.map { it.deviceType }
        if (configText.isNullOrBlank()) return defaultOrder

        val codeToType = MeasurementCatalog.specs.associate { it.deviceType.configCode.uppercase() to it.deviceType }
        val resolved = configText.lineSequence()
            .map { it.substringBefore('#').trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { code -> codeToType[code.uppercase()] }
            .toList()

        return resolved.ifEmpty { defaultOrder }
    }

    /** 首次运行时写入磁盘的默认模板：全部项目按内置顺序启用，管理员在行首加 `#` 即可隐藏某一项。 */
    fun defaultTemplate(): String = buildString {
        appendLine("# 健康小屋 - 检测项目显示配置")
        appendLine("# 每行一个设备代码，决定项目选择页显示哪些检测项目、以及显示顺序。")
        appendLine("# 在某一行行首加 # 可以注释掉该项，使其不在项目宫格中显示。")
        appendLine("# 修改后需要重启程序才能生效。")
        appendLine("#")
        MeasurementCatalog.specs.forEach { spec ->
            appendLine("${spec.deviceType.configCode}  # ${spec.title}")
        }
    }
}

/**
 * 读取平台对应位置的项目显示配置文件内容；文件不存在时会自动写入 [ItemVisibilityConfig.defaultTemplate]
 * 模板（方便管理员直接在原文件上注释/调整），并返回这份刚写入的内容。
 */
@Composable
expect fun rememberItemConfigText(): String?
