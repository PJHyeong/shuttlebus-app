package com.example.shuttlebusapplication.model

data class LoginResponse(
    val token: String,
    val user: UserInfo
)