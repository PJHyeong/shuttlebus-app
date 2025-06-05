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
    private val isAdmin: Boolean,
    private val onDeleteClick: (CommentResponse) -> Unit,
    private val onEditClick: (CommentResponse) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNickname: TextView = view.findViewById(R.id.textNickname)
        val textComment: TextView = view.findViewById(R.id.textComment)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteComment)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditComment)
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

        if (isAdmin) {
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
}

