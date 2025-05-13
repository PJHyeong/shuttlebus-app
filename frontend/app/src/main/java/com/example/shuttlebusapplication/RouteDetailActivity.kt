package com.example.shuttlebusapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class RouteDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)

        val stationContainer = findViewById<LinearLayout>(R.id.stationListContainer)
        val stationList = intent.getStringArrayListExtra("stationList") ?: listOf()
        val shuttleName = intent.getStringExtra("shuttleName") ?: "셔틀 노선"

        // 제목 설정
        val titleText = findViewById<TextView?>(R.id.textRouteTitle)
        titleText?.text = shuttleName

        // 정류장 목록 표시
        stationList.forEachIndexed { index, stationName ->
            val itemLayout = layoutInflater.inflate(R.layout.item_station_line, stationContainer, false)

            val dot = itemLayout.findViewById<ImageView>(R.id.dot)
            val text = itemLayout.findViewById<TextView>(R.id.textStationName)
            val busIcon = itemLayout.findViewById<ImageView?>(R.id.busMarker)

            text.text = stationName

            // 예시: 버스 마커 표시 (index == 3일 때만 보이게)
            if (index == 3 && busIcon != null) {
                busIcon.visibility = View.VISIBLE
            }

            stationContainer.addView(itemLayout)
        }

        // 하단 뒤로가기 버튼
        val btnBack = findViewById<View>(R.id.btnBackToBottomSheet)
        btnBack.setOnClickListener {
            finish() // 팝업은 다시 지도를 눌러 띄우는 UX로 처리
        }
    }
}
