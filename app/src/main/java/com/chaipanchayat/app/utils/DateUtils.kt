package com.chaipanchayat.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private fun parseDate(iso: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val parsed = sdf.parse(iso)
                if (parsed != null) return parsed
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun timeAgo(iso: String): String {
        val date = parseDate(iso) ?: return iso
        val diff = (System.currentTimeMillis() - date.time).coerceAtLeast(0)
        val min = diff / 60000
        if (min < 1) return "just now"
        if (min < 60) return "$min min ago"
        val hr = min / 60
        if (hr < 24) return "$hr hour${if (hr == 1L) "" else "s"} ago"
        val days = hr / 24
        if (days < 7) return "$days day${if (days == 1L) "" else "s"} ago"
        return formatLongDate(iso)
    }

    fun getRelativeTime(iso: String): String = timeAgo(iso)

    fun formatLongDate(iso: String): String {
        val date = parseDate(iso) ?: return iso
        val outSdf = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
        return outSdf.format(date)
    }

    fun formatDateTime(iso: String): String {
        val date = parseDate(iso) ?: return iso
        val outSdf = SimpleDateFormat("d MMMM yyyy · h:mm a", Locale.ENGLISH)
        return outSdf.format(date)
    }
}
