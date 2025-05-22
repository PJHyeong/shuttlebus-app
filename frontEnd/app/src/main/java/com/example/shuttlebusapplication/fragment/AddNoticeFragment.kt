package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.databinding.FragmentAddNoticeBinding
import com.example.shuttlebusapplication.model.NoticeRequest
import com.example.shuttlebusapplication.viewmodel.NoticeViewModel

class AddNoticeFragment : Fragment(R.layout.fragment_add_notice) {

    private var _binding: FragmentAddNoticeBinding? = null
    private val binding get() = _binding!!

    // ViewModel 연결
    private val viewModel: NoticeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAddNoticeBinding.bind(view)

        // 등록 버튼 클릭
        binding.btnSave.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val content = binding.editContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // NoticeItem 대신 NoticeRequest DTO 사용
            val req = NoticeRequest(
                title   = title,
                content = content
            )

            viewModel.createNotice(req)    // API 호출

            Toast.makeText(requireContext(), "등록되었습니다.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addNoticeFragment_to_noticeFragment)
        }

        // 취소 버튼
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
