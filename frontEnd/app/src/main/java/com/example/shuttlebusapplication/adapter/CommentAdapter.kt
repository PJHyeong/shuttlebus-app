package com.example.shuttlebusapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.CommentResponse
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private val items: List<CommentResponse>,
    private val isAdmin: Boolean,
    private val myNickname: String,
    private val onDeleteClick: (CommentResponse) -> Unit,
    private val onEditClick: (CommentResponse) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNickname: TextView = view.findViewById(R.id.textNickname)
        val textComment: TextView = view.findViewById(R.id.textComment)
        val textCommentDate: TextView = view.findViewById(R.id.textCommentDate)
        val textCommentTime: TextView = view.findViewById(R.id.textCommentTime)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteComment)
        val btnEdit: Button = view.findViewById(R.id.btnEditComment)
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

        // 날짜/시간 파싱
        val date = parseDate(comment.createdAt)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.textCommentDate.text = dateFormat.format(date)
        holder.textCommentTime.text = timeFormat.format(date)

        val canEditOrDelete = isAdmin || comment.userId == myNickname

        if (canEditOrDelete) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDeleteClick(comment) }
            holder.btnEdit.setOnClickListener { onEditClick(comment) }
        } else {
            holder.btnDelete.visibility = View.GONE
            holder.btnEdit.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

    private fun parseDate(dateString: String): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}
