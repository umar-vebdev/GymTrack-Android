package com.example.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun String.formatAsReadableDate(): String {
    return try {
        // Try parsing full ISO datetime
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(this)
        
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        date?.let { formatter.format(it) } ?: this
    } catch (e: Exception) {
        // Fallback for simple date (e.g. 2026-08-06)
        try {
            val parser2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser2.parse(this)
            val formatter2 = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            date?.let { formatter2.format(it) } ?: this
        } catch (e2: Exception) {
            this
        }
    }
}
