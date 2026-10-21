package com.example.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(
    private val context: Context,
    private val onStateChange: (AssistantState) -> Unit,
    private val onInitResult: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized: Boolean = false
    private var preferredLocale: Locale = Locale.forLanguageTag("hi-IN")
    private var speechRate: Float = 0.95f // Calm, sweet natural pace
    private var speechPitch: Float = 1.15f // Soft-spoken, warm female pitch

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupVoice()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onStateChange(AssistantState.SPEAKING)
                }

                override fun onDone(utteranceId: String?) {
                    onStateChange(AssistantState.IDLE)
                }

                override fun onError(utteranceId: String?) {
                    onStateChange(AssistantState.IDLE)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?, errorCode: Int) {
                    onStateChange(AssistantState.IDLE)
                }
            })
            onInitResult(true)
        } else {
            isInitialized = false
            Log.e("JarvisTTS", "TTS Initialization failed with status $status")
            onInitResult(false)
        }
    }

    private fun setupVoice() {
        tts?.let { engine ->
            val result = engine.setLanguage(preferredLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.ENGLISH)
            }

            // Look for female / natural voice
            try {
                val availableVoices = engine.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val femaleVoice = availableVoices.firstOrNull { voice ->
                        voice.locale.language == preferredLocale.language &&
                                (voice.name.contains("female", ignoreCase = true) ||
                                        voice.name.contains("woman", ignoreCase = true) ||
                                        voice.name.contains("hie", ignoreCase = true) ||
                                        voice.name.contains("hid", ignoreCase = true))
                    } ?: availableVoices.firstOrNull { it.locale.language == preferredLocale.language }

                    if (femaleVoice != null) {
                        engine.voice = femaleVoice
                    }
                }
            } catch (e: Exception) {
                Log.d("JarvisTTS", "Voice selection fallback: ${e.message}")
            }

            engine.setSpeechRate(speechRate)
            engine.setPitch(speechPitch)
        }
    }

    fun setLanguage(languageCode: String) {
        preferredLocale = when (languageCode) {
            "en-IN", "en-US", "en" -> Locale.forLanguageTag("en-IN")
            else -> Locale.forLanguageTag("hi-IN")
        }
        if (isInitialized) {
            setupVoice()
        }
    }

    fun setSpeedAndPitch(rate: Float, pitch: Float) {
        speechRate = rate
        speechPitch = pitch
        if (isInitialized) {
            tts?.setSpeechRate(rate)
            tts?.setPitch(pitch)
        }
    }

    fun speak(text: String, utteranceId: String = "JarvisResponse_${System.currentTimeMillis()}") {
        if (!isInitialized || tts == null) {
            Log.w("JarvisTTS", "TTS not initialized, skipping speak")
            return
        }

        // Clean out special symbols while keeping natural pauses ("...", commas)
        val cleaned = text
            .replace(Regex("[*#_`~]"), "")
            .replace(Regex("\\bhttps?://\\S+"), "link")
            .trim()

        if (cleaned.isEmpty()) return

        stop()
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
            onStateChange(AssistantState.IDLE)
        } catch (e: Exception) {
            Log.e("JarvisTTS", "Error stopping TTS", e)
        }
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("JarvisTTS", "Error shutting down TTS", e)
        }
    }
}
