package com.example.shuttlebusapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shuttlebusapplication.model.NoticeItem

class NoticeViewModel : ViewModel() {

    private val _notices = MutableLiveData<MutableList<NoticeItem>>().apply {
        value = mutableListOf(
            *(1..53).map {
                NoticeItem(
                    id = it,
                    title = "공지 제목 $it",
                    date = "2025-05-${(it % 30 + 1).toString().padStart(2, '0')}",
                    content = "이것은 공지 ${it}의 더미 내용입니다."
                )
            }.toTypedArray()
        )
    }

    val notices: LiveData<MutableList<NoticeItem>> get() = _notices

    fun addNotice(notice: NoticeItem) {
        _notices.value?.add(0, notice) // 가장 위에 추가
        _notices.postValue(_notices.value)
    }

    fun deleteNotice(id: Int) {
        _notices.value?.removeIf { it.id == id }
        _notices.postValue(_notices.value)
    }
}
