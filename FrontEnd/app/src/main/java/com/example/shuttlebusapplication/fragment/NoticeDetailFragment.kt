package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.View        // ← 추가
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.databinding.FragmentNoticeDetailBinding
import com.example.shuttlebusapplication.model.NoticeItem

class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail) {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var notice: NoticeItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentNoticeDetailBinding.bind(view)

        // SafeArgs 로 전달된 notice
        notice = NoticeDetailFragmentArgs.fromBundle(requireArguments()).noticeItem

        binding.btnBackDetail.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.textDetailTitle.text = notice.title
        binding.textDetailDate.text = notice.date
        binding.textDetailContent.text = notice.content
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}