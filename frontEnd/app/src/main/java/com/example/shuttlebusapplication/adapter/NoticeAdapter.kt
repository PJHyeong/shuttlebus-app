package com.example.shuttlebusapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.NoticeItem

class NoticeAdapter(
    private val isAdmin: Boolean,
    private val notices: List<NoticeItem>,
    private val onItemClick: (NoticeItem) -> Unit,
    private val onEditClick: (NoticeItem) -> Unit,
    private val onDeleteClick: (NoticeItem) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title   = itemView.findViewById<TextView>(R.id.textTitle)
        private val date    = itemView.findViewById<TextView>(R.id.textDate)
        private val adminButtons = itemView.findViewById<LinearLayout>(R.id.adminButtons)
        private val btnEdit = itemView.findViewById<Button>(R.id.btnEdit)
        private val btnDelete = itemView.findViewById<Button>(R.id.btnDelete)

        fun bind(item: NoticeItem) {
            title.text = item.title
            date.text  = item.date

            adminButtons.visibility = if (isAdmin) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onItemClick(item) }
            btnEdit.setOnClickListener   { onEditClick(item) }
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(notices[position])
    }

    override fun getItemCount(): Int = notices.size
}
