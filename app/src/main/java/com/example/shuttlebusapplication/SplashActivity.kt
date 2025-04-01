package com.example.shuttlebusapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 여기에 splash 화면 layout을 넣을 수도 있음. (ex. setContentView(R.layout.activity_splash))

        // 바로 MainActivity로 이동
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}