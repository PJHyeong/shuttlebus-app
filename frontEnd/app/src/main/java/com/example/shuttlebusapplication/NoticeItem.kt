package com.example.shuttlebusapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoticeItem(
    val id: Int,
    val title: String,
    val date: String,
    val content: String // 내용 전달 필요 시 추가
) : Parcelable
