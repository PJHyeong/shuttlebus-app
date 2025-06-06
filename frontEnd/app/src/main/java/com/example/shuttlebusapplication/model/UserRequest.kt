package com.example.shuttlebusapplication.model

data class UserRequest(
    val studentid: String,
    val password: String,
    val name: String? = null // 회원가입 할 때만 필요하므로 기본값을 null로 설정합니다.
)