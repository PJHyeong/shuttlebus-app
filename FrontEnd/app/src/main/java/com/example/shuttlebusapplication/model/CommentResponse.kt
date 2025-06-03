package com.example.shuttlebusapplication.model

data class CommentResponse(
    val id: String,
    val userId: String,
    val content: String,
    val createdAt: String
)
