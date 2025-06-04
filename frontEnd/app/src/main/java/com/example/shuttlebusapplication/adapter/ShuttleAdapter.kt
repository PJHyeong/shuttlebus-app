// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/adapter/ShuttleAdapter.kt

package com.example.shuttlebusapplication.adapter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.ShuttleSchedule
import com.example.shuttlebusapplication.AlarmReceiver
import java.util.Calendar

/**
 * ShuttleAdapter:
 * - data: 각 노선의 ShuttleSchedule 리스트
 * - context: Context
 *
 * 이 어댑터 안에서 “SettingsFragment의 마스터 스위치” 상태를 SharedPreferences에서 읽어서
 * 사용자가 스위치를 켜려고 하면 마스터가 OFF인지 체크하고, OFF면 강제 OFF 처리(토스트 후),
 * ON이면 출발 3분 전에 정확 알람을 예약합니다.
 */
class ShuttleAdapter(
    private var data: List<ShuttleSchedule>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_SPECIAL = 1

        // SettingsFragment의 마스터 스위치용 SharedPreferences 파일명/키
        private const val MASTER_PREFS = "AlarmPrefs"
        private const val MASTER_KEY   = "alarm_master_switch"
    }

    // “개별 셔틀 알람 예약 상태”를 저장하는 SharedPreferences
    private val prefs = context.getSharedPreferences(MASTER_PREFS, Context.MODE_PRIVATE)

    override fun getItemViewType(position: Int): Int =
        if (!data[position].viaTime.isNullOrEmpty()) VIEW_TYPE_SPECIAL else VIEW_TYPE_NORMAL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_NORMAL) {
            NormalViewHolder(inflater.inflate(R.layout.item_timetable_normal, parent, false))
        } else {
            SpecialViewHolder(inflater.inflate(R.layout.item_timetable_special, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        if (holder is NormalViewHolder) holder.bind(item, position)
        else if (holder is SpecialViewHolder) holder.bind(item, position)
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<ShuttleSchedule>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class NormalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val textOrder: TextView = v.findViewById(R.id.textOrder)
        private val textTime: TextView = v.findViewById(R.id.textTime)
        private val textExpectedArrival: TextView = v.findViewById(R.id.textExpectedArrival)
        private val switchAlarm: Switch = v.findViewById(R.id.switchAlarm)

        fun bind(item: ShuttleSchedule, pos: Int) {
            textOrder.text = (pos + 1).toString()
            textTime.text = item.departureTime
            textExpectedArrival.text = item.expectedArrivalTime ?: "-"

            // “departureTime + shuttleName”을 키로 해서 개별 알람 예약 여부를 저장
            val key = item.departureTime + item.shuttleName
            val saved = prefs.getBoolean(key, false)

            // 🚨 만료된 예약(출발 3분 전이 이미 지났으면) 자동 해제
            val now = Calendar.getInstance()
            val parts = item.departureTime.trim().split(":")
            val h = parts.getOrNull(0)?.toIntOrNull()
            val m = parts.getOrNull(1)?.toIntOrNull()
            val expireCal = Calendar.getInstance().apply {
                if (h != null && m != null) {
                    set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), h, m)
                    add(Calendar.MINUTE, -3)
                }
            }
            val isExpired = expireCal.before(now)
            val finalState = if (saved && isExpired) {
                prefs.edit().putBoolean(key, false).apply()
                false
            } else {
                saved
            }

            switchAlarm.setOnCheckedChangeListener(null)
            switchAlarm.isChecked = finalState
            item.isAlarmSet = finalState

            switchAlarm.setOnCheckedChangeListener { _, checked ->
                // ─── ① 마스터 스위치 확인 ───
                val masterOn = prefs.getBoolean(MASTER_KEY, true)
                if (!masterOn && checked) {
                    // 마스터가 OFF인데 사용자 스위치를 켜려 하면
                    Toast.makeText(context, "알림이 OFF 상태입니다.", Toast.LENGTH_SHORT).show()
                    switchAlarm.isChecked = false
                    prefs.edit().putBoolean(key, false).apply()
                    item.isAlarmSet = false
                    return@setOnCheckedChangeListener
                }

                // ─── ② 마스터 ON이거나, 스위치를 끄려는(checked == false) 상황
                item.isAlarmSet = checked
                prefs.edit().putBoolean(key, checked).apply()
                if (checked) {
                    scheduleAlarm(context, item)
                } else {
                    cancelAlarm(context, item)
                }
                Log.d("ShuttleAdapter", "🔔 알람 스위치: ${item.shuttleName} → $checked")
            }
        }
    }

    inner class SpecialViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val textOrder: TextView = v.findViewById(R.id.textOrder)
        private val textTime: TextView = v.findViewById(R.id.textTime)
        private val textViaTime: TextView = v.findViewById(R.id.textViaTime)
        private val switchAlarm: Switch = v.findViewById(R.id.switchAlarm)

        fun bind(item: ShuttleSchedule, pos: Int) {
            textOrder.text = (pos + 1).toString()
            textTime.text = item.departureTime
            textViaTime.text = item.viaTime ?: "-"

            val key = item.departureTime + item.shuttleName
            val saved = prefs.getBoolean(key, false)

            // 🚨 만료된 예약 자동 해제
            val now = Calendar.getInstance()
            val parts = item.departureTime.trim().split(":")
            val h = parts.getOrNull(0)?.toIntOrNull()
            val m = parts.getOrNull(1)?.toIntOrNull()
            val expireCal = Calendar.getInstance().apply {
                if (h != null && m != null) {
                    set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), h, m)
                    add(Calendar.MINUTE, -3)
                }
            }
            val isExpired = expireCal.before(now)
            val finalState = if (saved && isExpired) {
                prefs.edit().putBoolean(key, false).apply()
                false
            } else {
                saved
            }

            switchAlarm.setOnCheckedChangeListener(null)
            switchAlarm.isChecked = finalState
            item.isAlarmSet = finalState

            switchAlarm.setOnCheckedChangeListener { _, checked ->
                // ─── ① 마스터 스위치 확인 ───
                val masterOn = prefs.getBoolean(MASTER_KEY, true)
                if (!masterOn && checked) {
                    Toast.makeText(context, "알림이 OFF 상태입니다.", Toast.LENGTH_SHORT).show()
                    switchAlarm.isChecked = false
                    prefs.edit().putBoolean(key, false).apply()
                    item.isAlarmSet = false
                    return@setOnCheckedChangeListener
                }

                // ─── ② 정상 토글 동작 ───
                item.isAlarmSet = checked
                prefs.edit().putBoolean(key, checked).apply()
                if (checked) {
                    scheduleAlarm(context, item)
                } else {
                    cancelAlarm(context, item)
                }
                Log.d("ShuttleAdapter", "🔔 알람 스위치: ${item.shuttleName} → $checked")
            }
        }
    }

    /**
     * “출발 3분 전”에 알람을 예약합니다.
     */
    private fun scheduleAlarm(context: Context, item: ShuttleSchedule) {
        // Android 12(API 31)+: 정확 알람 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                Toast.makeText(
                    context,
                    "정확 알람 예약 권한이 필요합니다.\n설정에서 ‘정확 알람 예약’을 허용해주세요.",
                    Toast.LENGTH_LONG
                ).show()
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                return
            }
        }

        val raw = item.departureTime.trim()
        val parts = raw.split(":")
        if (parts.size != 2) {
            Toast.makeText(context, "시간 파싱 실패: '$raw'", Toast.LENGTH_SHORT).show()
            return
        }
        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()
        if (hour == null || minute == null) {
            Toast.makeText(context, "시간 파싱 실패: '$raw'", Toast.LENGTH_SHORT).show()
            return
        }

        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), hour, minute)
            add(Calendar.MINUTE, -3) // “출발 3분 전” 시점으로 이동
        }
        if (cal.before(now)) {
            Toast.makeText(context, "이미 지난 시간입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmType", "timetable")
            putExtra("departureTime", raw)
        }
        val requestCode = (item.shuttleName + raw).hashCode()
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)

        Toast.makeText(context, "알림이 예약되었습니다: 출발 3분 전", Toast.LENGTH_SHORT).show()
    }

    /**
     * 이미 예약된 알람을 취소합니다.
     */
    private fun cancelAlarm(context: Context, item: ShuttleSchedule) {
        val raw = item.departureTime.trim()
        val requestCode = (item.shuttleName + raw).hashCode()
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pi)

        Toast.makeText(context, "알림이 취소되었습니다.", Toast.LENGTH_SHORT).show()
    }
}
