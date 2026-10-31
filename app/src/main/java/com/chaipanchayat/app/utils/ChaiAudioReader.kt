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

    data class ActiveArticleAudio(
        val postId: Long,
        val title: String,
        val subtitle: String = "चाय पंचायत ऑडियो • Hindi Voice",
        val imageUrl: String? = null,
        val contentHtml: String = "",
        val category: String = "ताज़ा ख़बरें"
    )

    companion object {
        @Volatile
        private var instance: ChaiAudioReader? = null

        fun getInstance(context: Context): ChaiAudioReader {
            return instance ?: synchronized(this) {
                instance ?: ChaiAudioReader(context.applicationContext).also { instance = it }
            }
        }
    }

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingPlayWhenReady = false

    private val _playState = mutableStateOf(PlayState.IDLE)
    val playState: State<PlayState> = _playState

    private val _progress = mutableFloatStateOf(0f)
    val progress: State<Float> = _progress

    private var userDismissed = false

    private val _activeArticle = mutableStateOf<ActiveArticleAudio?>(
        ActiveArticleAudio(
            postId = 0L,
            title = "चाय पंचायत • आज की प्रमुख ख़बरें",
            subtitle = "चाय पंचायत ऑडियो • Hindi Voice",
            imageUrl = null,
            contentHtml = "चाय पंचायत में आपका स्वागत है। देश, प्रदेश और आपके शहर की ताज़ा और विश्वसनीय ख़बरें अब ऑडियो में सुनें।",
            category = "ताज़ा ख़बरें"
        )
    )
    val activeArticle: State<ActiveArticleAudio?> = _activeArticle

    private var sentences: List<String> = emptyList()
    private var currentSentenceIndex = 0

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                try {
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
                } catch (_: Exception) {
                    _playState.value = PlayState.ERROR
                }
            }
        } catch (_: Exception) {
            _playState.value = PlayState.ERROR
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

    fun seedFeaturedArticle(
        postId: Long,
        title: String,
        contentHtml: String,
        imageUrl: String? = null,
        category: String = "ताज़ा ख़बरें"
    ) {
        if (userDismissed) return
        val current = _activeArticle.value
        if (current == null || current.postId == 0L) {
            _activeArticle.value = ActiveArticleAudio(
                postId = postId,
                title = title,
                subtitle = "$category • ऑडियो समाचार",
                imageUrl = imageUrl,
                contentHtml = contentHtml,
                category = category
            )
        }
    }

    fun playArticle(
        postId: Long,
        title: String,
        contentHtml: String,
        imageUrl: String? = null,
        category: String = "ताज़ा ख़बरें",
        locale: Locale? = null
    ) {
        userDismissed = false
        _activeArticle.value = ActiveArticleAudio(
            postId = postId,
            title = title,
            subtitle = "$category • ऑडियो समाचार",
            imageUrl = imageUrl,
            contentHtml = contentHtml,
            category = category
        )
        prepareAndPlay(title, contentHtml, locale)
    }

    fun prepareAndPlay(title: String, contentHtml: String, locale: Locale? = null) {
        val targetLocale = locale ?: Locale("hi", "IN")
        if (_activeArticle.value == null || _activeArticle.value?.title != title) {
            _activeArticle.value = ActiveArticleAudio(
                postId = _activeArticle.value?.postId ?: 0L,
                title = title,
                subtitle = "चाय पंचायत ऑडियो • Hindi Voice",
                imageUrl = _activeArticle.value?.imageUrl,
                contentHtml = contentHtml,
                category = _activeArticle.value?.category ?: "ताज़ा ख़बरें"
            )
        }
        try {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.ENGLISH)
            }
        } catch (_: Exception) {}

        val plainContent = HtmlUtils.stripHtml(contentHtml)
            .replace(Regex("https?://\\S+"), "")
            .replace(Regex("[\\[\\](){}]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val isEnglish = targetLocale.language == "en"
        val prefix = if (isEnglish) "Chai Panchayat News. " else "चाय पंचायत समाचार। "
        val fullText = "$prefix$title। $plainContent"

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
        } else if (_playState.value == PlayState.IDLE || _playState.value == PlayState.COMPLETED) {
            _activeArticle.value?.let {
                prepareAndPlay(it.title, it.contentHtml)
            }
        }
    }

    fun togglePlayPause(title: String, contentHtml: String, locale: Locale? = null) {
        when (_playState.value) {
            PlayState.PLAYING -> pause()
            PlayState.PAUSED -> resume()
            PlayState.COMPLETED, PlayState.IDLE, PlayState.ERROR -> prepareAndPlay(title, contentHtml, locale)
            PlayState.PREPARING -> {}
        }
    }

    fun toggleCurrent() {
        when (_playState.value) {
            PlayState.PLAYING -> pause()
            PlayState.PAUSED -> resume()
            PlayState.COMPLETED, PlayState.IDLE, PlayState.ERROR -> {
                _activeArticle.value?.let {
                    prepareAndPlay(it.title, it.contentHtml)
                }
            }
            PlayState.PREPARING -> {}
        }
    }

    fun skipNext() {
        if (sentences.isNotEmpty() && currentSentenceIndex < sentences.size - 1) {
            tts?.stop()
            currentSentenceIndex++
            updateProgress()
            if (_playState.value == PlayState.PLAYING) {
                speakSentence(currentSentenceIndex)
            }
        } else {
            stop()
        }
    }

    fun skipPrevious() {
        if (sentences.isNotEmpty()) {
            tts?.stop()
            currentSentenceIndex = (currentSentenceIndex - 1).coerceAtLeast(0)
            updateProgress()
            if (_playState.value == PlayState.PLAYING) {
                speakSentence(currentSentenceIndex)
            }
        }
    }

    fun stop() {
        tts?.stop()
        currentSentenceIndex = 0
        _progress.value = 0f
        _playState.value = PlayState.IDLE
    }

    fun dismiss() {
        userDismissed = true
        stop()
        _activeArticle.value = null
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
