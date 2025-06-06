package com.example.shuttlebusapplication.repository

import com.example.shuttlebusapplication.model.ShuttleSchedule

class ShuttleRepository {

    // 1. 명지대 → 기흥역
    fun getRoute1(): List<ShuttleSchedule> = listOf(
        ShuttleSchedule(1, "08:00", expectedArrivalTime = "08:15", vehicleCount = 1),
        ShuttleSchedule(2, "09:05", expectedArrivalTime = "09:20", vehicleCount = 3),
        ShuttleSchedule(3, "09:10", expectedArrivalTime = "09:25", vehicleCount = 2),
        ShuttleSchedule(4, "10:00", expectedArrivalTime = "10:15", vehicleCount = 3),
        ShuttleSchedule(5, "10:05", expectedArrivalTime = "10:20", vehicleCount = 2),
        ShuttleSchedule(6, "12:00", expectedArrivalTime = "12:15", vehicleCount = 1),
        ShuttleSchedule(7, "13:00", expectedArrivalTime = "13:15", vehicleCount = 1),
        ShuttleSchedule(8, "14:00", expectedArrivalTime = "14:15", vehicleCount = 1),
        ShuttleSchedule(9, "15:15", expectedArrivalTime = "15:30", vehicleCount = 2),
        ShuttleSchedule(10, "16:15", expectedArrivalTime = "16:30", vehicleCount = 3),
        ShuttleSchedule(11, "17:15", expectedArrivalTime = "17:30", vehicleCount = 5),
        ShuttleSchedule(12, "18:15", expectedArrivalTime = "18:30", vehicleCount = 5),
        ShuttleSchedule(13, "19:15", expectedArrivalTime = "19:30", vehicleCount = 1)

    )


    // 2. 기흥역 → 명지대 (도착예정시간: 출발 + 15분)
    fun getRoute2(): List<ShuttleSchedule> = listOf(
        ShuttleSchedule(1, "08:15", expectedArrivalTime = "08:30", vehicleCount = 3),
        ShuttleSchedule(2, "08:20", expectedArrivalTime = "08:35", vehicleCount = 2),
        ShuttleSchedule(3, "09:15", expectedArrivalTime = "09:30", vehicleCount = 3),
        ShuttleSchedule(4, "09:20", expectedArrivalTime = "09:35", vehicleCount = 2),
        ShuttleSchedule(5, "10:15", expectedArrivalTime = "10:30", vehicleCount = 3),
        ShuttleSchedule(6, "10:20", expectedArrivalTime = "10:35", vehicleCount = 2),
        ShuttleSchedule(7, "12:15", expectedArrivalTime = "12:30", vehicleCount = 1),
        ShuttleSchedule(8, "13:15", expectedArrivalTime = "13:30", vehicleCount = 1),
        ShuttleSchedule(9, "14:15", expectedArrivalTime = "14:30", vehicleCount = 1),
        ShuttleSchedule(10, "15:30", expectedArrivalTime = "15:45", vehicleCount = 2),
        ShuttleSchedule(11, "16:30", expectedArrivalTime = "16:45", vehicleCount = 3),
        ShuttleSchedule(12, "17:30", expectedArrivalTime = "17:45", vehicleCount = 1),
        ShuttleSchedule(13, "18:30", expectedArrivalTime = "18:45", vehicleCount = 1),
        ShuttleSchedule(14, "19:30", expectedArrivalTime = "19:45", vehicleCount = 1)
    )

    // 3. 시내 셔틀
    fun getRoute3(): List<ShuttleSchedule> = listOf(
        ShuttleSchedule(1, "08:05", viaTime = "08:20"),
        ShuttleSchedule(2, "08:55", viaTime = "09:10"),
        ShuttleSchedule(3, "10:10", viaTime = "10:25"),
        ShuttleSchedule(4, "11:20", viaTime = "11:35"),
        ShuttleSchedule(5, "13:10", viaTime = "13:25"),
        ShuttleSchedule(6, "14:20", viaTime = "14:35"),
        ShuttleSchedule(7, "15:40", viaTime = "15:55"),
        ShuttleSchedule(8, "16:35", viaTime = "16:50"),
        ShuttleSchedule(9, "18:10", viaTime = "18:35"),
        ShuttleSchedule(10, "20:00", viaTime = "20:15"),
        // ... 필요 시 추가
    )

    // 4. 진입로 셔틀 명지대역
    fun getRoute4(): List<ShuttleSchedule> = listOf(
        ShuttleSchedule(1, "08:00", viaTime = "08:15"),
        ShuttleSchedule(2, "08:15", viaTime = "08:30"),
        ShuttleSchedule(3, "08:20", viaTime = "08:35"),
        ShuttleSchedule(4, "08:25", viaTime = "08:40"),
        ShuttleSchedule(5, "08:35", viaTime = "08:50"),
        ShuttleSchedule(6, "08:45", viaTime = "09:00"),
        ShuttleSchedule(7, "08:50", viaTime = "09:05"),
        ShuttleSchedule(8, "09:00", viaTime = "09:15"),
        ShuttleSchedule(9, "09:15", viaTime = "09:30"),
        ShuttleSchedule(10, "09:25", viaTime = "09:40"),
        ShuttleSchedule(11, "09:30", viaTime = "09:45"),
        ShuttleSchedule(12, "09:35", viaTime = "09:50"),
        ShuttleSchedule(13, "09:40", viaTime = "09:55"),
        ShuttleSchedule(14, "09:55", viaTime = "10:10"),
        ShuttleSchedule(15, "10:00", viaTime = "10:15"),
        ShuttleSchedule(16, "10:20", viaTime = "10:35"),
        ShuttleSchedule(17, "10:30", viaTime = "10:45"),
        ShuttleSchedule(18, "10:40", viaTime = "10:55"),
        ShuttleSchedule(19, "10:45", viaTime = "11:00"),
        ShuttleSchedule(20, "11:00", viaTime = "11:15"),
        ShuttleSchedule(21, "11:25", viaTime = "11:40"),
        ShuttleSchedule(22, "11:30", viaTime = "11:45"),
        ShuttleSchedule(23, "11:45", viaTime = "12:00"),
        ShuttleSchedule(24, "11:55", viaTime = "12:10"),
        ShuttleSchedule(25, "12:05", viaTime = "12:20"),
        ShuttleSchedule(26, "12:20", viaTime = "12:35"),
        ShuttleSchedule(27, "12:30", viaTime = "12:45"),
        ShuttleSchedule(28, "12:45", viaTime = "13:00"),
        ShuttleSchedule(29, "13:00", viaTime = "13:15"),
        ShuttleSchedule(30, "13:25", viaTime = "13:40"),
        ShuttleSchedule(31, "13:40", viaTime = "13:55"),
        ShuttleSchedule(32, "14:00", viaTime = "14:15"),
        ShuttleSchedule(33, "14:10", viaTime = "14:25"),
        ShuttleSchedule(34, "14:15", viaTime = "14:30"),
        ShuttleSchedule(35, "14:30", viaTime = "14:45"),
        ShuttleSchedule(36, "14:50", viaTime = "15:05"),
        ShuttleSchedule(37, "15:00", viaTime = "15:15"),
        ShuttleSchedule(38, "15:10", viaTime = "15:25"),
        ShuttleSchedule(39, "15:25", viaTime = "15:40"),
        ShuttleSchedule(40, "15:30", viaTime = "15:45"),
        ShuttleSchedule(41, "15:55", viaTime = "16:10"),
        ShuttleSchedule(42, "16:10", viaTime = "16:25"),
        ShuttleSchedule(43, "16:25", viaTime = "16:40"),
        ShuttleSchedule(44, "16:30", viaTime = "16:45"),
        ShuttleSchedule(45, "16:50", viaTime = "17:05"),
        ShuttleSchedule(46, "17:00", viaTime = "17:15"),
        ShuttleSchedule(47, "17:10", viaTime = "17:25"),
        ShuttleSchedule(48, "17:20", viaTime = "17:35"),
        ShuttleSchedule(49, "17:30", viaTime = "17:45"),
        ShuttleSchedule(50, "17:45", viaTime = "18:00"),
        ShuttleSchedule(51, "18:00", viaTime = "18:15"),
        ShuttleSchedule(52, "19:00", viaTime = "19:15"),
        ShuttleSchedule(53, "19:20", viaTime = "19:35"),
        ShuttleSchedule(54, "19:30", viaTime = "19:45"),
    )


    // 5. 주말/공휴일
    fun getRoute5(): List<ShuttleSchedule> = listOf(
        ShuttleSchedule(1, "08:20", expectedArrivalTime = "08:45"),
        ShuttleSchedule(2, "09:20", expectedArrivalTime = "09:45"),
        ShuttleSchedule(3, "10:20", expectedArrivalTime = "10:45"),
        ShuttleSchedule(4, "11:20", expectedArrivalTime = "11:45"),
        ShuttleSchedule(5, "12:20", expectedArrivalTime = "12:45"),
        ShuttleSchedule(6, "13:20", expectedArrivalTime = "13:45"),
        ShuttleSchedule(7, "15:20", expectedArrivalTime = "15:45"),
        ShuttleSchedule(8, "16:20", expectedArrivalTime = "16:45"),
        ShuttleSchedule(9, "17:20", expectedArrivalTime = "17:45"),
        ShuttleSchedule(10, "18:00", expectedArrivalTime = "18:25")
    )
}
