package com.chaipanchayat.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    // WordPress site 'chaipanchayat.com' operates in Indian Standard Time (IST, UTC+05:30)
    val IST_TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")
    val UTC_TIMEZONE: TimeZone = TimeZone.getTimeZone("UTC")

    /**
     * Parses WordPress ISO dates.
     * If the string has an explicit offset (e.g. 'Z', '+05:30'), it respects it.
     * If naive (no timezone offset), it correctly defaults to Indian Standard Time (Asia/Kolkata).
     */
    fun parseDate(rawIso: String?): Date? {
        if (rawIso.isNullOrBlank()) return null
        val str = rawIso.trim()
        val hasExplicitZone = str.endsWith("Z", ignoreCase = true) ||
                Regex("""[+-]\d{2}:?\d{2}$""").containsMatchIn(str)

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )

        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = if (hasExplicitZone) UTC_TIMEZONE else IST_TIMEZONE
                }
                val parsed = sdf.parse(str)
                if (parsed != null) return parsed
            } catch (_: Exception) {
            }
        }
        return null
    }

    /**
     * Checks if a post date is today or within the last [pastDays] calendar days in IST.
     * E.g. pastDays = 5 checks today plus the last 5 days.
     */
    fun isTodayOrPastDays(iso: String?, pastDays: Int = 5): Boolean {
        val date = parseDate(iso) ?: return false
        val cal = Calendar.getInstance(IST_TIMEZONE).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -pastDays)
        }
        return date.time >= cal.timeInMillis
    }

    /**
     * Calculates authentic, non-made-up relative elapsed time.
     * E.g. "अभी-अभी", "5 मिनट पहले", "2 घंटे पहले", "कल", "3 दिन पहले".
     */
    fun timeAgo(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val date = parseDate(iso) ?: return iso
        val now = System.currentTimeMillis()
        val diff = now - date.time

        // If clock skew or future by a few minutes, treat as just now
        if (diff < 60_000L) {
            return "अभी-अभी"
        }

        val minutes = diff / 60_000L
        if (minutes < 60) {
            return "$minutes मिनट पहले"
        }

        val hours = minutes / 60
        if (hours < 24) {
            return "$hours घंटे पहले"
        }

        val days = hours / 24
        if (days == 1L) {
            return "कल"
        }
        if (days < 7) {
            return "$days दिन पहले"
        }
        if (days < 30) {
            val weeks = days / 7
            return "$weeks सप्ताह पहले"
        }

        return formatLongDate(date)
    }

    fun getRelativeTime(iso: String?): String = timeAgo(iso)

    fun formatLongDate(date: Date): String {
        val outSdf = SimpleDateFormat("d MMMM yyyy", Locale("hi", "IN")).apply {
            timeZone = TimeZone.getDefault()
        }
        return outSdf.format(date)
    }

    fun formatLongDate(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val date = parseDate(iso) ?: return iso
        return formatLongDate(date)
    }

    fun formatDateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val date = parseDate(iso) ?: return iso
        val outSdf = SimpleDateFormat("d MMMM yyyy · h:mm a", Locale("hi", "IN")).apply {
            timeZone = TimeZone.getDefault()
        }
        return outSdf.format(date)
    }
}
