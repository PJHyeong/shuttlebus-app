package com.example.login

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.widget.TextView
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.View



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val kakaoButton = findViewById<ImageView>(R.id.kakao_login)
        kakaoButton.setOnClickListener {

            Toast.makeText(this, "카카오 계정 로그인", Toast.LENGTH_LONG).show()
        }

        val naverButton = findViewById<ImageView>(R.id.naver_login)
        naverButton.setOnClickListener {

            Toast.makeText(this, "네이버 계정 로그인", Toast.LENGTH_LONG).show()
        }

        val findAccountTextView = findViewById<TextView>(R.id.find_account)

        val text = "아이디 찾기 | 비밀번호 찾기\n회원가입"
        val spannableString = SpannableString(text)

        // "아이디 찾기" 클릭 이벤트
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@MainActivity, "아이디 찾기 시작", Toast.LENGTH_SHORT).show()
            }
        }, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // "아이디 찾기"

        // "비밀번호 찾기" 클릭 이벤트
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@MainActivity, "비밀번호 찾기 시작", Toast.LENGTH_SHORT).show()
            }
        }, 9, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // "비밀번호 찾기"

        // "회원가입" 클릭 이벤트
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@MainActivity, "회원 가입 진행", Toast.LENGTH_SHORT).show()
            }
        }, 16, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // "회원가입"

        findAccountTextView.text = spannableString
        findAccountTextView.movementMethod = LinkMovementMethod.getInstance() // 필수: 클릭 가능하게 설정
        findAccountTextView.setHighlightColor(Color.TRANSPARENT) // 클릭 시 배경색 제거

    }
}