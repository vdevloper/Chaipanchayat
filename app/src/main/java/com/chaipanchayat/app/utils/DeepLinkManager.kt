package com.chaipanchayat.app.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object DeepLinkManager {

    private const val PREFS_NAME = "chai_deeplink_prefs"
    private const val KEY_LAST_HANDLED_ID = "last_handled_post_id"
    private const val APP_PACKAGE = "com.chaipanchayat.app"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

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
     * - https://chaipanchayat.com/?p=1234
     * - https://chaipanchayat.com/post/1234
     */
    fun parseArticleIdFromUri(uri: Uri): Long? {
        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase()

        // Handle chaipanchayat scheme
        if (scheme == "chaipanchayat") {
            val queryId = uri.getQueryParameter("id")?.toLongOrNull()
                ?: uri.getQueryParameter("post_id")?.toLongOrNull()
                ?: uri.getQueryParameter("p")?.toLongOrNull()
                ?: uri.getQueryParameter("article_id")?.toLongOrNull()
            if (queryId != null && queryId > 0) return queryId

            val segments = uri.pathSegments
            if (segments.isNotEmpty()) {
                val lastSegment = segments.lastOrNull()?.toLongOrNull()
                if (lastSegment != null && lastSegment > 0) return lastSegment
            }

            val hostAsNum = host?.toLongOrNull()
            if (hostAsNum != null && hostAsNum > 0) return hostAsNum
        }

        // Handle standard http/https web links (clickable in WhatsApp, Telegram, etc.)
        if (scheme == "http" || scheme == "https") {
            val queryId = uri.getQueryParameter("p")?.toLongOrNull()
                ?: uri.getQueryParameter("id")?.toLongOrNull()
                ?: uri.getQueryParameter("post_id")?.toLongOrNull()
                ?: uri.getQueryParameter("article_id")?.toLongOrNull()
            if (queryId != null && queryId > 0) return queryId

            val segments = uri.pathSegments
            if (segments.isNotEmpty()) {
                val lastSegment = segments.lastOrNull()?.toLongOrNull()
                if (lastSegment != null && lastSegment > 0) return lastSegment
                val anyNumSegment = segments.firstOrNull { it.toLongOrNull() != null && it.toLong() > 0 }?.toLongOrNull()
                if (anyNumSegment != null) return anyNumSegment
            }
        }

        // Check if URI query has referrer
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

        // 2. Check Clipboard for Chai Panchayat shared links
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val clipText = clipData.getItemAt(0)?.text?.toString().orEmpty()
                    if (clipText.contains("chaipanchayat") || clipText.contains("post_id=") || clipText.contains("web.app") || clipText.contains("firebaseapp")) {
                        val customMatch = Regex("""(?:chaipanchayat://post/|\?p=|/post/)(\d+)""").find(clipText)
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
     * Builds a clean, fully clickable share message for WhatsApp, Telegram, SMS, etc.
     * Includes:
     * 1) Smart In-App Link (Firebase Hosting intent redirector): https://gen-lang-client-0700030242.web.app/post/$postId
     * 2) Official Website Article Link: https://chaipanchayat.com/?p=$postId (or post permalink)
     * 3) App Download Link (Google Play Store)
     */
    fun buildShareMessage(postId: Long, cleanTitle: String, permalink: String? = null): String {
        val websiteLink = if (!permalink.isNullOrBlank()) permalink.trim() else "https://chaipanchayat.com/?p=$postId"

        return buildString {
            append("☕ ")
            append(cleanTitle.trim())
            append("\n")
            append("चाय पंचायत - बेबाक, सटीक, निष्पक्ष")
            append("\n\n")
            append(websiteLink)
        }
    }

    /**
     * Builds rich HTML content with clickable hyperlinks for apps that support HTML (Gmail, Outlook, Keep, Notes).
     */
    fun buildHtmlShareMessage(postId: Long, cleanTitle: String, permalink: String? = null): String {
        val websiteLink = if (!permalink.isNullOrBlank()) permalink.trim() else "https://chaipanchayat.com/?p=$postId"
        val safeTitle = cleanTitle.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").trim()

        return """
            <p><b>☕ $safeTitle</b></p>
            <p><i>चाय पंचायत - बेबाक, सटीक, निष्पक्ष</i></p>
            <p><a href="$websiteLink">$websiteLink</a></p>
        """.trimIndent()
    }

    /**
     * Shares article with featured image attached and clickable links in caption.
     * Uses Coil cache first, then OkHttp, with fallback to the high-res app logo.
     * Explicitly grants URI read permissions to target apps and the chooser.
     */
    suspend fun shareArticle(
        context: Context,
        postId: Long,
        cleanTitle: String,
        imageUrl: String? = null,
        permalink: String? = null
    ) = withContext(Dispatchers.IO) {
        val shareMessage = buildShareMessage(postId, cleanTitle, permalink)
        val htmlShareMessage = buildHtmlShareMessage(postId, cleanTitle, permalink)
        var imageContentUri: Uri? = null

        val imagesDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        var imageFile = File(imagesDir, "share_${postId}.jpg")

        // 1. Try preparing image if imageUrl is present and not cached
        if (!imageUrl.isNullOrBlank()) {
            if (!imageFile.exists() || imageFile.length() < 500L) {
                // A. Fast path: load from Coil (disk/memory cache)
                try {
                    val imageLoader = Coil.imageLoader(context)
                    val req = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .allowHardware(false)
                        .build()
                    val result = imageLoader.execute(req)
                    if (result is SuccessResult) {
                        val bitmap = result.drawable.toBitmap()
                        val tempFile = File(imagesDir, "share_${postId}_tmp.jpg")
                        FileOutputStream(tempFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                        }
                        if (tempFile.length() > 500L) {
                            if (imageFile.exists()) imageFile.delete()
                            tempFile.renameTo(imageFile)
                        }
                    }
                } catch (_: Exception) {}

                // B. Fallback: direct OkHttp download
                if (!imageFile.exists() || imageFile.length() < 500L) {
                    try {
                        val safeUrl = when {
                            imageUrl.startsWith("//") -> "https:$imageUrl"
                            !imageUrl.startsWith("http") -> "https://$imageUrl"
                            else -> imageUrl
                        }.replace(" ", "%20")

                        val request = Request.Builder()
                            .url(safeUrl)
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                            .header("Accept", "image/webp,image/jpeg,image/png,image/*,*/*;q=0.8")
                            .build()
                        val response = httpClient.newCall(request).execute()
                        if (response.isSuccessful) {
                            val bytes = response.body?.bytes()
                            if (bytes != null && bytes.isNotEmpty()) {
                                val tempFile = File(imagesDir, "share_${postId}_tmp.jpg")
                                FileOutputStream(tempFile).use { fos ->
                                    fos.write(bytes)
                                }
                                if (tempFile.length() > 500L) {
                                    if (imageFile.exists()) imageFile.delete()
                                    tempFile.renameTo(imageFile)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        // 2. Fallback: if no image exists yet, copy app logo as high-res branded share card
        if (!imageFile.exists() || imageFile.length() < 500L) {
            try {
                val logoFile = File(imagesDir, "chai_logo_share.png")
                if (!logoFile.exists() || logoFile.length() < 500L) {
                    context.resources.openRawResource(com.chaipanchayat.app.R.drawable.ic_logo).use { input ->
                        FileOutputStream(logoFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                if (logoFile.exists() && logoFile.length() > 500L) {
                    imageFile = logoFile
                }
            } catch (_: Exception) {}
        }

        // 3. Obtain FileProvider Content Uri
        if (imageFile.exists() && imageFile.length() > 0L) {
            try {
                imageContentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            } catch (_: Exception) {}
        }

        // 4. Build Send Intent with rich extras and ClipData
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, cleanTitle)
            putExtra(Intent.EXTRA_TITLE, cleanTitle)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            putExtra(Intent.EXTRA_HTML_TEXT, htmlShareMessage)

            if (imageContentUri != null) {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageContentUri)
                val clipItem = ClipData.Item(shareMessage, htmlShareMessage, null, imageContentUri)
                clipData = ClipData(
                    ClipDescription(cleanTitle, arrayOf("image/jpeg", "image/*", "text/plain", "text/html")),
                    clipItem
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
                val clipItem = ClipData.Item(shareMessage, htmlShareMessage, null, null)
                clipData = ClipData(
                    ClipDescription(cleanTitle, arrayOf("text/plain", "text/html")),
                    clipItem
                )
            }
        }

        // 5. Build Chooser and grant URI permissions to all matching target apps
        val chooser = Intent.createChooser(sendIntent, cleanTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (imageContentUri != null) {
                clipData = sendIntent.clipData
            }
        }

        if (imageContentUri != null) {
            try {
                val resInfoList = context.packageManager.queryIntentActivities(
                    sendIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (resolveInfo in resInfoList) {
                    val pkg = resolveInfo.activityInfo.packageName
                    try {
                        context.grantUriPermission(pkg, imageContentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }

        withContext(Dispatchers.Main) {
            context.startActivity(chooser)
        }
    }

    /**
     * Backward-compatible synchronous share intent creator with clickable links.
     */
    fun createShareIntent(postId: Long, cleanTitle: String, permalink: String? = null): Intent {
        val shareMessage = buildShareMessage(postId, cleanTitle, permalink)
        val htmlShareMessage = buildHtmlShareMessage(postId, cleanTitle, permalink)
        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, cleanTitle)
            putExtra(Intent.EXTRA_TITLE, cleanTitle)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            putExtra(Intent.EXTRA_HTML_TEXT, htmlShareMessage)
            clipData = ClipData(
                ClipDescription(cleanTitle, arrayOf("text/plain", "text/html")),
                ClipData.Item(shareMessage, htmlShareMessage, null, null)
            )
            type = "text/plain"
        }
    }
}
