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

/**
 * 공지사항 상세 화면 Fragment
 * SafeArgs로 전달받은 noticeItem을 화면에 표시하고,
 * 댓글 목록을 RecyclerView로 보여준 뒤, 댓글 작성 시 서버에 전송
 */
class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail) {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!


    private lateinit var notice: NoticeItem

    // 댓글을 담을 리스트와 어댑터 변수
    private val commentList = mutableListOf<CommentResponse>()
    private lateinit var commentAdapter: CommentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNoticeDetailBinding.bind(view)

        // 1) SafeArgs로 전달된 NoticeItem 객체 가져오기
        notice = NoticeDetailFragmentArgs.fromBundle(requireArguments()).noticeItem

        // 2) 뒤로가기 버튼 클릭 리스너
        binding.btnBackDetail.setOnClickListener {
            findNavController().popBackStack()
        }

        // 3) 화면에 공지사항 제목/날짜/내용 세팅
        binding.textDetailTitle.text = notice.title
        binding.textDetailDate.text = notice.date
        binding.textDetailContent.text = notice.content

        // 4) SharedPreferences에서 닉네임 불러오기
        val loginPrefs = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val nickname = loginPrefs.getString("nickname", "알 수 없는 사용자") ?: "알 수 없는 사용자"

        // 5) RecyclerView에 댓글 목록 세팅
        commentAdapter = CommentAdapter(commentList)
        binding.recyclerComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 6) 서버에서 기존 댓글을 불러와서 보여주기
        fetchComments()

        // 7) 댓글 작성(등록) 버튼 클릭 리스너
        // XML에서 Button의 android:id="@+id/btnSendComment" 라고 되어 있으므로, binding.btnSendComment 로 참조
        binding.btnSendComment.setOnClickListener {
            val content = binding.editComment.text.toString().trim()
            if (content.isNotEmpty()) {
                // CommentRequest에 공지 ID, userId(닉네임), content 담아서 API 호출
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
                                    // 등록 성공 시 EditText 초기화
                                    binding.editComment.text.clear()
                                    // 리스트에 댓글 추가 및 어댑터 갱신
                                    commentList.add(newComment)
                                    commentAdapter.notifyItemInserted(commentList.size - 1)
                                    // 스크롤을 마지막 댓글로 이동
                                    binding.recyclerComments.scrollToPosition(commentList.size - 1)
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "댓글 작성에 실패했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                            Toast.makeText(
                                requireContext(),
                                "댓글 작성 중 오류가 발생했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                Toast.makeText(requireContext(), "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 서버에서 해당 공지글의 댓글 목록을 불러와 RecyclerView에 표시하는 메서드
     */
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
                        Toast.makeText(
                            requireContext(),
                            "댓글을 불러오는데 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<CommentResponse>>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "댓글 불러오기 중 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
