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
    private lateinit var container: LinearLayout
    private lateinit var arrivingSoonText: TextView
    private lateinit var inflaterRef: LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stationName = arguments?.getString(ARG_STATION_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_station_info_bottom_sheet, container, false)
        inflaterRef = inflater

        view.findViewById<TextView>(R.id.textStationTitle).text = stationName ?: "정류장 정보 없음"
        arrivingSoonText = view.findViewById(R.id.textArrivingSoon)
        this.container = view.findViewById(R.id.containerArrivalList)

        loadArrivalInfo()

        view.findViewById<ImageButton>(R.id.btnRefresh).setOnClickListener {
            loadArrivalInfo()
        }

        return view
    }

    private fun loadArrivalInfo() {
        container.removeAllViews()

        val arrivalList = when (stationName) {
            "버스관리사무소 정류장 (기점)" -> listOf("명지대역 셔틀", "시내셔틀", "기흥셔틀")
            "이마트 상공회의소 앞", "역북동행정복지센터 건너편",
            "역북동 행정복지센터 앞", "이마트 상공회의소 건너편", "제3공학관" -> listOf("명지대역 셔틀", "시내셔틀")
            "명진당" -> listOf("명지대역 셔틀")
            else -> emptyList()
        }

        val arrivalInfos = arrivalList.map { name ->
            ArrivalInfo(name, listOf(1, 3, 5, 7).random())
        }

        val arrivingSoon = arrivalInfos.firstOrNull { it.minutesLeft <= 2 }
        arrivingSoonText.text = arrivingSoon?.let {
            "곧 도착: ${it.name} (잠시 후 도착)"
        } ?: "곧 도착 셔틀 없음"

        arrivalInfos.filter { it != arrivingSoon }.forEach { info ->
            val itemView = inflaterRef.inflate(R.layout.item_arrival_bus, container, false)
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
                val route = when (info.name) {
                    "명지대역 셔틀" -> listOf("채플관 앞", "이마트 상공회의소 앞","역북동 행정복지센터 건너편",
                        "명지대역 사거리","역북동 행정복지센터 앞","이마트 상공회의소 건너편 ","명진당", "제3공학관")
                    "시내셔틀" -> listOf(
                        "채플관 앞", "이마트 상공회의소 앞", "역북동 행정복지센터 건너편",
                        "동부경찰서 중앙지구대 앞", "용인 CGV", "중앙공영주차장 앞",
                        "역북동 행정복지센터 앞", "이마트 상공회의소 건너편",
                        "제1공학관", "제3공학관"
                    )
                    "기흥셔틀" -> listOf("채플관 앞", "기흥역 5번 출구", "채플관 앞")
                    else -> emptyList()
                }

                val intent = Intent(requireContext(), RouteDetailActivity::class.java).apply {
                    putExtra("shuttleName", info.name)
                    putStringArrayListExtra("stationList", ArrayList(route))
                }
                dismiss()
                startActivity(intent)
            }

            container.addView(itemView)
        }
    }

    companion object {
        private const val ARG_STATION_NAME = "station_name"
        fun newInstance(stationName: String): StationInfoBottomSheetFragment {
            return StationInfoBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATION_NAME, stationName)
                }
            }
        }
    }

    data class ArrivalInfo(val name: String, val minutesLeft: Int)
}
