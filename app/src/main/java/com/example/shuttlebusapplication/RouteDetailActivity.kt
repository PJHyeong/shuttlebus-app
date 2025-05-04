package com.example.shuttlebusapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RouteDetailActivity : AppCompatActivity() {

    private val stationList = listOf(
        "채플관 앞",
        "이마트 상공회의소 앞",
        "역북동 행정복지센터 건너편",
        "명지대역 사거리",
        "역북동 행정복지센터 앞",
        "이마트 상공회의소 건너편",
        "명진당",
        "제3공학관"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)

        val stationContainer = findViewById<LinearLayout>(R.id.stationListContainer)

        stationList.forEachIndexed { index, name ->
            val itemLayout = layoutInflater.inflate(R.layout.item_station_line, stationContainer, false)

            val dot = itemLayout.findViewById<ImageView>(R.id.dot)
            val text = itemLayout.findViewById<TextView>(R.id.textStationName)
            text.text = name

            stationContainer.addView(itemLayout)
        }
    }
}
