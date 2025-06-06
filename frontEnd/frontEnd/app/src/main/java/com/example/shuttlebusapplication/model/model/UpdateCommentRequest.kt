package com.example.shuttlebusapplication.model

data class UpdateCommentRequest(
    val userId: String,
    val userRole: String, // "admin" or "user"
    val content: String
)
