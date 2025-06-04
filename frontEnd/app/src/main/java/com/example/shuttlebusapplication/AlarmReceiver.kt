// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/AlarmReceiver.kt

package com.example.shuttlebusapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 1) 예약 시 넣어 둔 alarmType 구하기 (기본값은 "timetable")
        val alarmType     = intent.getStringExtra("alarmType") ?: "timetable"

        // 2) 공통으로 넘어오는 셔틀 이름
        val shuttleName   = intent.getStringExtra("shuttleName") ?: "셔틀버스"

        // 3) “시간표 알림”인 경우 넘어오는 departureTime
        val departureTime = intent.getStringExtra("departureTime") ?: ""

        // 4) “정류장 알림”인 경우 넘어오는 stationName
        val stationName   = intent.getStringExtra("stationName") ?: ""

        // 5) 제목과 본문을 분기해서 설정
        val title: String
        val body: String

        if (alarmType == "station") {
            // ▶ 정류장 도착 예보 알림
            title = "$stationName 정류장 도착 예정"
            body  = "$shuttleName 셔틀이 곧 $stationName 정류장에 도착합니다."
        } else {
            // ▶ 시간표(출발 3분 전) 알림
            title = "셔틀 시간표 알림"
            body  = "$departureTime 출발 예정 셔틀이 곧 출발합니다."
        }

        // 6) 실제 노티피케이션 띄우기
        NotificationUtil.showNotification(context, title, body)
    }
}
