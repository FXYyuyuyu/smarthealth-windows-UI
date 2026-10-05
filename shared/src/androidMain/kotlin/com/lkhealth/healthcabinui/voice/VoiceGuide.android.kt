package com.lkhealth.healthcabinui.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
actual fun rememberVoiceGuide(): VoiceGuideApi {
    val appContext = LocalContext.current.applicationContext
    val guide = remember { AndroidVoiceGuide(appContext) }
    DisposableEffect(Unit) {
        onDispose { guide.release() }
    }
    return guide
}

private class AndroidVoiceGuide(context: Context) : VoiceGuideApi {
    private var engine: TextToSpeech? = null

    init {
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.CHINA
            }
        }
    }

    override fun speak(text: String) {
        engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun stop() {
        engine?.stop()
    }

    fun release() {
        engine?.shutdown()
        engine = null
    }
}
