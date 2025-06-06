// 파일: app/src/main/java/com/example/shuttlebusapplication/model/Arrival.kt
package com.example.shuttlebusapplication.model

/**
 * 정류장에 도착 예정인 셔틀 정보를 담는 데이터 클래스
 *
 * @param shuttleName 화면에 표시할 셔틀 이름 (예: "명지대역 셔틀")
 * @param etaSec      남은 도착 시간(초 단위) (예: 300 = 5분)
 */
data class Arrival(
    val shuttleName: String,
    val etaSec: Long
)
