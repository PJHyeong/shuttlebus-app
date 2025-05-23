package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.adapter.NoticeAdapter
import com.example.shuttlebusapplication.databinding.FragmentNoticeBinding
import com.example.shuttlebusapplication.model.NoticeItem
import com.example.shuttlebusapplication.model.NoticeRequest
import com.example.shuttlebusapplication.viewmodel.NoticeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NoticeFragment : Fragment(R.layout.fragment_notice) {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoticeViewModel by activityViewModels()

    private val isAdmin: Boolean
        get() = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getBoolean("isAdmin", false)
    private var currentPage = 1
    private val pageSize = 10

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentNoticeBinding.bind(view)

        // 메뉴버튼
        binding.btnMenu.setOnClickListener {
            findNavController().navigate(R.id.action_noticeFragment_to_mainMenuFragment)
        }

        // 글쓰기 버튼 (관리자만 보이게)
        binding.btnAddNotice.apply {
            visibility = if (isAdmin) View.VISIBLE else View.GONE
            setOnClickListener {
                // 새 공지 Fragment로 이동
                findNavController().navigate(R.id.action_noticeFragment_to_addNoticeFragment)
            }
        }

        // RecyclerView & Adapter 초기 세팅은 페이징 함수에서 처리
        binding.recyclerViewNotice.layoutManager = LinearLayoutManager(requireContext())

        // LiveData 관찰 → refreshList() 호출
        viewModel.notices.observe(viewLifecycleOwner) {
            refreshList()
        }
        // **초기 데이터 로드**
        viewModel.fetchNotices()

        // 이전/다음 페이지
        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                refreshList()
            }
        }
        binding.btnNextPage.setOnClickListener {
            viewModel.notices.value?.let { list ->
                val totalPages = (list.size + pageSize - 1) / pageSize
                if (currentPage < totalPages) {
                    currentPage++
                    refreshList()
                }
            }
        }
    }

    private fun refreshList() {
        viewModel.notices.value?.let { noticeList ->
            val start = (currentPage - 1) * pageSize
            val pageData = noticeList.drop(start).take(pageSize)
            val totalPages = (noticeList.size + pageSize - 1) / pageSize

            binding.textPageInfo.text = "$currentPage / $totalPages"

            binding.recyclerViewNotice.adapter = NoticeAdapter(
                isAdmin = isAdmin,
                notices = pageData,
                onItemClick = { notice ->
                    val action = NoticeFragmentDirections
                        .actionNoticeFragmentToNoticeDetailFragment(notice)
                    findNavController().navigate(action)
                },
                onEditClick = { notice ->
                    showEditDialog(notice)
                },
                onDeleteClick = { notice ->
                    viewModel.deleteNotice(notice.id)
                    Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    // 페이지 보정
                    if ((currentPage - 1) * pageSize >= noticeList.size && currentPage > 1) {
                        currentPage--
                    }
                    refreshList()
                }
            )
        }
    }

    // ——————————————
    // 생성 다이얼로그
    // ——————————————
    private fun showCreateDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_notice, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etEditTitle)
        val etContent = dialogView.findViewById<EditText>(R.id.etEditContent)

        AlertDialog.Builder(requireContext())
            .setTitle("새 공지사항 등록")
            .setView(dialogView)
            .setPositiveButton("등록") { _, _ ->
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()
                viewModel.createNotice(NoticeRequest(title, content))
                currentPage = 1
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // ——————————————
    // 수정 다이얼로그
    // ——————————————
    private fun showEditDialog(notice: NoticeItem) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_notice, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etEditTitle)
        val etContent = dialogView.findViewById<EditText>(R.id.etEditContent)
        etTitle.setText(notice.title)
        etContent.setText(notice.content)

        AlertDialog.Builder(requireContext())
            .setTitle("공지사항 수정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val newTitle = etTitle.text.toString().trim()
                val newContent = etContent.text.toString().trim()
                viewModel.updateNotice(
                    id = notice.id,          // String 타입 그대로 전달
                    req = NoticeRequest(newTitle, newContent)
                )
                Toast.makeText(requireContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
