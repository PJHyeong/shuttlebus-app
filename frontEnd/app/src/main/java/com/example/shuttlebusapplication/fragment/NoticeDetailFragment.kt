package com.example.shuttlebusapplication.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.adapter.CommentAdapter
import com.example.shuttlebusapplication.databinding.FragmentNoticeDetailBinding
import com.example.shuttlebusapplication.model.CommentRequest
import com.example.shuttlebusapplication.model.CommentResponse
import com.example.shuttlebusapplication.model.NoticeItem
import com.example.shuttlebusapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail) {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var notice: NoticeItem
    private val commentList = mutableListOf<CommentResponse>()
    private lateinit var commentAdapter: CommentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNoticeDetailBinding.bind(view)

        // SafeArgs로 공지사항 객체 받기
        notice = NoticeDetailFragmentArgs.fromBundle(requireArguments()).noticeItem

        // 뒤로가기 버튼
        binding.btnBackDetail.setOnClickListener {
            findNavController().popBackStack()
        }

        // 공지 내용 표시
        binding.textDetailTitle.text = notice.title
        binding.textDetailDate.text = notice.date
        binding.textDetailContent.text = notice.content

        // SharedPreferences에서 닉네임과 관리자 여부 불러오기
        val loginPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val nickname = loginPrefs.getString("nickname", "알 수 없는 사용자") ?: "알 수 없는 사용자"
        val isAdmin = loginPrefs.getBoolean("isAdmin", false)

        // 댓글 어댑터 설정
        commentAdapter = CommentAdapter(
            items = commentList,
            isAdmin = true, // 관리자 여부와 상관없이 모두 삭제 버튼 보이게 변경
            onDeleteClick = { comment ->
                RetrofitClient.apiService.deleteComment(comment.id).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            val index = commentList.indexOf(comment)
                            if (index != -1) {
                                commentList.removeAt(index)
                                commentAdapter.notifyItemRemoved(index)
                                Toast.makeText(requireContext(), "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(requireContext(), "댓글 삭제 중 오류 발생", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        )


        binding.recyclerComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 댓글 불러오기
        fetchComments()

        // 댓글 등록 버튼
        binding.btnSendComment.setOnClickListener {
            val content = binding.editComment.text.toString().trim()
            if (content.isNotEmpty()) {
                val commentReq = CommentRequest(
                    announcementId = notice.id,
                    userId = nickname,
                    content = content
                )
                RetrofitClient.apiService.addComment(commentReq)
                    .enqueue(object : Callback<CommentResponse> {
                        override fun onResponse(
                            call: Call<CommentResponse>,
                            response: Response<CommentResponse>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.let { newComment ->
                                    binding.editComment.text.clear()
                                    commentList.add(newComment)
                                    commentAdapter.notifyItemInserted(commentList.size - 1)
                                    binding.recyclerComments.scrollToPosition(commentList.size - 1)
                                }
                            } else {
                                Toast.makeText(requireContext(), "댓글 작성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                            Toast.makeText(requireContext(), "댓글 작성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                Toast.makeText(requireContext(), "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchComments() {
        RetrofitClient.apiService.getComments(notice.id)
            .enqueue(object : Callback<List<CommentResponse>> {
                override fun onResponse(
                    call: Call<List<CommentResponse>>,
                    response: Response<List<CommentResponse>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { list ->
                            commentList.clear()
                            commentList.addAll(list)
                            commentAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(requireContext(), "댓글을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<CommentResponse>>, t: Throwable) {
                    Toast.makeText(requireContext(), "댓글 불러오기 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
