package com.example.shuttlebusapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.shuttlebusapplication.model.UserRequest
import com.example.shuttlebusapplication.model.UserResponse
import com.example.shuttlebusapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NicknameActivity : AppCompatActivity() {
    private lateinit var etNickname: EditText
    private lateinit var textNicknameError: TextView
    private lateinit var btnComplete: Button

    private var studentId: String = ""
    private var password: String = ""
    private var pushAgree: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        etNickname = findViewById(R.id.etNickname)
        textNicknameError = findViewById(R.id.textNicknameError)
        btnComplete = findViewById(R.id.btnComplete)
        btnComplete.isEnabled = false

        // 1. Intent로 데이터 받기
        studentId = intent.getStringExtra("studentId") ?: ""
        password = intent.getStringExtra("password") ?: ""
        pushAgree = intent.getBooleanExtra("pushAgree", false)

        etNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateNickname()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnComplete.setOnClickListener {
            val nickname = etNickname.text.toString()
            // 2. 서버로 회원가입 요청
            val userRequest = UserRequest(
                studentid = studentId,
                password = password,
                name = nickname // 'name' 필드를 nickname으로 활용
            )
            RetrofitClient.apiService.registerUser(userRequest).enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@NicknameActivity, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@NicknameActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        textNicknameError.text = "회원가입에 실패했습니다: ${response.message()}"
                        textNicknameError.visibility = TextView.VISIBLE
                    }
                }
                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    textNicknameError.text = "네트워크 오류: ${t.localizedMessage}"
                    textNicknameError.visibility = TextView.VISIBLE
                }
            })
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
