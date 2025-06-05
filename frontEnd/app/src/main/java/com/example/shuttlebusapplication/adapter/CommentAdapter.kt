package com.example.shuttlebusapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.CommentResponse

class CommentAdapter(
    private val items: List<CommentResponse>,
    private val isAdmin: Boolean, // ← 관리자 여부 전달
    private val onDeleteClick: (CommentResponse) -> Unit // ← 삭제 콜백 전달
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNickname: TextView = view.findViewById(R.id.textNickname)
        val textComment: TextView = view.findViewById(R.id.textComment)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteComment) // ← 삭제 버튼 추가
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = items[position]
        holder.textNickname.text = comment.userId
        holder.textComment.text = comment.content

        // 관리자만 삭제 버튼 보이게 처리
        if (isAdmin) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDeleteClick(comment)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size
}
