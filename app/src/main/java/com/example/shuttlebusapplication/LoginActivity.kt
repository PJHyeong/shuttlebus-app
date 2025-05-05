package com.example.shuttlebusapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.text.SpannableString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val findAccountTextView = findViewById<TextView>(R.id.find_account)
        val fullText = "아이디 찾기 | 비밀번호 찾기\n회원가입"
        val spannableString = SpannableString(fullText)



        // 로그인 버튼 클릭 시 메인 화면으로 이동
        val btnLogin = findViewById<Button>(R.id.login_button)
        btnLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 뒤로가기 시 로그인 화면 안 나오도록
        }

        val signupStart = fullText.indexOf("회원가입")
        val signupEnd = signupStart + "회원가입".length

        // "아이디 찾기" 클릭 이벤트
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginActivity, "아이디 찾기 시작", Toast.LENGTH_SHORT).show()
            }
        }, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // "아이디 찾기"

        // "비밀번호 찾기" 클릭 이벤트
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginActivity, "비밀번호 찾기 시작", Toast.LENGTH_SHORT).show()
            }
        }, 9, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // "비밀번호 찾기"

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginActivity, "회원가입 화면으로 이동", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(intent)
            }
        }, signupStart, signupEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


        findAccountTextView.text = spannableString
        findAccountTextView.movementMethod = LinkMovementMethod.getInstance() // 필수: 클릭 가능하게 설정
        findAccountTextView.setHighlightColor(Color.TRANSPARENT) // 클릭 시 배경색 제거
    }
}