package com.example.shuttlebusapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.CommentResponse

/**
 * CommentResponse 리스트를 받아서
 * RecyclerView에 “닉네임 + 댓글 내용” 형태로 보여주는 Adapter
 */
class CommentAdapter(
    private val items: List<CommentResponse>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNickname: TextView = view.findViewById(R.id.textNickname)
        val textComment: TextView = view.findViewById(R.id.textComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = items[position]
        holder.textNickname.text = comment.userId      // CommentResponse.userId 필드에 닉네임이 들어있다고 가정
        holder.textComment.text = comment.content
    }

    override fun getItemCount(): Int = items.size
}
