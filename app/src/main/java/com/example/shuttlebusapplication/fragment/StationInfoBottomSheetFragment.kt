package com.example.shuttlebusapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.RouteDetailActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StationInfoBottomSheetFragment : BottomSheetDialogFragment() {

    private var stationName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stationName = arguments?.getString(ARG_STATION_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_station_info_bottom_sheet, container, false)

        view.findViewById<TextView>(R.id.textStationTitle).text = stationName ?: "정류장 정보 없음"

        // 📍 구조화된 더미 데이터
        val dummyArrivalList = listOf(
            ArrivalInfo("명지대역 셔틀", 5),
            ArrivalInfo("새내셔틀", 2),
            ArrivalInfo("기흥셔틀", 7)
        )

        // 🔔 곧 도착 셔틀 탐색 (2분 이하)
        val arrivingSoon = dummyArrivalList.firstOrNull { it.minutesLeft <= 2 }
        val arrivingSoonText = if (arrivingSoon != null) {
            "곧 도착: ${arrivingSoon.name} (잠시 후 도착)"
        } else {
            "곧 도착 셔틀 없음"
        }
        view.findViewById<TextView>(R.id.textArrivingSoon).text = arrivingSoonText

        // 📋 도착 예정 셔틀 리스트
        val containerArrival = view.findViewById<LinearLayout>(R.id.containerArrivalList)
        dummyArrivalList
            .filter { it != arrivingSoon } // 이미 '곧 도착'으로 표시된 것은 제외
            .forEach { info ->
                val itemView = inflater.inflate(R.layout.item_arrival_bus, containerArrival, false)

                val textInfo = itemView.findViewById<TextView>(R.id.textShuttleInfo)
                val btnAlarm = itemView.findViewById<ImageButton>(R.id.btnAlarm)

                val arrivalText = if (info.minutesLeft <= 2) "잠시 후 도착" else "${info.minutesLeft}분 후 도착"
                textInfo.text = "${info.name} - $arrivalText"

                btnAlarm.isSelected = false
                btnAlarm.setImageResource(R.drawable.non_act_bell)

                btnAlarm.setOnClickListener {
                    btnAlarm.isSelected = !btnAlarm.isSelected
                    val icon = if (btnAlarm.isSelected) R.drawable.act_bell else R.drawable.non_act_bell
                    btnAlarm.setImageResource(icon)
                }

                itemView.setOnClickListener {
                    val intent = Intent(requireContext(), RouteDetailActivity::class.java)
                    intent.putExtra("shuttleName", info.name)
                    startActivity(intent)
                }

                containerArrival.addView(itemView)
            }

        return view
    }

    companion object {
        private const val ARG_STATION_NAME = "station_name"

        fun newInstance(stationName: String): StationInfoBottomSheetFragment {
            val fragment = StationInfoBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_STATION_NAME, stationName)
            fragment.arguments = args
            return fragment
        }
    }

    data class ArrivalInfo(val name: String, val minutesLeft: Int)
}
