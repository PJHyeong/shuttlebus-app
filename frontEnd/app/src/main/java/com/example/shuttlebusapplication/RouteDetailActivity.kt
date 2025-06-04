package com.example.shuttlebusapplication

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RouteDetailActivity : AppCompatActivity() {

    // 정류장 목록 컨테이너
    private lateinit var stationContainer: LinearLayout

    // 전달 데이터
    private lateinit var stationList: List<String>
    private lateinit var shuttleName: String

    // 각 정류장마다 버스 마커 뷰
    private val busMarkerViews = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)

        // 1. 인텐트에서 데이터 받아오기
        stationList = intent.getStringArrayListExtra("stationList") ?: emptyList()
        shuttleName = intent.getStringExtra("shuttleName") ?: "셔틀 노선"
        val busIndex = intent.getIntExtra("busIndex", 0) // 실제 정류장 인덱스

        // 2. 타이틀 셋팅
        findViewById<TextView>(R.id.textRouteTitle).text = shuttleName

        // 3. 정류장 레이아웃에 리스트 inflate
        stationContainer = findViewById(R.id.stationListContainer)
        busMarkerViews.clear()
        stationList.forEachIndexed { index, stationName ->
            val itemLayout = layoutInflater.inflate(
                R.layout.item_station_line,
                stationContainer,
                false
            )
            val textView = itemLayout.findViewById<TextView>(R.id.textStationName)
            val busIcon = itemLayout.findViewById<ImageView>(R.id.busMarker)
            textView.text = stationName
            busIcon.visibility = View.GONE // 초기에는 모두 숨김
            busMarkerViews.add(busIcon)
            stationContainer.addView(itemLayout)
        }

        // 4. “명지대역 셔틀”인 경우에만 해당 인덱스 마커를 VISIBLE 처리
        if (shuttleName == "명지대역 셔틀") {
            busMarkerViews.forEachIndexed { idx, imageView ->
                imageView.visibility = if (idx == busIndex) View.VISIBLE else View.GONE
            }
        }
        // “시내 셔틀” 또는 “기흥 셔틀”은 마커를 모두 숨긴 채로 둡니다.

        // 5. 뒤로가기 버튼 처리 (있으면)
        findViewById<View>(R.id.btnBackToBottomSheet)?.setOnClickListener {
            finish()
        }
    }
}
