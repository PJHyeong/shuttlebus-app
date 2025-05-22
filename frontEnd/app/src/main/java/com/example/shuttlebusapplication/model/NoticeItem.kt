package com.example.shuttlebusapplication.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoticeItem(
    @SerializedName("_id")       val id: String,
    val title:                    String,
    @SerializedName("createdAt") val date: String,
    val content:                  String
) : Parcelable