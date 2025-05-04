package com.example.shuttlebusapplication.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.ShuttleSchedule

class ShuttleAdapter(
    private val context: Context,
    private var data: List<ShuttleSchedule>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_SPECIAL = 1
        private const val PREFS_NAME = "AlarmPrefs"
        private const val MASTER_SWITCH_KEY = "alarm_master_switch"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

    // 🔒 SharedPreferences 저장/불러오기
    private fun saveAlarmState(position: Int, isChecked: Boolean) {
        sharedPrefs.edit().putBoolean("alarm_item_$position", isChecked).apply()
    }

    private fun getAlarmState(position: Int): Boolean {
        val masterOn = sharedPrefs.getBoolean(MASTER_SWITCH_KEY, true)
        return if (!masterOn) false else sharedPrefs.getBoolean("alarm_item_$position", false)
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

            val savedState = getAlarmState(position)
            switchAlarm.isChecked = savedState
            item.isAlarmSet = savedState
            switchAlarm.isEnabled = sharedPrefs.getBoolean(MASTER_SWITCH_KEY, true)

            switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                item.isAlarmSet = isChecked
                saveAlarmState(position, isChecked)
                Log.d("ShuttleAdapter", "🔔 알림 스위치 변경됨! position = $position -> $isChecked")
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

            val savedState = getAlarmState(position)
            switchAlarm.isChecked = savedState
            item.isAlarmSet = savedState
            switchAlarm.isEnabled = sharedPrefs.getBoolean(MASTER_SWITCH_KEY, true)

            switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                item.isAlarmSet = isChecked
                saveAlarmState(position, isChecked)
                Log.d("ShuttleAdapter", "🔔 알림 스위치 변경됨! position = $position -> $isChecked")
            }
        }
    }
}
