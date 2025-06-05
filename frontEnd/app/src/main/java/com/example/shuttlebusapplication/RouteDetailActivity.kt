package com.example.shuttlebusapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RouteDetailActivity : AppCompatActivity() {

    private lateinit var stationContainer: LinearLayout
    private lateinit var stationList: List<String>
    private lateinit var shuttleName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)

        // 뒤로가기 버튼
        findViewById<ImageButton>(R.id.btnBackToBottomSheet).setOnClickListener {
            finish()
        }

        // Intent에서 정류장 리스트와 셔틀 이름 받아오기
        stationList = intent.getStringArrayListExtra("stationList") ?: emptyList()
        shuttleName = intent.getStringExtra("shuttleName") ?: "셔틀 노선"

        // 셔틀 이름 화면 타이틀 세팅
        findViewById<TextView>(R.id.textRouteTitle).text = shuttleName

        // 정류장 이름만 세로로 나열
        stationContainer = findViewById(R.id.stationListContainer)
        stationContainer.removeAllViews()
        stationList.forEach { stationName ->
            val itemLayout = layoutInflater.inflate(
                R.layout.item_station_line,
                stationContainer,
                false
            )
            val textView = itemLayout.findViewById<TextView>(R.id.textStationName)
            textView.text = stationName

            // busMarker 관련 코드 완전 삭제!
            // (xml에서 해당 뷰가 없으니 busMarker 관련 코드도 전부 제거)

            stationContainer.addView(itemLayout)
        }
    }
}
