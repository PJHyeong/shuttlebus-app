package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.network.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import com.example.shuttlebusapplication.fragment.MapFragment

class StationInfoBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_STATION_NAME = "station_name"
        private const val ARG_ETA_SEC      = "eta_sec"
        private const val ARG_STATION_IDX  = "station_idx"

        fun newInstance(
            stationName: String,
            etaSec: Long,
            stationIdx: Int
        ): StationInfoBottomSheetFragment {
            return StationInfoBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATION_NAME, stationName)
                    putLong(ARG_ETA_SEC,        etaSec)
                    putInt(ARG_STATION_IDX, stationIdx)
                }
            }
        }
    }

    private var stationName: String? = null
    private var etaSec: Long = -1L
    private var stationIdx: Int = -1

    private lateinit var arrivingSoonText: TextView
    private lateinit var btnRefresh: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            stationName = it.getString(ARG_STATION_NAME)
            etaSec      = it.getLong(ARG_ETA_SEC)
            stationIdx  = it.getInt(ARG_STATION_IDX)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_station_info_bottom_sheet, container, false)
        .also { view ->
            // ① 텍스트뷰
            view.findViewById<TextView>(R.id.textStationTitle).text = stationName
            arrivingSoonText = view.findViewById(R.id.textArrivingSoon)
            arrivingSoonText.text = formatEta(etaSec)

            // ② 새로고침 버튼
            btnRefresh = view.findViewById<ImageButton>(R.id.btnRefresh)
                .apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        // MapFragment 인스턴스 찾아서 재계산
                        // 👇 deprecated targetFragment, findFragmentByTag 대신
                        val mapFrag = requireParentFragment() as MapFragment
                        val newEta = mapFrag?.computeEtaForStationByIdx(stationIdx) ?: -1L
                        arrivingSoonText.text = formatEta(newEta)
                    }
                }
        }

    private fun formatEta(sec: Long): String = when {
        sec <  0    -> "정보 없음"
        sec <= 30 -> "곧 도착"
        else        -> "${sec/60}분 ${sec%60}초"
    }

}
