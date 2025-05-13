package com.example.shuttlebusapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.shuttlebusapplication.R

object NotificationUtil {

    private const val CHANNEL_ID = "shuttle_channel"

    /**
     * 앱 런치 시 한 번만 호출하세요.
     * Android O 이상에서만 동작하므로 minSdk가 26 이상이면
     * SDK_INT 체크는 사실 불필요하지만, lint를 위해 남깁니다.
     */
    @SuppressLint("ObsoleteSdkInt")
    @JvmStatic
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Shuttle Notification"
            val descriptionText = "셔틀버스 도착 알림"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Android 13 이상에서는 POST_NOTIFICATIONS 권한이 필요합니다.
     * 호출 전 권한 체크가 안 된다면 알림이 표시되지 않습니다.
     */
    @JvmStatic
    fun showNotification(context: Context, title: String, message: String) {
        // Android 13+ 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            // 반드시 흰색 실루엣 벡터 아이콘을 넣어야 상태바에 제대로 표시됩니다
            .setSmallIcon(R.drawable.ic_notification_shuttle)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
