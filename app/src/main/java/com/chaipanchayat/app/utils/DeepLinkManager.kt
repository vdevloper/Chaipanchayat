package com.chaipanchayat.app.utils

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object DeepLinkManager {

    private const val PREFS_NAME = "chai_deeplink_prefs"
    private const val KEY_LAST_HANDLED_ID = "last_handled_post_id"
    private const val APP_PACKAGE = "com.chaipanchayat.app"

    /**
     * Extracts an article ID from an incoming Intent (deep link URI or extras).
     */
    fun extractArticleId(intent: Intent?): Long? {
        if (intent == null) return null

        // 1. Check URI Data
        val data: Uri? = intent.data
        if (data != null) {
            val fromUri = parseArticleIdFromUri(data)
            if (fromUri != null && fromUri > 0L) {
                return fromUri
            }
        }

        // 2. Check Intent Extras directly
        val extraLong = intent.getLongExtra("article_id", -1L)
            .takeIf { it > 0 }
            ?: intent.getLongExtra("post_id", -1L).takeIf { it > 0 }
            ?: intent.getLongExtra("id", -1L).takeIf { it > 0 }
        if (extraLong != null) return extraLong

        val extraString = intent.getStringExtra("article_id")
            ?: intent.getStringExtra("post_id")
            ?: intent.getStringExtra("id")
        extraString?.toLongOrNull()?.takeIf { it > 0 }?.let { return it }

        // 3. Check Referrer String in Intent extras (e.g. from Google Play Store install)
        val referrer = intent.getStringExtra("referrer")
        if (!referrer.isNullOrBlank()) {
            val fromReferrer = parseArticleIdFromReferrer(referrer)
            if (fromReferrer != null && fromReferrer > 0L) {
                return fromReferrer
            }
        }

        return null
    }

    /**
     * Parses article ID from Uri like:
     * - chaipanchayat://post/1234
     * - chaipanchayat://post?id=1234
     * - chaipanchayat://article/1234
     * - intent://post/1234#Intent;scheme=chaipanchayat;...
     */
    fun parseArticleIdFromUri(uri: Uri): Long? {
        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase()

        // Handle chaipanchayat scheme
        if (scheme == "chaipanchayat") {
            // Check query parameters first (?id=123, ?post_id=123, ?p=123)
            val queryId = uri.getQueryParameter("id")?.toLongOrNull()
                ?: uri.getQueryParameter("post_id")?.toLongOrNull()
                ?: uri.getQueryParameter("p")?.toLongOrNull()
                ?: uri.getQueryParameter("article_id")?.toLongOrNull()
            if (queryId != null && queryId > 0) return queryId

            // Check path segments: e.g. /1234 or /post/1234
            val segments = uri.pathSegments
            if (segments.isNotEmpty()) {
                val lastSegment = segments.lastOrNull()?.toLongOrNull()
                if (lastSegment != null && lastSegment > 0) return lastSegment
            }

            // Check if host itself is a number (e.g. chaipanchayat://1234)
            val hostAsNum = host?.toLongOrNull()
            if (hostAsNum != null && hostAsNum > 0) return hostAsNum
        }

        // Also check if URI query has referrer
        val referrerParam = uri.getQueryParameter("referrer")
        if (!referrerParam.isNullOrBlank()) {
            val fromReferrer = parseArticleIdFromReferrer(referrerParam)
            if (fromReferrer != null && fromReferrer > 0) return fromReferrer
        }

        return null
    }

    /**
     * Parses "post_id=1234" or "article_id=1234" from a Google Play Store referrer query string.
     */
    fun parseArticleIdFromReferrer(referrerString: String): Long? {
        return try {
            val decoded = URLDecoder.decode(referrerString, StandardCharsets.UTF_8.name())
            val regex = Regex("""(?:post_id|article_id|id|p)=(\d+)""", RegexOption.IGNORE_CASE)
            val match = regex.find(decoded) ?: regex.find(referrerString)
            match?.groupValues?.getOrNull(1)?.toLongOrNull()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Checks if a deferred deep link exists (from install referrer or clipboard on fresh launch).
     */
    fun checkDeferredInstallDeepLink(context: Context): Long? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Check if there was an install referrer stored
        val storedReferrerId = prefs.getLong("pending_referrer_post_id", -1L).takeIf { it > 0 }
        if (storedReferrerId != null) {
            prefs.edit().remove("pending_referrer_post_id").apply()
            markHandled(context, storedReferrerId)
            return storedReferrerId
        }

        // 2. Check Clipboard for Chai Panchayat shared links (when user copied link and then installed app)
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val clipText = clipData.getItemAt(0)?.text?.toString().orEmpty()
                    if (clipText.contains("chaipanchayat://post/") || clipText.contains("post_id=")) {
                        val customMatch = Regex("""chaipanchayat://post/(\d+)""").find(clipText)
                        val candidateId = customMatch?.groupValues?.getOrNull(1)?.toLongOrNull()
                            ?: parseArticleIdFromReferrer(clipText)

                        if (candidateId != null && candidateId > 0) {
                            val lastHandled = prefs.getLong(KEY_LAST_HANDLED_ID, -1L)
                            if (candidateId != lastHandled) {
                                markHandled(context, candidateId)
                                return candidateId
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore clipboard security exceptions on background threads or restricted OEMs
        }

        return null
    }

    /**
     * Records a handled post ID to avoid re-triggering on subsequent app launches.
     */
    fun markHandled(context: Context, postId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_HANDLED_ID, postId).apply()
    }

    /**
     * Saves a pending referrer ID received from Google Play Install Referrer.
     */
    fun savePendingReferrerId(context: Context, postId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong("pending_referrer_post_id", postId).apply()
    }

    /**
     * Generates a direct in-app share intent without sending users to the external website.
     */
    fun createShareIntent(postId: Long, cleanTitle: String): Intent {
        val appSchemeUrl = "chaipanchayat://post/$postId"
        val playStoreUrl = "https://play.google.com/store/apps/details?id=$APP_PACKAGE&referrer=post_id%3D$postId"

        val shareMessage = buildString {
            append("🔥 ")
            append(cleanTitle)
            append("\n\n")
            append("📲 चाय पंचायत ऐप में सीधे खोलें व पढ़ें:\n")
            append(appSchemeUrl)
            append("\n\n")
            append("📥 यदि आपके पास ऐप नहीं है, तो यहाँ से तुरंत डाउनलोड करें:\n")
            append(playStoreUrl)
            append("\n\n")
            append("(चाय पंचायत - बेबाक, सटीक, निष्पक्ष खबरें)")
        }

        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, cleanTitle)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
    }
}
