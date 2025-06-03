package com.example.shuttlebusapplication.model

data class ShuttleSchedule(
    val order: Int,
    val departureTime: String,      // 출발 시간
    val expectedArrivalTime: String? = null,
    val viaTime: String? = null,
    val vehicleCount: Int? = null,
    var isFavorite: Boolean = false,
    var isAlarmSet: Boolean = false,
    var shuttleName: String = "기본노선"
)
