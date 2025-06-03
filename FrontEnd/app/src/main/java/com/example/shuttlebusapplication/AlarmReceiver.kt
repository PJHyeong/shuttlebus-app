package com.example.shuttlebusapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 인텐트에서 넘어온 셔틀명과 출발시간 꺼내기
        val shuttleName = intent.getStringExtra("shuttleName") ?: "셔틀버스"
        val departureTime = intent.getStringExtra("departureTime") ?: ""

        // NotificationUtil 의 showNotification 호출
        NotificationUtil.showNotification(
            context,
            "$shuttleName 도착 예정",
            "$departureTime 출발 예정 셔틀이 곧 도착합니다."
        )
    }
}
