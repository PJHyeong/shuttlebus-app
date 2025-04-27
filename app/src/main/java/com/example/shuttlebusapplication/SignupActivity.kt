package com.example.shuttlebusapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignupActivity : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var textNicknameError: TextView
    private lateinit var btnSignUp: Button
    private lateinit var switchPush: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNickname = findViewById(R.id.etNickname)
        textNicknameError = findViewById(R.id.textNicknameError)
        btnSignUp = findViewById(R.id.btnSignUp)
        switchPush = findViewById(R.id.switchPush)

        btnSignUp.isEnabled = false

        // 🔥 닉네임 입력 감시
        etNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateNickname()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🔙 뒤로가기 버튼 (로그인 화면)
        findViewById<ImageButton>(R.id.btnBackToLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // ✅ 가입하기 버튼 클릭
        btnSignUp.setOnClickListener {
            val nickname = etNickname.text.toString()
            val pushAgree = switchPush.isChecked

            // SharedPreferences에 저장
            val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("nickname", nickname)
            editor.putBoolean("pushAgree", pushAgree)
            editor.apply()

            // 🌟 "회원가입 완료" Toast 띄우기
            Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()

            // 🌟 로그인 화면으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // ✨ 닉네임 유효성 검사
    private fun validateNickname() {
        val nickname = etNickname.text.toString()

        when {
            nickname.isBlank() -> {
                textNicknameError.text = "닉네임을 입력해주세요."
                textNicknameError.visibility = TextView.VISIBLE
                btnSignUp.isEnabled = false
            }
            nickname.contains(" ") -> {
                textNicknameError.text = "닉네임에 공백을 포함할 수 없습니다."
                textNicknameError.visibility = TextView.VISIBLE
                btnSignUp.isEnabled = false
            }
            nickname.length < 2 -> {
                textNicknameError.text = "닉네임은 2자 이상이어야 합니다."
                textNicknameError.visibility = TextView.VISIBLE
                btnSignUp.isEnabled = false
            }
            nickname.length > 10 -> {
                textNicknameError.text = "닉네임은 10자 이하로 입력해주세요."
                textNicknameError.visibility = TextView.VISIBLE
                btnSignUp.isEnabled = false
            }
            nickname.contains(Regex("[^a-zA-Z0-9가-힣]")) -> {
                textNicknameError.text = "특수문자는 사용할 수 없습니다."
                textNicknameError.visibility = TextView.VISIBLE
                btnSignUp.isEnabled = false
            }
            else -> {
                textNicknameError.text = ""
                textNicknameError.visibility = TextView.GONE
                btnSignUp.isEnabled = true
            }
        }
    }
}
