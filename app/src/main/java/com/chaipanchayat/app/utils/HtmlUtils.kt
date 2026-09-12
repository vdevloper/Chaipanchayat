package com.chaipanchayat.app.utils

import android.os.Build
import android.text.Html

object HtmlUtils {

    fun decodeEntities(text: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(text).toString()
            }
        } catch (_: Exception) {
            manualDecode(text)
        }
    }

    fun stripHtml(html: String): String {
        val decoded = decodeEntities(html)
        return decoded
            .replace(Regex("<[^>]+>"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun manualDecode(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#8217;", "\u2019")
            .replace("&#8216;", "\u2018")
            .replace("&#8220;", "\u201C")
            .replace("&#8221;", "\u201D")
            .replace("&#8211;", "\u2013")
            .replace("&#8230;", "\u2026")
            .replace("&#039;", "'")
            .replace("&hellip;", "\u2026")
    }
}
