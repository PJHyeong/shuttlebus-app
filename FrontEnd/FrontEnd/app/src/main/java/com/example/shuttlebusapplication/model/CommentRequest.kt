package com.example.shuttlebusapplication.model

data class CommentRequest(
    val announcementId: String,
    val userId: String,  // 현재 사용자의 ID
    val content: String
)