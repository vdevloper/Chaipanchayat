package com.chaipanchayat.app.data.api

import android.util.LruCache
import com.chaipanchayat.app.data.model.AppLanguage
import com.chaipanchayat.app.data.model.WPPost
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

data class TranslatedArticle(
    val postId: Long,
    val targetLang: AppLanguage,
    val translatedTitle: String,
    val translatedExcerpt: String,
    val translatedContent: String
)

object TranslationService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .build()

    // In-memory cache: "postId_langCode" -> TranslatedArticle
    private val articleCache = LruCache<String, TranslatedArticle>(50)
    // Dedicated title cache: "postId_langCode" -> translatedTitle
    private val titleCache = LruCache<String, String>(300)
    // General text cache: "langCode_textHash" -> translatedText
    private val textCache = LruCache<String, String>(300)

    fun getCachedTitle(postId: Long, targetLang: AppLanguage): String? {
        if (targetLang == AppLanguage.HINDI) return null
        return titleCache.get("${postId}_${targetLang.code}")
    }

    suspend fun translateTitle(postId: Long, originalTitle: String, targetLang: AppLanguage): String {
        if (targetLang == AppLanguage.HINDI || originalTitle.isBlank()) return originalTitle
        val cacheKey = "${postId}_${targetLang.code}"
        val cached = titleCache.get(cacheKey)
        if (cached != null) return cached

        val translated = translateText(originalTitle, targetLang)
        titleCache.put(cacheKey, translated)
        return translated
    }

    suspend fun translatePostsTitles(posts: List<WPPost>, targetLang: AppLanguage): Map<Long, String> = coroutineScope {
        if (targetLang == AppLanguage.HINDI || posts.isEmpty()) return@coroutineScope emptyMap()

        val results = posts.map { post ->
            async(Dispatchers.IO) {
                val translated = translateTitle(post.id, post.cleanTitle, targetLang)
                post.id to translated
            }
        }.awaitAll().toMap()

        results
    }

    fun getCachedArticle(postId: Long, targetLang: AppLanguage): TranslatedArticle? {
        val key = "${postId}_${targetLang.code}"
        return articleCache.get(key)
    }

    suspend fun translateArticle(
        postId: Long,
        title: String,
        excerpt: String,
        content: String,
        targetLang: AppLanguage
    ): TranslatedArticle = withContext(Dispatchers.IO) {
        val cacheKey = "${postId}_${targetLang.code}"
        val cached = articleCache.get(cacheKey)
        if (cached != null) return@withContext cached

        // If target is already Hindi, return as-is
        if (targetLang == AppLanguage.HINDI) {
            val original = TranslatedArticle(
                postId = postId,
                targetLang = targetLang,
                translatedTitle = title,
                translatedExcerpt = excerpt,
                translatedContent = content
            )
            articleCache.put(cacheKey, original)
            return@withContext original
        }

        val translatedTitle = translateText(title, targetLang)
        val translatedExcerpt = if (excerpt.isNotBlank()) translateText(excerpt, targetLang) else ""
        val translatedContent = if (content.isNotBlank()) translateHtmlContent(content, targetLang) else ""

        val result = TranslatedArticle(
            postId = postId,
            targetLang = targetLang,
            translatedTitle = translatedTitle,
            translatedExcerpt = translatedExcerpt,
            translatedContent = translatedContent
        )
        titleCache.put("${postId}_${targetLang.code}", translatedTitle)
        articleCache.put(cacheKey, result)
        result
    }

    suspend fun translateText(text: String, targetLang: AppLanguage): String = withContext(Dispatchers.IO) {
        if (text.isBlank() || targetLang == AppLanguage.HINDI) return@withContext text

        val cacheKey = "${targetLang.code}_${text.hashCode()}"
        val cached = textCache.get(cacheKey)
        if (cached != null) return@withContext cached

        // Process in manageable chunks of max ~1500 chars to prevent URL overflow
        val result = if (text.length > 1500) {
            val chunks = text.chunked(1500)
            chunks.joinToString(" ") { chunk ->
                fetchTranslation(chunk, targetLang.code)
            }
        } else {
            fetchTranslation(text, targetLang.code)
        }

        textCache.put(cacheKey, result)
        result
    }

    private fun translateHtmlContent(htmlContent: String, targetLang: AppLanguage): String {
        return try {
            // Regex to preserve HTML tags while translating inner text
            val tagRegex = Regex("""(<[^>]+>)""")
            val tokens = mutableListOf<String>()
            var lastIndex = 0

            tagRegex.findAll(htmlContent).forEach { match ->
                if (match.range.first > lastIndex) {
                    tokens.add(htmlContent.substring(lastIndex, match.range.first))
                }
                tokens.add(match.value)
                lastIndex = match.range.last + 1
            }
            if (lastIndex < htmlContent.length) {
                tokens.add(htmlContent.substring(lastIndex))
            }

            val translatedSb = StringBuilder()
            val textBatch = StringBuilder()

            for (token in tokens) {
                if (token.startsWith("<") && token.endsWith(">")) {
                    if (textBatch.isNotEmpty()) {
                        val translated = fetchTranslation(textBatch.toString(), targetLang.code)
                        translatedSb.append(translated)
                        textBatch.clear()
                    }
                    translatedSb.append(token)
                } else {
                    val trimmed = token.trim()
                    if (trimmed.isNotBlank()) {
                        textBatch.append(token)
                    } else {
                        translatedSb.append(token)
                    }
                }
            }

            if (textBatch.isNotEmpty()) {
                val translated = fetchTranslation(textBatch.toString(), targetLang.code)
                translatedSb.append(translated)
            }

            translatedSb.toString()
        } catch (_: Exception) {
            fetchTranslation(htmlContent, targetLang.code)
        }
    }

    private fun fetchTranslation(text: String, targetLangCode: String): String {
        return try {
            val url = "https://translate.googleapis.com/translate_a/single".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("client", "gtx")
                ?.addQueryParameter("sl", "auto")
                ?.addQueryParameter("tl", targetLangCode)
                ?.addQueryParameter("dt", "t")
                ?.addQueryParameter("q", text)
                ?.build() ?: return text

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return text

            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return text

            val rootArray = JSONArray(body)
            val sentencesArray = rootArray.optJSONArray(0) ?: return text

            val sb = StringBuilder()
            for (i in 0 until sentencesArray.length()) {
                val sentence = sentencesArray.optJSONArray(i)
                val translatedPart = sentence?.optString(0, "")
                if (!translatedPart.isNullOrEmpty()) {
                    sb.append(translatedPart)
                }
            }

            if (sb.isNotEmpty()) sb.toString() else text
        } catch (_: Exception) {
            text
        }
    }
}
