package com.example.shuttlebusapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class NicknameActivity : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var textNicknameError: TextView
    private lateinit var btnComplete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        etNickname = findViewById(R.id.etNickname)
        textNicknameError = findViewById(R.id.textNicknameError)
        btnComplete = findViewById(R.id.btnComplete)

        btnComplete.isEnabled = false

        etNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateNickname()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnComplete.setOnClickListener {
            // 닉네임 중복 확인 + 서버 등록 요청 이후 처리 예정
            Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateNickname() {
        val nickname = etNickname.text.toString()
        val isValid = when {
            nickname.isBlank() -> {
                textNicknameError.text = "닉네임을 입력해주세요."
                false
            }
            nickname.contains(" ") -> {
                textNicknameError.text = "닉네임에 공백을 포함할 수 없습니다."
                false
            }
            nickname.length < 2 -> {
                textNicknameError.text = "닉네임은 2자 이상이어야 합니다."
                false
            }
            nickname.length > 10 -> {
                textNicknameError.text = "닉네임은 10자 이하로 입력해주세요."
                false
            }
            nickname.contains(Regex("[^a-zA-Z0-9가-힣]")) -> {
                textNicknameError.text = "특수문자는 사용할 수 없습니다."
                false
            }
            else -> {
                textNicknameError.text = ""
                true
            }
        }

        textNicknameError.visibility = if (isValid) TextView.GONE else TextView.VISIBLE
        btnComplete.isEnabled = isValid
    }
}
