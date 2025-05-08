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

    // 🚍 정류장별로 도착 가능한 셔틀 이름만 매핑
    private val stationToShuttles = mapOf(
        "명진당" to listOf("명지대역 셔틀"),
        "역북동행정복지센터 앞" to listOf("명지대역 셔틀", "시내셔틀"),
        "이마트.상공회의소 건너편" to listOf("명지대역 셔틀", "시내셔틀"),
        // 필요하면 다른 정류장도 추가
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stationName = arguments?.getString(ARG_STATION_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_station_info_bottom_sheet, container, false)

        val title = view.findViewById<TextView>(R.id.textStationTitle)
        val stopName = stationName ?: "정류장 정보 없음"
        title.text = stopName

        // 🚍 모든 더미 셔틀 정보
        val allShuttles = listOf(
            ArrivalInfo("명지대역 셔틀", 5),
            ArrivalInfo("시내셔틀", 2),
            ArrivalInfo("기흥셔틀", 7)
        )

        // ✅ 현재 정류장에 도착 가능한 셔틀만 필터링
        val allowedShuttles = stationToShuttles[stopName].orEmpty()
        val filteredShuttles = allShuttles.filter { it.name in allowedShuttles }

        // 🔔 곧 도착 셔틀 탐색 (2분 이하)
        val arrivingSoon = filteredShuttles.firstOrNull { it.minutesLeft <= 2 }
        val arrivingSoonText = arrivingSoon?.let {
            "곧 도착: ${it.name} (잠시 후 도착)"
        } ?: "곧 도착 셔틀 없음"
        view.findViewById<TextView>(R.id.textArrivingSoon).text = arrivingSoonText

        val containerArrival = view.findViewById<LinearLayout>(R.id.containerArrivalList)
        filteredShuttles
            .filter { it != arrivingSoon }
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

                // ✅ 명지대역 셔틀만 상세 화면 이동 가능
                itemView.setOnClickListener {
                    if (info.name == "명지대역 셔틀") {
                        val intent = Intent(requireContext(), RouteDetailActivity::class.java)
                        intent.putExtra("shuttleName", info.name)
                        dismiss()
                        startActivity(intent)
                    }
                }

                containerArrival.addView(itemView)
            }

        return view
    }

    companion object {
        private const val ARG_STATION_NAME = "station_name"

        fun newInstance(stationName: String): StationInfoBottomSheetFragment {
            val fragment = StationInfoBottomSheetFragment()
            val args = Bundle().apply {
                putString(ARG_STATION_NAME, stationName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    data class ArrivalInfo(val name: String, val minutesLeft: Int)
}
