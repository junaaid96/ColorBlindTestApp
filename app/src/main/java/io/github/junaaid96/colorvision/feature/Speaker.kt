package io.github.junaaid96.colorvision.feature

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Thin wrapper around the on-device TextToSpeech engine, released with the composition. */
class Speaker(context: android.content.Context) {
    private var ready = false
    private val tts = TextToSpeech(context.applicationContext) { ready = it == TextToSpeech.SUCCESS }
    fun say(text: String) { if (ready) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "color") }
    fun release() = tts.shutdown()
}

@Composable
fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val speaker = remember { Speaker(context) }
    DisposableEffect(speaker) { onDispose { speaker.release() } }
    return speaker
}
