package com.example.shuttlebusapplication.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtils {

    fun getRelativeTime(dateString: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            val past = format.parse(dateString)
            val now = Date()
            val diff = now.time - past.time

            when {
                diff < 60 * 1000 -> "방금 전"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}분 전"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}시간 전"
                else -> "${diff / (24 * 60 * 60 * 1000)}일 전"
            }
        } catch (e: Exception) {
            ""
        }
    }
}