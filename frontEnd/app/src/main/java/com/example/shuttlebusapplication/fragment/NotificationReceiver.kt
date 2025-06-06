// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/fragment/NotificationReceiver.kt

package com.example.shuttlebusapplication.fragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.shuttlebusapplication.R

/**
 * AlarmManager가 예약된 시점에 브로드캐스트를 보내면 이 리시버가 호출됩니다.
 * 인텐트의 extras("bus_name", "station_name")를 읽어와서
 * 로컬 Notification을 생성하고 띄워 줍니다.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "bus_arrival_channel"
        private const val CHANNEL_NAME = "셔틀 도착 알림"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 1) Intent에서 bus_name, station_name 읽어오기
        val busName = intent.getStringExtra("bus_name") ?: "셔틀"
        val stationName = intent.getStringExtra("station_name") ?: "정류장"

        // 2) NotificationManager 가져오기
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 3) 안드로이드 8.0 (Oreo) 이상용 NotificationChannel 생성
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "버스가 곧 도착할 때 알려주는 알림 채널"
            }
            nm.createNotificationChannel(channel)
        }

        // 4) Notification 빌드
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // 내장 아이콘(android.R.drawable.ic_dialog_info)을 사용
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("셔틀 도착 알림")
            .setContentText("$stationName 에 $busName 이잠시후 도착합니다.")
            .setAutoCancel(true)
            .build()

        // 5) 알림 표시 (unique ID로 busName+stationName 해시코드 사용)
        val notificationId = (busName + stationName).hashCode()
        nm.notify(notificationId, notification)
    }
}
