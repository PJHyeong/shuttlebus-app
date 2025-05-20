package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.databinding.FragmentAddNoticeBinding
import com.example.shuttlebusapplication.model.NoticeItem
import com.example.shuttlebusapplication.viewmodel.NoticeViewModel

class AddNoticeFragment : Fragment(R.layout.fragment_add_notice) {

    private var _binding: FragmentAddNoticeBinding? = null
    private val binding get() = _binding!!

    // ViewModel 연결
    private val viewModel: NoticeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAddNoticeBinding.bind(view)

        // 등록 버튼: 제목/내용 체크 + ViewModel에 추가
        binding.btnSave.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val content = binding.editContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newNotice = NoticeItem(
                id = System.currentTimeMillis().toInt(), // 임시 ID
                title = title,
                date = "2025-05-20", // 실제는 날짜 포맷으로 처리 가능
                content = content
            )

            viewModel.addNotice(newNotice)

            Toast.makeText(requireContext(), "등록되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addNoticeFragment_to_noticeFragment)
        }

        // 취소 버튼
        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
