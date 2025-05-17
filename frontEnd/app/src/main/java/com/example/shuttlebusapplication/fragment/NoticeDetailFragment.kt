package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.databinding.FragmentNoticeDetailBinding
import com.example.shuttlebusapplication.model.NoticeItem
import com.example.shuttlebusapplication.adapter.CommentAdapter // <- import 확인!

class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail) {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var notice: NoticeItem
    private val commentList = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentNoticeDetailBinding.bind(view)

        // Safe Args로 데이터 받기
        val args = NoticeDetailFragmentArgs.fromBundle(requireArguments())
        notice = args.noticeItem

        binding.btnBackDetail.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.textDetailTitle.text = notice.title
        binding.textDetailDate.text = notice.date
        binding.textDetailContent.text = "여기에 상세 내용을 불러와서 표시합니다."

        val commentAdapter = CommentAdapter(commentList)
        binding.recyclerComments.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerComments.adapter = commentAdapter

        binding.btnSendComment.setOnClickListener {
            val text = binding.editComment.text.toString().trim()
            if (text.isNotEmpty()) {
                commentList.add(text)
                commentAdapter.notifyItemInserted(commentList.size - 1)
                binding.editComment.text!!.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
