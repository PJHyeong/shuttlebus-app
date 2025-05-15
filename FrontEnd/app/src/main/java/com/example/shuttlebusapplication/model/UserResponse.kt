package com.example.shuttlebusapplication.model

data class UserResponse(
    val token: String,
    val user: UserInfo // UserInfo 클래스를 사용하여 사용자 정보를 포함합니다.
)

data class UserInfo(
    val id: String,
    val name: String,
    val studentid: String
)