package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognitionHelper(
    private val context: Context,
    private val onStateChange: (AssistantState) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening: Boolean = false

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onStateChange(AssistantState.LISTENING)
            }

            override fun onBeginningOfSpeech() {
                onStateChange(AssistantState.LISTENING)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Can be used for audio amplitude waves
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                onStateChange(AssistantState.THINKING)
            }

            override fun onError(error: Int) {
                isListening = false
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error (माइक त्रुटि)"
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognition cancelled"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required (माइक अनुमति चाहिए)"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network unavailable for speech (नेटवर्क त्रुटि)"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Kuch sunai nahi diya. Kripya dobara bolein."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected (समय समाप्त)"
                    else -> "Speech recognition failed ($error)"
                }
                Log.w("SadbhavnaSpeech", "Error: $message")
                onError(message)
                onStateChange(AssistantState.IDLE)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim() ?: ""
                if (recognizedText.isNotEmpty()) {
                    onFinalResult(recognizedText)
                } else {
                    onStateChange(AssistantState.IDLE)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull()?.trim() ?: ""
                if (partialText.isNotEmpty()) {
                    onPartialResult(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening(languageCode: String = "hi-IN") {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not supported on this device.")
            return
        }

        if (speechRecognizer == null) {
            initRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("SadbhavnaSpeech", "startListening error", e)
            onError("Mic start failed: ${e.message}")
            onStateChange(AssistantState.IDLE)
        }
    }

    fun stopListening() {
        try {
            if (isListening) {
                speechRecognizer?.stopListening()
                isListening = false
            }
        } catch (e: Exception) {
            Log.e("SadbhavnaSpeech", "stopListening error", e)
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (e: Exception) {
            Log.e("SadbhavnaSpeech", "destroy error", e)
        }
    }
}
