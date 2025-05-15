package com.example.shuttlebusapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.text.SpannableString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.shuttlebusapplication.model.LoginRequest
import com.example.shuttlebusapplication.model.LoginResponse
import com.example.shuttlebusapplication.network.RetrofitClient


class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // EditText 초기화
        emailEditText = findViewById(R.id.id_input)
        passwordEditText = findViewById(R.id.password_input)

        val findAccountTextView = findViewById<TextView>(R.id.find_account)
        val fullText = "회원가입"
        val spannableString = SpannableString(fullText)

        // 로그인 버튼 클릭 시 API 호출
        val btnLogin = findViewById<Button>(R.id.login_button)
        btnLogin.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            // 입력된 이메일과 비밀번호로 로그인 함수 호출
            loginUser(email, password)
        }

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
        findAccountTextView.movementMethod = LinkMovementMethod.getInstance() // 필수: 클릭 가능하게 설정
        findAccountTextView.setHighlightColor(Color.TRANSPARENT) // 클릭 시 배경색 제거
    }

    private fun loginUser(email: String, password: String) {
        // 빈 입력 처리
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val loginRequest = LoginRequest(email, password) // LoginRequest 데이터 클래스 사용

        val call = RetrofitClient.apiService.loginUser(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    val token = userResponse?.token  // JWT 토큰
                    val userInfo = userResponse?.user  // 사용자 정보

                    // 로그인 성공 후 처리
                    Toast.makeText(this@LoginActivity, "로그인 성공: ${userInfo?.name}", Toast.LENGTH_SHORT).show()

                    // 메인 화면으로 이동
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() // 뒤로가기 시 로그인 화면이 안 나타나게 함
                } else {
                    Toast.makeText(this@LoginActivity, "로그인 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}