// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/LoginActivity.kt

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

        // ─────────────────────────────────────────────────────────────
        // 1) SharedPreferences에서 저장된 JWT 토큰과 '자동 로그인' 여부 가져오기
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedToken = prefs.getString("jwt_token", null)
        val autoLoginEnabled = prefs.getBoolean("auto_login_enabled", false)

        // 2) 만약 토큰이 있고, '자동 로그인'이 켜져 있으면 곧바로 MainActivity로 이동
        if (!savedToken.isNullOrEmpty() && autoLoginEnabled) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // ─────────────────────────────────────────────────────────────
        // 3) 로그인 화면 UI 초기화 (기존 레이아웃 그대로 사용)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // 3-1) '회원가입' 텍스트에 클릭 가능한 스팬 처리
        val findAccountTextView = findViewById<TextView>(R.id.find_account)
        val fullText = "회원가입"
        val spannableString = SpannableString(fullText)
        val signupStart = fullText.indexOf("회원가입")
        val signupEnd = signupStart + "회원가입".length

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginActivity, "회원가입 화면으로 이동", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            }
        }, signupStart, signupEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        findAccountTextView.text = spannableString
        findAccountTextView.movementMethod = LinkMovementMethod.getInstance()
        findAccountTextView.setHighlightColor(Color.TRANSPARENT)

        // 3-2) 로그인 관련 뷰 가져오기
        val emailEditText = findViewById<EditText>(R.id.id_input)
        val passwordEditText = findViewById<EditText>(R.id.password_input)
        val autoLoginCheckbox = findViewById<CheckBox>(R.id.auto_login_checkbox)
        val btnLogin = findViewById<Button>(R.id.login_button)

        // 3-3) SharedPreferences에 저장된 이전 아이디/비밀번호가 있으면 미리 채워주기
        val savedId = prefs.getString("saved_id", "")
        val savedPw = prefs.getString("saved_pw", "")
        if (!savedId.isNullOrEmpty()) {
            emailEditText.setText(savedId)
        }
        if (!savedPw.isNullOrEmpty()) {
            passwordEditText.setText(savedPw)
        }
        // 체크박스는 이전에 저장된 auto_login_enabled 값을 그대로 적용
        autoLoginCheckbox.isChecked = autoLoginEnabled

        // ─────────────────────────────────────────────────────────────
        // 4) 로그인 버튼 클릭 시 Retrofit을 이용한 API 호출
        btnLogin.setOnClickListener {
            val studentid = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (studentid.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(studentid, password)
            RetrofitClient.apiService.loginUser(loginRequest)
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                val user = body.user      // user는 non-null
                                val token = body.token

                                // ─────────────────────────────────────────
                                // 4-1) SharedPreferences에 로그인 정보 저장
                                prefs.edit().apply {
                                    putString("jwt_token", token)
                                    putString("nickname", user.name)
                                    putBoolean("isAdmin", user.role == "admin")

                                    // '자동 로그인' 체크 상태 저장
                                    putBoolean("auto_login_enabled", autoLoginCheckbox.isChecked)
                                    if (autoLoginCheckbox.isChecked) {
                                        // 자동 로그인 ON → 아이디/비밀번호도 저장
                                        putString("saved_id", studentid)
                                        putString("saved_pw", password)
                                    } else {
                                        // 자동 로그인 OFF → 삭제
                                        remove("saved_id")
                                        remove("saved_pw")
                                    }
                                    apply()
                                }

                                Toast.makeText(
                                    this@LoginActivity,
                                    "환영합니다 ${user.name}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // ─────────────────────────────────────────
                                // 4-2) MainActivity로 이동
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "로그인 응답이 올바르지 않습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "로그인 실패: 아이디 또는 비밀번호를 확인하세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(
                            this@LoginActivity,
                            "서버 연결 실패: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}
