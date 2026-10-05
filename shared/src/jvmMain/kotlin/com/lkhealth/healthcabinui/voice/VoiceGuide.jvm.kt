package com.lkhealth.healthcabinui.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Composable
actual fun rememberVoiceGuide(): VoiceGuideApi = remember { WindowsSapiVoiceGuide() }

/**
 * 桌面端目标机型是 Windows 一体机，直接调用系统自带的 SAPI 语音合成（System.Speech），
 * 不需要额外的第三方 TTS 库。每次播报都会先打断上一个仍在朗读的进程。
 */
private class WindowsSapiVoiceGuide : VoiceGuideApi {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var currentProcess: Process? = null

    override fun speak(text: String) {
        if (text.isBlank()) return
        scope.launch {
            stop()
            try {
                val escaped = text.replace("'", "''")
                val script = "Add-Type -AssemblyName System.Speech; " +
                    "(New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('$escaped')"
                val process = ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-WindowStyle", "Hidden",
                    "-Command", script,
                ).redirectErrorStream(true).start()
                currentProcess = process
                process.inputStream.readBytes()
                process.waitFor()
            } catch (_: Exception) {
                // 非 Windows 开发环境（无 powershell/SAPI）时静默跳过，不影响 UI 流程。
            }
        }
    }

    override fun stop() {
        currentProcess?.destroy()
        currentProcess = null
    }
}
