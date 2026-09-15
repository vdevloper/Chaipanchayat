package com.chaipanchayat.app.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

class ChaiAudioReader(context: Context) {
    enum class PlayState {
        IDLE,
        PREPARING,
        PLAYING,
        PAUSED,
        COMPLETED,
        ERROR
    }

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingPlayWhenReady = false

    private val _playState = mutableStateOf(PlayState.IDLE)
    val playState: State<PlayState> = _playState

    private val _progress = mutableFloatStateOf(0f)
    val progress: State<Float> = _progress

    private var sentences: List<String> = emptyList()
    private var currentSentenceIndex = 0

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                val hindiLocale = Locale("hi", "IN")
                val langResult = tts?.setLanguage(hindiLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
                tts?.setSpeechRate(0.95f) // Natural pacing for Hindi news reading
                tts?.setPitch(1.0f)
                setupUtteranceListener()

                if (pendingPlayWhenReady) {
                    pendingPlayWhenReady = false
                    startSpeakingFromCurrent()
                }
            } else {
                _playState.value = PlayState.ERROR
            }
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _playState.value = PlayState.PLAYING
                updateProgress()
            }

            override fun onDone(utteranceId: String?) {
                currentSentenceIndex++
                updateProgress()
                if (currentSentenceIndex < sentences.size && _playState.value == PlayState.PLAYING) {
                    speakSentence(currentSentenceIndex)
                } else if (currentSentenceIndex >= sentences.size) {
                    _playState.value = PlayState.COMPLETED
                    _progress.value = 1f
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _playState.value = PlayState.ERROR
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _playState.value = PlayState.ERROR
            }
        })
    }

    fun prepareAndPlay(title: String, contentHtml: String) {
        val plainContent = HtmlUtils.stripHtml(contentHtml)
            .replace(Regex("https?://\\S+"), "")
            .replace(Regex("[\\[\\](){}]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val fullText = "चाय पंचायत समाचार। $title। $plainContent"

        // Split by Hindi and English sentence terminators
        val rawSentences = fullText.split(Regex("[।\n.!?]+"))
            .map { it.trim() }
            .filter { it.length > 2 }

        sentences = if (rawSentences.isNotEmpty()) rawSentences else listOf(title)
        currentSentenceIndex = 0
        _progress.value = 0f

        if (isTtsInitialized) {
            _playState.value = PlayState.PREPARING
            startSpeakingFromCurrent()
        } else {
            pendingPlayWhenReady = true
            _playState.value = PlayState.PREPARING
        }
    }

    private fun startSpeakingFromCurrent() {
        if (sentences.isEmpty() || currentSentenceIndex >= sentences.size) {
            _playState.value = PlayState.COMPLETED
            return
        }
        _playState.value = PlayState.PLAYING
        speakSentence(currentSentenceIndex)
    }

    private fun speakSentence(index: Int) {
        if (index !in sentences.indices) return
        val sentence = sentences[index]
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "chai_utt_$index")
        tts?.speak(sentence, TextToSpeech.QUEUE_FLUSH, params, "chai_utt_$index")
    }

    fun pause() {
        if (_playState.value == PlayState.PLAYING) {
            tts?.stop()
            _playState.value = PlayState.PAUSED
        }
    }

    fun resume() {
        if (_playState.value == PlayState.PAUSED) {
            startSpeakingFromCurrent()
        }
    }

    fun togglePlayPause(title: String, contentHtml: String) {
        when (_playState.value) {
            PlayState.PLAYING -> pause()
            PlayState.PAUSED -> resume()
            PlayState.COMPLETED, PlayState.IDLE, PlayState.ERROR -> prepareAndPlay(title, contentHtml)
            PlayState.PREPARING -> {}
        }
    }

    fun stop() {
        tts?.stop()
        currentSentenceIndex = 0
        _progress.value = 0f
        _playState.value = PlayState.IDLE
    }

    private fun updateProgress() {
        if (sentences.isNotEmpty()) {
            _progress.value = (currentSentenceIndex.toFloat() / sentences.size).coerceIn(0f, 1f)
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isTtsInitialized = false
    }
}
