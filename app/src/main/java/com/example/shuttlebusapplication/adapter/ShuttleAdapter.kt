package com.example.shuttlebusapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.ShuttleSchedule

class ShuttleAdapter(private var data: List<ShuttleSchedule>) :
    RecyclerView.Adapter<ShuttleAdapter.ShuttleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timetable, parent, false)
        return ShuttleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShuttleViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<ShuttleSchedule>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class ShuttleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOrder: TextView = itemView.findViewById(R.id.textOrder)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textViaTime: TextView = itemView.findViewById(R.id.textViaTime)
        private val textExpectedArrival: TextView = itemView.findViewById(R.id.textExpectedArrival)
        private val btnAlarm: ImageButton = itemView.findViewById(R.id.btnAlarm)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

        fun bind(item: ShuttleSchedule, position: Int) {
            // 데이터 바인딩
            textOrder.text = (position + 1).toString()
            textTime.text = item.departureTime
            textViaTime.text = item.viaTime ?: ""

            // 도착 예정 시간 표시 여부
            if (!item.expectedArrivalTime.isNullOrEmpty()) {
                textExpectedArrival.visibility = View.VISIBLE
                textExpectedArrival.text = item.expectedArrivalTime
            } else {
                textExpectedArrival.visibility = View.GONE
            }

            // ⭐️ 초기 상태 세팅 (재활용 이슈 방지)
            btnFavorite.setImageResource(
                if (item.isFavorite) R.drawable.act_star else R.drawable.non_act_star
            )
            btnAlarm.setImageResource(
                if (item.isAlarmSet) R.drawable.act_bell else R.drawable.non_act_bell
            )

            // 클릭 이벤트 (즐겨찾기 토글)
            btnFavorite.setOnClickListener {
                Log.d("ShuttleAdapter", "⭐️ 즐겨찾기 버튼 클릭됨! position = $position")
                item.isFavorite = !item.isFavorite
                notifyItemChanged(position) // ⭐️ RecyclerView 아이템 갱신
            }

            // 클릭 이벤트 (알림 토글)
            btnAlarm.setOnClickListener {
                Log.d("ShuttleAdapter", "🔔 알림 버튼 클릭됨! position = $position")
                item.isAlarmSet = !item.isAlarmSet
                notifyItemChanged(position) // ⭐️ RecyclerView 아이템 갱신
            }
        }
    }
}
