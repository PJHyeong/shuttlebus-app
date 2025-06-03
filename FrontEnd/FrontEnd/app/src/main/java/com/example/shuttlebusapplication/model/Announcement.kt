package com.example.shuttlebusapplication.model

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String // 공지사항의 생성 날짜 (형식에 따라 합당하게 변경)
)