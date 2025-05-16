package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.adapter.NoticeAdapter
import com.example.shuttlebusapplication.databinding.FragmentNoticeBinding
import com.example.shuttlebusapplication.model.NoticeItem

class NoticeFragment : Fragment(R.layout.fragment_notice) {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    // 테스트용 관리자 플래그 (실제론 세션/로그인 정보에서 받아옴)
    private val isAdmin = false

    private val allNotices = List(53) {  // 예시: 53개 더미 데이터
        NoticeItem(
            id = it + 1,
            title = "공지 제목 ${it + 1}",
            date  = "2025-05-${(it % 30) + 1}".padStart(10, '0')
        )
    }

    private var currentPage = 1
    private val pageSize = 10
    private val totalPages get() = (allNotices.size + pageSize - 1) / pageSize

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentNoticeBinding.bind(view)

        // 메인메뉴 버튼
        binding.btnMenu.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // RecyclerView 세팅
        binding.recyclerViewNotice.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 페이지 버튼
        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updateList()
            }
        }
        binding.btnNextPage.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                updateList()
            }
        }

        // 최초 데이터 표시
        updateList()
    }

    private fun updateList() {
        // 페이징 처리
        val start = (currentPage - 1) * pageSize
        val pageData = allNotices.subList(
            start,
            minOf(start + pageSize, allNotices.size)
        )

        binding.textPageInfo.text = "$currentPage / $totalPages"

        binding.recyclerViewNotice.adapter = NoticeAdapter(
            isAdmin, pageData,
            onItemClick = { notice ->
                // TODO: 상세 화면으로 이동
            },
            onEditClick = { notice ->
                // TODO: 수정 로직
            },
            onDeleteClick = { notice ->
                // TODO: 삭제 로직
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
