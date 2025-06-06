package com.example.shuttlebusapplication.model

data class DeleteCommentRequest(
    val userId: String,
    val userRole: String // 예: "admin" 또는 "user"
)
