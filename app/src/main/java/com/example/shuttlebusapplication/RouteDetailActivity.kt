package com.example.shuttlebusapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class RouteDetailActivity : AppCompatActivity() {

    data class StationInfo(
        val name: String,
        val hasBus: Boolean = false // 향후 실시간 위치 기반 마커 표시 용도
    )

    private val stationList = listOf(
        StationInfo("채플관 앞"),
        StationInfo("이마트 상공회의소 앞"),
        StationInfo("역북동 행정복지센터 건너편"),
        StationInfo("명지대역 사거리", hasBus = true),  // 예: 이 정류장에 버스가 있다고 가정
        StationInfo("역북동 행정복지센터 앞"),
        StationInfo("이마트 상공회의소 건너편"),
        StationInfo("명진당"),
        StationInfo("제3공학관")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)

        val stationContainer = findViewById<LinearLayout>(R.id.stationListContainer)

        stationList.forEachIndexed { index, station ->
            val itemLayout = layoutInflater.inflate(R.layout.item_station_line, stationContainer, false)

            val dot = itemLayout.findViewById<ImageView>(R.id.dot)
            val text = itemLayout.findViewById<TextView>(R.id.textStationName)
            val busIcon = itemLayout.findViewById<ImageView?>(R.id.busMarker) // 선택사항

            text.text = station.name

            // 🚍 버스 마커 표시 (임시 예시)
            if (station.hasBus && busIcon != null) {
                busIcon.visibility = View.VISIBLE
            }

            // 선 끊김 조절: 첫 줄은 위 선 X, 마지막은 아래 선 X (추후 UI 처리 가능)
            // 현재는 View 자체를 나누지 않았지만, 필요 시 위아래 선을 분리해서 조절 가능

            stationContainer.addView(itemLayout)
        }
    }
}
