package com.example.shuttlebusapplication.model

// 최상위 응답
data class DirectionResponse(
    val code: Int,
    val message: String,
    val currentDateTime: String,
    val route: Route?
)

// route 객체 안에 traoptimal 리스트
data class Route(
    val traoptimal: List<Traoptimal>?
)

// traoptimal 하나당 guide, path, summary 등이 담김
data class Traoptimal(
    val guide: List<Guide>?,
    val path: List<List<Double>>?,
    val summary: Summary?        // ← 여기에 summary를 추가
)

// guide 항목
data class Guide(
    val distance: Int,
    val duration: Int,
    val instructions: String,
    val pointIndex: Int,
    val type: Int
)

// summary 모델 (필요한 필드만 정의 가능)
data class Summary(
    val distance: Int,
    val duration: Int,        // JSON의 summary.duration을 매핑
    val goal: GoalSummary
)


data class GoalSummary(
    val pointIndex: Int
    // 필요하다면 location, duration 등 다른 필드도 추가 가능
)