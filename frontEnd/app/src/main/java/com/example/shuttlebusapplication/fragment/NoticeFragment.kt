package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.adapter.NoticeAdapter
import com.example.shuttlebusapplication.databinding.FragmentNoticeBinding
import com.example.shuttlebusapplication.viewmodel.NoticeViewModel

class NoticeFragment : Fragment(R.layout.fragment_notice) {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoticeViewModel by activityViewModels()

    private val isAdmin = true
    private var currentPage = 1
    private val pageSize = 10

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentNoticeBinding.bind(view)

        // 메뉴 버튼: 메인 메뉴로 이동
        binding.btnMenu.setOnClickListener {
            findNavController().navigate(R.id.action_noticeFragment_to_mainMenuFragment)
        }

        // 글쓰기 버튼: 관리자만 보임 + 이동 처리
        binding.btnAddNotice.apply {
            visibility = if (isAdmin) View.VISIBLE else View.GONE
            setOnClickListener {
                findNavController().navigate(R.id.action_noticeFragment_to_addNoticeFragment)
            }
        }

        // RecyclerView 초기화
        binding.recyclerViewNotice.layoutManager = LinearLayoutManager(requireContext())

        // 공지사항 리스트 관찰 (LiveData)
        viewModel.notices.observe(viewLifecycleOwner) {
            refreshList()
        }

        // 이전 페이지 버튼
        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                refreshList()
            }
        }

        // 다음 페이지 버튼
        binding.btnNextPage.setOnClickListener {
            viewModel.notices.value?.let {
                val totalPages = (it.size + pageSize - 1) / pageSize
                if (currentPage < totalPages) {
                    currentPage++
                    refreshList()
                }
            }
        }
    }

    // 페이지별 리스트 업데이트
    private fun refreshList() {
        viewModel.notices.value?.let { noticeList ->
            val start = (currentPage - 1) * pageSize
            val pageData = noticeList.drop(start).take(pageSize)
            val totalPages = (noticeList.size + pageSize - 1) / pageSize

            binding.textPageInfo.text = "$currentPage / $totalPages"

            binding.recyclerViewNotice.adapter = NoticeAdapter(
                isAdmin,
                pageData,
                onItemClick = { notice ->
                    val action = NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(notice)
                    findNavController().navigate(action)
                },
                onEditClick = { notice ->
                    Toast.makeText(requireContext(), "수정 기능은 준비 중", Toast.LENGTH_SHORT).show()
                },
                onDeleteClick = { notice ->
                    viewModel.deleteNotice(notice.id)
                    if ((currentPage - 1) * pageSize >= noticeList.size && currentPage > 1) {
                        currentPage--
                    }
                    refreshList()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
