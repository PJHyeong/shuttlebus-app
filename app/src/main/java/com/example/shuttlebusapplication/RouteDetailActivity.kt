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

        val titleText = findViewById<TextView?>(R.id.textRouteTitle)
        titleText?.text = shuttleName

        stationList.forEachIndexed { index, stationName ->
            val itemLayout = layoutInflater.inflate(R.layout.item_station_line, stationContainer, false)

            val dot = itemLayout.findViewById<ImageView>(R.id.dot)
            val text = itemLayout.findViewById<TextView>(R.id.textStationName)
            val busIcon = itemLayout.findViewById<ImageView?>(R.id.busMarker)

            text.text = stationName

            // 🚍 버스 위치 예시: index == 3이면 버스 표시
            if (index == 3 && busIcon != null) {
                busIcon.visibility = View.VISIBLE
            }

            stationContainer.addView(itemLayout)
        }
    }
}
