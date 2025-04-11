package com.example.shuttlebusapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // splash 화면 layout 설정
        setContentView(R.layout.activity_splash)

        // 2초 후 LoginActivity로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // 뒤로가기 눌러도 splash로 돌아가지 않게 finish()
        }, 2000) // 2초 = 2000ms
    }
}
