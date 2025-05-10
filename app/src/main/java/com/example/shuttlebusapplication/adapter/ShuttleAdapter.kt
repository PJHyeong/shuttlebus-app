package com.example.shuttlebusapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.ShuttleSchedule

class ShuttleAdapter(private var data: List<ShuttleSchedule>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_SPECIAL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (!data[position].viaTime.isNullOrEmpty()) VIEW_TYPE_SPECIAL else VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_NORMAL -> {
                val view = inflater.inflate(R.layout.item_timetable_normal, parent, false)
                NormalViewHolder(view)
            }
            VIEW_TYPE_SPECIAL -> {
                val view = inflater.inflate(R.layout.item_timetable_special, parent, false)
                SpecialViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is NormalViewHolder -> holder.bind(item, position)
            is SpecialViewHolder -> holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<ShuttleSchedule>) {
        data = newData
        notifyDataSetChanged()
    }

    // ✅ 일반 노선용 ViewHolder
    inner class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOrder: TextView = itemView.findViewById(R.id.textOrder)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textExpectedArrival: TextView = itemView.findViewById(R.id.textExpectedArrival)
        private val switchAlarm: Switch = itemView.findViewById(R.id.switchAlarm)

        fun bind(item: ShuttleSchedule, position: Int) {
            textOrder.text = (position + 1).toString()
            textTime.text = item.departureTime
            textExpectedArrival.text = item.expectedArrivalTime ?: "-"

            // 스위치 초기 상태 설정
            switchAlarm.isChecked = item.isAlarmSet

            switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                Log.d("ShuttleAdapter", "🔔 알림 스위치 변경됨! position = $position -> $isChecked")
                item.isAlarmSet = isChecked
            }
        }
    }

    // ✅ 특수 노선용 ViewHolder
    inner class SpecialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOrder: TextView = itemView.findViewById(R.id.textOrder)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textViaTime: TextView = itemView.findViewById(R.id.textViaTime)
        private val switchAlarm: Switch = itemView.findViewById(R.id.switchAlarm)

        fun bind(item: ShuttleSchedule, position: Int) {
            textOrder.text = (position + 1).toString()
            textTime.text = item.departureTime
            textViaTime.text = item.viaTime ?: "-"

            switchAlarm.isChecked = item.isAlarmSet

            switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                Log.d("ShuttleAdapter", "🔔 알림 스위치 변경됨! position = $position -> $isChecked")
                item.isAlarmSet = isChecked
            }
        }
    }
}
