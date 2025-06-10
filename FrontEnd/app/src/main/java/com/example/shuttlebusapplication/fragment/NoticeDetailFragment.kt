package com.example.shuttlebusapplication.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.adapter.CommentAdapter
import com.example.shuttlebusapplication.databinding.FragmentNoticeDetailBinding
import com.example.shuttlebusapplication.model.*
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

        notice = NoticeDetailFragmentArgs.fromBundle(requireArguments()).noticeItem

        binding.btnBackDetail.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.textDetailTitle.text = notice.title
        binding.textDetailDate.text = notice.date
        binding.textDetailContent.text = notice.content

        val loginPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val nickname = loginPrefs.getString("nickname", "알 수 없는 사용자") ?: "알 수 없는 사용자"
        val isAdmin = loginPrefs.getBoolean("isAdmin", false)

        commentAdapter = CommentAdapter(
            items = commentList,
            isAdmin = isAdmin,
            myNickname = nickname,
            onDeleteClick = { comment ->
                val deleteRequest = DeleteCommentRequest(
                    userId = nickname,
                    userRole = if (isAdmin) "admin" else "user"
                )
                RetrofitClient.apiService.deleteCommentWithBody(comment.id, deleteRequest)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                val index = commentList.indexOf(comment)
                                if (index != -1) {
                                    commentList.removeAt(index)
                                    commentAdapter.notifyItemRemoved(index)
                                    Toast.makeText(requireContext(), "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "삭제 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(requireContext(), "댓글 삭제 중 오류 발생", Toast.LENGTH_SHORT).show()
                        }
                    })
            },
            onEditClick = { comment ->
                showEditCommentDialog(comment, nickname, isAdmin)
            }
        )

        binding.recyclerComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        fetchComments()

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

    private fun showEditCommentDialog(comment: CommentResponse, nickname: String, isAdmin: Boolean) {
        val editText = EditText(requireContext()).apply {
            setText(comment.content)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("댓글 수정")
            .setView(editText)
            .setPositiveButton("수정") { _, _ ->
                val updatedContent = editText.text.toString().trim()
                if (updatedContent.isNotEmpty()) {
                    val request = UpdateCommentRequest(
                        userId = nickname,
                        userRole = if (isAdmin) "admin" else "user",
                        content = updatedContent
                    )
                    RetrofitClient.apiService.updateComment(comment.id, request)
                        .enqueue(object : Callback<CommentResponse> {
                            override fun onResponse(
                                call: Call<CommentResponse>,
                                response: Response<CommentResponse>
                            ) {
                                if (response.isSuccessful) {
                                    val index = commentList.indexOfFirst { it.id == comment.id }
                                    if (index != -1) {
                                        commentList[index] = response.body()!!
                                        commentAdapter.notifyItemChanged(index)
                                        Toast.makeText(requireContext(), "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "수정 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                                Toast.makeText(requireContext(), "댓글 수정 중 오류 발생", Toast.LENGTH_SHORT).show()
                            }
                        })
                } else {
                    Toast.makeText(requireContext(), "수정할 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
