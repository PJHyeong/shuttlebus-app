// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/fragment/StationInfoBottomSheetFragment.kt

package com.example.shuttlebusapplication.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.RouteDetailActivity
import com.example.shuttlebusapplication.model.Arrival
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 정류장 클릭 시 표시되는 Bottom Sheet Dialog Fragment
 * - 상단: 정류장 이름 (클릭 리스너 없음)
 * - 중단: 도착 예정 버스 목록 (item_arrival_bus.xml)
 *   • 각 셔틀 이름(textShuttleInfo)을 클릭하면 RouteDetailActivity로 이동
 * - 하단: 새로고침 버튼 (btnRefresh)
 */
class StationInfoBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_STATION_NAME = "station_name"
        private const val ARG_STATION_IDX  = "station_idx"

        // SharedPreferences 파일명 (MapFragment와 동일)
        private const val PREFS_NAME = "alarm_prefs"

        /**
         * BottomSheetFragment 인스턴스를 생성할 때 호출할 newInstance 메서드
         * @param stationName 정류장 이름 (String)
         * @param stationIdx  정류장 인덱스 (Int)
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

    // arguments로 전달받은 정류장 이름과 인덱스
    private var stationName: String? = null
    private var stationIdx: Int      = -1

    // 동적으로 “도착 예정 버스 목록”을 추가할 컨테이너
    private lateinit var containerArrivalList: LinearLayout

    // 새로고침 버튼
    private lateinit var btnRefresh: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bundle에서 stationName, stationIdx 값을 읽어온다.
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
        // fragment_station_info_bottom_sheet.xml 레이아웃을 inflate
        val view = inflater.inflate(
            R.layout.fragment_station_info_bottom_sheet,
            container,
            false
        )

        // 상단 정류장 이름(TextView) – 클릭 리스너 없음
        val textStationTitle: TextView = view.findViewById(R.id.textStationTitle)
        textStationTitle.text = stationName

        // 중단: 도착 예정 버스 목록을 동적 추가할 컨테이너
        containerArrivalList = view.findViewById(R.id.containerArrivalList)

        // 하단: 새로고침 버튼
        btnRefresh = view.findViewById<ImageButton>(R.id.btnRefresh).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                refreshArrivalList()
            }
        }

        // 최초 로딩 시 목록 한 번 채우기
        refreshArrivalList()

        return view
    }

    /**
     * 부모 MapFragment.getUpcomingArrivalsForStation(...)을 호출하여
     * List<Arrival>을 받아온 뒤, item_arrival_bus.xml 레이아웃을 하나씩 inflate하여
     * 화면에 동적으로 추가한다.
     */
    private fun refreshArrivalList() {
        // 1) 기존 뷰 모두 제거
        containerArrivalList.removeAllViews()

        // SharedPreferences 열기
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 2) 부모 Fragment가 MapFragment인지 확인
        val parentFrag = parentFragment
        if (parentFrag !is MapFragment) {
            Toast.makeText(requireContext(), "지도 화면을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3) MapFragment에서 실제 도착 예정 버스 목록(List<Arrival>)을 가져온다
        val arrivals: List<Arrival> = parentFrag.getUpcomingArrivalsForStation(stationIdx)

        // 4) 만약 목록이 비어 있으면 “곧 도착 셔틀 없음” 텍스트 표시 후 종료
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

        // 5) 목록이 비어 있지 않으면, item_arrival_bus.xml을 한 항목씩 inflate하여 추가
        arrivals.forEach { arrival ->
            val itemView = layoutInflater.inflate(
                R.layout.item_arrival_bus,  // XML 파일명: item_arrival_bus.xml
                containerArrivalList,
                false
            )

            // a) 셔틀명 + ETA 텍스트 (클릭하면 RouteDetailActivity 로 이동)
            val textShuttleInfo: TextView = itemView.findViewById(R.id.textShuttleInfo)
            textShuttleInfo.text = formatEtaLine(arrival.shuttleName, arrival.etaSec)

            // “셔틀명(textShuttleInfo)” 클릭 시 RouteDetailActivity 호출
            textShuttleInfo.setOnClickListener {
                val clickedShuttle = arrival.shuttleName

                // ▶▶▶ 셔틀 이름에 따라 서로 다른 정류장 리스트를 만들어서 넘긴다.
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
                    else -> arrayListOf(  // 그 외 셔틀(예비)
                        "기점(버스관리사무소)",
                        "이마트 앞",
                        "명지대역 사거리"
                    )
                }

                // 인텐트에 넘겨 주기
                val intent = Intent(requireContext(), RouteDetailActivity::class.java).apply {
                    putStringArrayListExtra("stationList", stationListForIntent)
                    putExtra("shuttleName", clickedShuttle)
                }
                startActivity(intent)
            }

            // b) ETA가 60초 미만(1분 미만)일 때 “곧 도착”을 빨간색, 굵은 글씨로 변경
            if (arrival.etaSec in 0..59) {
                textShuttleInfo.setTextColor(resources.getColor(
                    android.R.color.holo_red_dark, null
                ))
                textShuttleInfo.setTypeface(
                    textShuttleInfo.typeface,
                    android.graphics.Typeface.BOLD
                )
            } else {
                // 그 외에는 검정색, 일반 폰트
                textShuttleInfo.setTextColor(resources.getColor(
                    android.R.color.black, null
                ))
                textShuttleInfo.setTypeface(
                    textShuttleInfo.typeface,
                    android.graphics.Typeface.NORMAL
                )
            }

            // c) 알림 버튼(btnAlarm)
            val btnAlarm: ImageButton = itemView.findViewById(R.id.btnAlarm)

            // SharedPreferences에 저장된 키 조회
            // 키 형식: "stationIdx|shuttleName"
            val key = "$stationIdx|${arrival.shuttleName}"
            val isAlarmSet = prefs.getBoolean(key, false)

            // (1) 초기 상태에 따라 아이콘 세팅
            if (isAlarmSet) {
                btnAlarm.setImageResource(R.drawable.act_bell)    // 활성 아이콘
            } else {
                btnAlarm.setImageResource(R.drawable.non_act_bell) // 비활성 아이콘
            }

            // 알림 토글 상태 저장용 변수
            var alarmOn = isAlarmSet

            btnAlarm.setOnClickListener {
                if (!alarmOn) {
                    // ▶ 알림 켜기: 도착 3분 전 예약
                    scheduleNotification(arrival.shuttleName, arrival.etaSec)
                    btnAlarm.setImageResource(R.drawable.act_bell)
                    Toast.makeText(
                        requireContext(),
                        "${arrival.shuttleName} 알림이 등록되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // SharedPreferences에 저장
                    prefs.edit().putBoolean(key, true).apply()

                } else {
                    // ▶ 알림 끄기: 이미 예약된 알람 취소
                    cancelNotification(arrival.shuttleName)
                    btnAlarm.setImageResource(R.drawable.non_act_bell)
                    Toast.makeText(
                        requireContext(),
                        "${arrival.shuttleName} 알림이 취소되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // SharedPreferences에서 삭제
                    prefs.edit().remove(key).apply()
                }
                alarmOn = !alarmOn
            }

            containerArrivalList.addView(itemView)
        }
    }

    /**
     * "셔틀명 – N분 M초 후 도착" 또는 "도착정보 없음" 형식으로 출력할 문자열 생성
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
        val notifyBeforeSec = 180L // 3분 전 (원하시면 60L로 바꿔 “1분 전”으로도 설정 가능)
        val triggerInMs = (etaSec - notifyBeforeSec).coerceAtLeast(0L) * 1000L

        val alarmMgr = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
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
            alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (se: SecurityException) {
            Toast.makeText(requireContext(), "정확한 알람 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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
