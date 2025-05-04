package com.example.shuttlebusapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.example.shuttlebusapplication.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StationInfoBottomSheet(private val stationName: String) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_station_info_bottom_sheet, container, false)

        // 제목에 정류장 이름 표시
        val textTitle = view.findViewById<TextView>(R.id.textStationTitle)
        textTitle.text = stationName

        // 🔔 곧 도착 버스
        val textArrivingSoon = view.findViewById<TextView>(R.id.textArrivingSoon)
        textArrivingSoon.text = "곧 도착: 새내셔틀 (잠시 후 도착)"

        // 📋 도착 예정 셔틀들 (예시 데이터)
        val containerArrival = view.findViewById<LinearLayout>(R.id.containerArrivalList)
        val dummyArrivalList = listOf(
            "명지대역 셔틀 - 5분 후 도착",
            "새내셔틀 - 9분 후 도착"
        )

        dummyArrivalList.forEach { shuttleInfo ->
            val itemView = inflater.inflate(R.layout.item_arrival_bus, container, false)

            itemView.findViewById<TextView>(R.id.textShuttleInfo).text = shuttleInfo

            // 알림 버튼 클릭 리스너
            val btnAlarm = itemView.findViewById<ImageButton>(R.id.btnAlarm)
            btnAlarm.setOnClickListener {
                // TODO: 알림 예약 로직 연결 예정
            }

            // 셔틀 클릭 시 상세 페이지로 이동 예정
            itemView.setOnClickListener {
                // TODO: 버스 노선 페이지로 이동 로직
            }

            containerArrival.addView(itemView)
        }

        return view
    }
}
