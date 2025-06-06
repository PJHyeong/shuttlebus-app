package com.example.shuttlebusapplication.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.shuttlebusapplication.model.NoticeItem
import com.example.shuttlebusapplication.model.NoticeRequest
import com.example.shuttlebusapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeViewModel(application: Application) : AndroidViewModel(application) {

    // 공지사항 목록을 담는 LiveData
    val notices = MutableLiveData<List<NoticeItem>>()

    // SharedPreferences 에 저장된 JWT 토큰
    private val prefs = application
        .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // Retrofit API 인터페이스
    private val api = RetrofitClient.apiService

    /** 전체 공지사항 조회 */
    fun fetchNotices() {
        api.getNotices().enqueue(object : Callback<List<NoticeItem>> {
            override fun onResponse(
                call: Call<List<NoticeItem>>,
                response: Response<List<NoticeItem>>
            ) {
                if (response.isSuccessful) {
                    notices.value = response.body()
                } else {
                    Log.e("NoticeViewModel", "fetchNotices failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<NoticeItem>>, t: Throwable) {
                Log.e("NoticeViewModel", "fetchNotices onFailure", t)
            }
        })
    }

    /** 새 공지사항 생성 (관리자) */
    fun createNotice(req: NoticeRequest) {
        val token = prefs.getString("jwt_token", "")?.let { "Bearer $it" } ?: ""
        api.createNotice(token, req).enqueue(object : Callback<NoticeItem> {
            override fun onResponse(
                call: Call<NoticeItem>,
                response: Response<NoticeItem>
            ) {
                if (response.isSuccessful) {
                    fetchNotices()
                } else {
                    Log.e("NoticeViewModel", "createNotice failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NoticeItem>, t: Throwable) {
                Log.e("NoticeViewModel", "createNotice onFailure", t)
            }
        })
    }

    /** 기존 공지사항 수정 (관리자) */
    fun updateNotice(id: String, req: NoticeRequest) {
        val token = prefs.getString("jwt_token", "")?.let { "Bearer $it" } ?: ""
        api.updateNotice(token, id, req).enqueue(object : Callback<NoticeItem> {
            override fun onResponse(
                call: Call<NoticeItem>,
                response: Response<NoticeItem>
            ) {
                if (response.isSuccessful) {
                    fetchNotices()
                } else {
                    Log.e("NoticeViewModel", "updateNotice failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NoticeItem>, t: Throwable) {
                Log.e("NoticeViewModel", "updateNotice onFailure", t)
            }
        })
    }

    /** 공지사항 삭제 (관리자) */
    fun deleteNotice(id: String) {
        val token = prefs.getString("jwt_token", "")?.let { "Bearer $it" } ?: ""
        api.deleteNotice(token, id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchNotices()
                } else {
                    Log.e("NoticeViewModel", "deleteNotice failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("NoticeViewModel", "deleteNotice onFailure", t)
            }
        })
    }
}