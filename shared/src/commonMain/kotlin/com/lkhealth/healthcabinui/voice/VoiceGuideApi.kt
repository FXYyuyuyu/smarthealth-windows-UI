package com.lkhealth.healthcabinui.voice

import androidx.compose.runtime.Composable

/**
 * 语音播报契约，对应旧系统 VoiceHelper 的语音指导功能。与硬件设备不同，
 * 文字转语音是操作系统自带能力，UI 层可以直接提供真实实现（Android 用系统 TTS，
 * 桌面端用 Windows 自带的 SAPI 语音合成），不需要等底层设备服务接入。
 */
interface VoiceGuideApi {
    /** 播报一段文字，新的播报会打断上一句尚未播完的内容。 */
    fun speak(text: String)

    /** 停止当前播报。 */
    fun stop()
}

/** 获取当前平台的语音播报实例；桌面端返回全局单例，Android 端绑定到当前 Activity 生命周期。 */
@Composable
expect fun rememberVoiceGuide(): VoiceGuideApi
