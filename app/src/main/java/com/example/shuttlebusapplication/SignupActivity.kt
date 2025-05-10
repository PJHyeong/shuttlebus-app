package com.example.shuttlebusapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class SignupActivity : AppCompatActivity() {

    private lateinit var etStudentId: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var textStudentIdError: TextView
    private lateinit var textPasswordError: TextView
    private lateinit var switchPush: Switch
    private lateinit var btnSignUp: Button
    private lateinit var btnBackToLogin: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // UI 연결
        etStudentId = findViewById(R.id.etStudentId)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        textStudentIdError = findViewById(R.id.textStudentIdError)
        textPasswordError = findViewById(R.id.textPasswordError)
        switchPush = findViewById(R.id.switchPush)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)

        // 🔙 로그인으로 이동
        btnBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 🔍 입력 필드 감시
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateInput()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etStudentId.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)

        // 🔐 가입 버튼 클릭
        btnSignUp.setOnClickListener {
            val studentId = etStudentId.text.toString()
            val password = etPassword.text.toString()
            val pushAgree = switchPush.isChecked

            Log.d("SignupActivity", "회원가입 완료됨 → 닉네임 입력으로 이동")
            val intent = Intent(this, NicknameActivity::class.java)
            intent.putExtra("studentId", studentId)
            intent.putExtra("password", password)
            intent.putExtra("pushAgree", pushAgree)
            startActivity(intent)

            // ❌ finish() 제거함 — 닉네임 화면이 제대로 뜨도록 하기 위해
        }
    }

    // ✅ 입력값 유효성 검사
    private fun validateInput() {
        val studentId = etStudentId.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        var isValid = true

        if (!Regex("^60\\d{6}$").matches(studentId)) {
            textStudentIdError.text = "학번은 숫자 8자리이며 '60'으로 시작해야 합니다."
            textStudentIdError.visibility = TextView.VISIBLE
            isValid = false
        } else {
            textStudentIdError.visibility = TextView.GONE
        }

        if (!Regex("^[a-zA-Z0-9]{4,}$").matches(password)) {
            textPasswordError.text = "비밀번호는 4자 이상, 영어/숫자만 가능합니다."
            textPasswordError.visibility = TextView.VISIBLE
            isValid = false
        } else if (password != confirmPassword) {
            textPasswordError.text = "비밀번호가 일치하지 않습니다."
            textPasswordError.visibility = TextView.VISIBLE
            isValid = false
        } else {
            textPasswordError.visibility = TextView.GONE
        }

        btnSignUp.isEnabled = isValid
    }
}
