// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/fragment/StationInfoBottomSheetFragment.kt

package com.example.shuttlebusapplication.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.RouteDetailActivity
import com.example.shuttlebusapplication.model.Arrival
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 정류장 클릭 시 표시되는 Bottom Sheet Dialog Fragment
 * - 상단: 정류장 이름
 * - 중단: 도착 예정 버스 목록 (item_arrival_bus.xml)
 *   • “셔틀명”을 클릭하면 RouteDetailActivity로 이동
 * - 하단: 새로고침 버튼(btnRefresh)
 */
class StationInfoBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_STATION_NAME = "station_name"
        private const val ARG_STATION_IDX  = "station_idx"

        // BottomSheetFragment 쪽 SharedPreferences 이름
        private const val PREFS_NAME = "alarm_prefs"
        // SettingsFragment 마스터 스위치용 SharedPreferences 이름/키
        private const val MASTER_PREFS = "AlarmPrefs"
        private const val MASTER_KEY   = "alarm_master_switch"

        /**
         * newInstance로 생성 시 호출
         * @param stationName (String) 정류장 이름
         * @param stationIdx  (Int) 정류장 인덱스
         */
        fun newInstance(
            stationName: String,
            stationIdx: Int
        ): StationInfoBottomSheetFragment {
            return StationInfoBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATION_NAME, stationName)
                    putInt(ARG_STATION_IDX, stationIdx)
                }
            }
        }
    }

    // arguments에서 받아올 정류장 이름/인덱스
    private var stationName: String? = null
    private var stationIdx: Int      = -1

    // 동적으로 “도착 예정 버스 목록”을 추가할 컨테이너
    private lateinit var containerArrivalList: LinearLayout

    // 새로고침 버튼
    private lateinit var btnRefresh: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // arguments에서 stationName, stationIdx 읽어오기
        arguments?.let {
            stationName = it.getString(ARG_STATION_NAME)
            stationIdx  = it.getInt(ARG_STATION_IDX)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // fragment_station_info_bottom_sheet.xml을 inflate
        val view = inflater.inflate(
            R.layout.fragment_station_info_bottom_sheet,
            container,
            false
        )

        // (1) 상단 텍스트: 정류장 이름
        val textStationTitle: TextView = view.findViewById(R.id.textStationTitle)
        textStationTitle.text = stationName

        // (2) 중단 컨테이너: 도착 예정 버스 목록
        containerArrivalList = view.findViewById(R.id.containerArrivalList)

        // (3) 하단 새로고침 버튼
        btnRefresh = view.findViewById<ImageButton>(R.id.btnRefresh).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                refreshArrivalList()
            }
        }

        // 최초 로딩 시 한 번 호출
        refreshArrivalList()
        return view
    }

    /**
     * MapFragment.getUpcomingArrivalsForStation(...)을 호출하여
     * List<Arrival>을 가져온 뒤 item_arrival_bus.xml 레이아웃을 inflate해 추가
     */
    private fun refreshArrivalList() {
        // 1) 기존 뷰 모두 제거
        containerArrivalList.removeAllViews()

        // 2) BottomSheetFragment 전용 SharedPreferences 열기
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 3) 부모 Fragment가 MapFragment인지 확인
        val parentFrag = parentFragment
        if (parentFrag !is MapFragment) {
            Toast.makeText(requireContext(), "지도 화면을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 4) MapFragment에서 도착 예정 버스 목록 가져오기
        val arrivals: List<Arrival> = parentFrag.getUpcomingArrivalsForStation(stationIdx)

        // 5) 목록이 비어 있으면 “곧 도착 셔틀 없음” 표시
        if (arrivals.isEmpty()) {
            val tvNone = TextView(requireContext()).apply {
                text = "곧 도착 셔틀 없음"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
            }
            containerArrivalList.addView(tvNone)
            return
        }

        // 6) 목록이 있으면 item_arrival_bus.xml을 하나씩 inflate하여 추가
        arrivals.forEach { arrival ->
            val itemView = layoutInflater.inflate(
                R.layout.item_arrival_bus,  // item_arrival_bus.xml
                containerArrivalList,
                false
            )

            // (a) 셔틀명 + ETA 텍스트
            val textShuttleInfo: TextView = itemView.findViewById(R.id.textShuttleInfo)
            textShuttleInfo.text = formatEtaLine(arrival.shuttleName, arrival.etaSec)

            // “셔틀명” 클릭 시 RouteDetailActivity로 이동
            textShuttleInfo.setOnClickListener {
                val clickedShuttle = arrival.shuttleName

                // ▶▶▶ 셔틀 이름에 따라 stationListForIntent 구성
                val stationListForIntent = when (clickedShuttle) {
                    "명지대역 셔틀" -> arrayListOf(
                        "기점(버스관리사무소)",
                        "이마트 앞",
                        "역북동 행정복지센터 건너편",
                        "명지대역 사거리",
                        "역북동 행정복지센터 앞",
                        "광장 정류장",
                        "명진당 앞",
                        "3공학관 앞"
                    )
                    "시내 셔틀"    -> arrayListOf(
                        "기점(버스관리사무소)",
                        "이마트 상공회의소 앞",
                        "역북동 행정복지센터 건너편",
                        "동부경찰서 중앙지구대 앞",
                        "용인 CGV",
                        "중앙공영주차장 앞",
                        "역북동 행정복지센터 앞",
                        "이마트 상공회의소 건너편",
                        "제1공학관",
                        "제3공학관"
                    )
                    "기흥 셔틀"    -> arrayListOf(
                        "기점(버스관리사무소)",
                        "기흥역 5번 출구",
                        "채플관 앞"
                    )
                    else -> arrayListOf(
                        "기점(버스관리사무소)",
                        "이마트 앞",
                        "명지대역 사거리"
                    )
                }

                val parentMapFrag = parentFragment
                val busIndexToSend = if (parentMapFrag is MapFragment) {
                    parentMapFrag.getCurrentBusIndex()
                } else {
                    0
                }

                // Intent로 RouteDetailActivity 호출
                val intent = Intent(requireContext(), RouteDetailActivity::class.java).apply {
                    putStringArrayListExtra("stationList", stationListForIntent)
                    putExtra("shuttleName", clickedShuttle)
                    putExtra("busIndex", busIndexToSend)
                }
                startActivity(intent)
            }

            // (b) ETA가 60초 미만일 때 빨간색+굵은 글씨
            if (arrival.etaSec in 0..59) {
                textShuttleInfo.setTextColor(resources.getColor(
                    android.R.color.holo_red_dark, null
                ))
                textShuttleInfo.setTypeface(
                    textShuttleInfo.typeface,
                    android.graphics.Typeface.BOLD
                )
            } else {
                textShuttleInfo.setTextColor(resources.getColor(
                    android.R.color.black, null
                ))
                textShuttleInfo.setTypeface(
                    textShuttleInfo.typeface,
                    android.graphics.Typeface.NORMAL
                )
            }

            // (c) 알림 버튼(btnAlarm)
            val btnAlarm: ImageButton = itemView.findViewById(R.id.btnAlarm)

            // SharedPreferences에 저장된 키 조회 ("stationIdx|shuttleName")
            val key = "$stationIdx|${arrival.shuttleName}"
            val isAlarmSet = prefs.getBoolean(key, false)

            // 초기 상태에 따라 아이콘 설정
            btnAlarm.setImageResource(
                if (isAlarmSet) R.drawable.act_bell else R.drawable.non_act_bell
            )
            var alarmOn = isAlarmSet

            btnAlarm.setOnClickListener {
                // ── 1) 마스터 스위치 확인 ──
                val masterOn = requireContext()
                    .getSharedPreferences(MASTER_PREFS, Context.MODE_PRIVATE)
                    .getBoolean(MASTER_KEY, true)
                if (!masterOn) {
                    Toast.makeText(requireContext(), "알림이 OFF 상태입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // ── 2) 토글 동작: 알림 켜기/끄기 ──
                if (!alarmOn) {
                    // ▶ 알림 켜기: 예약
                    scheduleNotification(arrival.shuttleName, arrival.etaSec)
                    btnAlarm.setImageResource(R.drawable.act_bell)
                    Toast.makeText(
                        requireContext(),
                        "${arrival.shuttleName} 알림이 등록되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    prefs.edit().putBoolean(key, true).apply()
                } else {
                    // ▶ 알림 끄기: 취소
                    cancelNotification(arrival.shuttleName)
                    btnAlarm.setImageResource(R.drawable.non_act_bell)
                    Toast.makeText(
                        requireContext(),
                        "${arrival.shuttleName} 알림이 취소되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    prefs.edit().remove(key).apply()
                }
                alarmOn = !alarmOn
            }

            containerArrivalList.addView(itemView)
        }
    }

    /**
     * "셔틀명 – N분 M초 후 도착" 또는 "도착정보 없음" 문자열 반환
     */
    private fun formatEtaLine(shuttleName: String, etaSec: Long): String {
        return when {
            etaSec < 0    -> "$shuttleName – 도착정보 없음"
            etaSec <= 30  -> "$shuttleName – 곧 도착"
            etaSec < 60   -> "$shuttleName – ${etaSec}초 후 도착"
            else          -> {
                val m = etaSec / 60
                val s = etaSec % 60
                "$shuttleName – ${m}분 ${s}초 후 도착"
            }
        }
    }

    /**
     * AlarmManager를 이용해 “도착 3분 전”에 NotificationReceiver로 알림 Intent 전송 예약
     */
    private fun scheduleNotification(shuttleName: String, etaSec: Long) {
        // 1) 마스터 스위치가 OFF면 예약하지 않음 (안전하게 다시 한 번 체크)
        val masterOn = requireContext()
            .getSharedPreferences(MASTER_PREFS, Context.MODE_PRIVATE)
            .getBoolean(MASTER_KEY, true)
        if (!masterOn) {
            Toast.makeText(requireContext(), "알림이 OFF 상태입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val notifyBeforeSec = 180L // 3분 전
        val triggerInMs = (etaSec - notifyBeforeSec).coerceAtLeast(0L) * 1000L
        val alarmMgr = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("alarmType", "station")
            putExtra("bus_name", shuttleName)
            putExtra("station_name", stationName)
        }
        val requestCode = stationIdx * 1000 + shuttleName.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + triggerInMs
        try {
            // Android 12(API 31)+: 정확한 알람 권한 필요
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Toast.makeText(requireContext(), "도착 3분 전 알림이 예약되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (se: SecurityException) {
            // 정확한 알람 권한이 없을 때 “설정 화면”으로 유도
            Toast.makeText(requireContext(), "정확한 알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            val pm = requireContext().packageManager
            val intentSettings = Intent().apply {
                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                data = android.net.Uri.parse("package:${requireContext().packageName}")
            }
            if (intentSettings.resolveActivity(pm) != null) {
                startActivity(intentSettings)
            }
        }
    }

    /**
     * 이미 예약된 알림을 취소할 때 호출
     */
    private fun cancelNotification(shuttleName: String) {
        val alarmMgr = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = stationIdx * 1000 + shuttleName.hashCode()
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmMgr.cancel(pendingIntent)
    }
}
