package com.example.shuttlebusapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.shuttlebusapplication.model.LoginRequest
import com.example.shuttlebusapplication.model.LoginResponse
import com.example.shuttlebusapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // 회원가입 텍스트
        val findAccountTextView = findViewById<TextView>(R.id.find_account)
        val fullText = "회원가입"
        val spannableString = SpannableString(fullText)
        val signupStart = fullText.indexOf("회원가입")
        val signupEnd = signupStart + "회원가입".length

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginActivity, "회원가입 화면으로 이동", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(intent)
            }
        }, signupStart, signupEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        findAccountTextView.text = spannableString
        findAccountTextView.movementMethod = LinkMovementMethod.getInstance()
        findAccountTextView.setHighlightColor(Color.TRANSPARENT)

        // 로그인 관련 뷰
        val emailEditText = findViewById<EditText>(R.id.id_input)
        val passwordEditText = findViewById<EditText>(R.id.password_input)
        val btnLogin = findViewById<Button>(R.id.login_button)

        // 로그인 버튼 클릭 시 API 요청
        btnLogin.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(email, password)

            RetrofitClient.authApi.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val user = response.body()?.user
                        Toast.makeText(this@LoginActivity, "환영합니다 ${user?.name}", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패: 아이디 또는 비밀번호 확인", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
