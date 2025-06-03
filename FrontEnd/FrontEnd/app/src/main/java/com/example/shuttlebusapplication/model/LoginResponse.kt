package com.example.shuttlebusapplication.model

data class LoginResponse(
    val token: String,
    val user: UserInfo, // UserInfo 클래스를 사용하여 사용자 정보를 포함합니다.
)