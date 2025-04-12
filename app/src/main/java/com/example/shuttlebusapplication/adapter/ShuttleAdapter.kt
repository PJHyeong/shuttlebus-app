// ShuttleAdapter.kt

package com.example.shuttlebusapplication.adapter

import android.content.Context
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

    inner class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOrder: TextView = itemView.findViewById(R.id.textOrder)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textExpectedArrival: TextView = itemView.findViewById(R.id.textExpectedArrival)
        private val btnAlarm: ImageButton = itemView.findViewById(R.id.btnAlarm)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

        fun bind(item: ShuttleSchedule, position: Int) {
            textOrder.text = (position + 1).toString()
            textTime.text = item.departureTime
            textExpectedArrival.text = item.expectedArrivalTime ?: "-"

            val prefs = itemView.context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
            val masterSwitch = prefs.getBoolean("alarm_master_switch", true)
            val alarmKey = "alarm_enabled_$position"
            val alarmState = prefs.getBoolean(alarmKey, false)
            item.isAlarmSet = alarmState

            btnAlarm.setImageResource(
                if (masterSwitch && item.isAlarmSet) R.drawable.act_bell else R.drawable.non_act_bell
            )

            btnAlarm.setOnClickListener {
                item.isAlarmSet = !item.isAlarmSet
                prefs.edit().putBoolean(alarmKey, item.isAlarmSet).apply()
                btnAlarm.setImageResource(
                    if (prefs.getBoolean("alarm_master_switch", true) && item.isAlarmSet)
                        R.drawable.act_bell else R.drawable.non_act_bell
                )
                Log.d("ShuttleAdapter", "🔔 알림 버튼 클릭됨! 상태 = ${item.isAlarmSet}, position = $position")
            }

            btnFavorite.setOnClickListener {
                item.isFavorite = !item.isFavorite
                btnFavorite.setImageResource(
                    if (item.isFavorite) R.drawable.act_star else R.drawable.non_act_star
                )
            }
        }
    }

    inner class SpecialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOrder: TextView = itemView.findViewById(R.id.textOrder)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textViaTime: TextView = itemView.findViewById(R.id.textViaTime)
        private val btnAlarm: ImageButton = itemView.findViewById(R.id.btnAlarm)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

        fun bind(item: ShuttleSchedule, position: Int) {
            textOrder.text = (position + 1).toString()
            textTime.text = item.departureTime
            textViaTime.text = item.viaTime ?: "-"

            val prefs = itemView.context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
            val masterSwitch = prefs.getBoolean("alarm_master_switch", true)
            val alarmKey = "alarm_enabled_$position"
            val alarmState = prefs.getBoolean(alarmKey, false)
            item.isAlarmSet = alarmState

            btnAlarm.setImageResource(
                if (masterSwitch && item.isAlarmSet) R.drawable.act_bell else R.drawable.non_act_bell
            )

            btnAlarm.setOnClickListener {
                item.isAlarmSet = !item.isAlarmSet
                prefs.edit().putBoolean(alarmKey, item.isAlarmSet).apply()
                btnAlarm.setImageResource(
                    if (prefs.getBoolean("alarm_master_switch", true) && item.isAlarmSet)
                        R.drawable.act_bell else R.drawable.non_act_bell
                )
            }

            btnFavorite.setImageResource(
                if (item.isFavorite) R.drawable.act_star else R.drawable.non_act_star
            )

            btnFavorite.setOnClickListener {
                item.isFavorite = !item.isFavorite
                btnFavorite.setImageResource(
                    if (item.isFavorite) R.drawable.act_star else R.drawable.non_act_star
                )
            }
        }
    }
}
