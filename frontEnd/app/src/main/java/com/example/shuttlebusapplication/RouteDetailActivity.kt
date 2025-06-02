// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/RouteDetailActivity.kt

package com.example.shuttlebusapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shuttlebusapplication.network.RetrofitClient
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

class RouteDetailActivity : AppCompatActivity() {

    // 1) 정류장 목록 컨테이너
    private lateinit var stationContainer: LinearLayout

    // 2) 인텐트로 전달된 데이터
    private lateinit var stationList: List<String>
    private lateinit var shuttleName: String

    // 3) 각 정류장 항목 내 busMarker(ImageView)를 모아 둘 리스트
    private val busMarkerViews = mutableListOf<ImageView>()

    // 4) “버스 위치”를 서버에서 주기적으로 받아올 API
    private val busApi = RetrofitClient.apiService

    // 5) 정류장별 실제 위경도 좌표 (MapFragment와 동일하게 정의)
    private val locations = listOf(
        LatLng(37.2242, 127.1876),       // 0: 기점(버스관리사무소)
        LatLng(37.2305, 127.1881),       // 1: 이마트 앞
        LatLng(37.233863, 127.188726),   // 2: 역북동 행정복지센터 건너편
        LatLng(37.238471, 127.189537),   // 3: 명지대역 사거리
        LatLng(37.234104, 127.188628),   // 4: 역북동 행정복지센터 앞
        LatLng(37.2313, 127.1882),       // 5: 광장 정류장
        LatLng(37.2223, 127.1889),       // 6: 명진당 앞
        LatLng(37.2195, 127.1836)        // 7: 3공학관 앞
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)


        // 1) 인텐트에서 넘어온 정류장 리스트와 셔틀 이름을 받아온다.
        stationList = intent.getStringArrayListExtra("stationList") ?: emptyList()
        shuttleName = intent.getStringExtra("shuttleName") ?: "셔틀 노선"

        // 2) 화면 상단 타이틀에 셔틀 이름 표시
        findViewById<TextView>(R.id.textRouteTitle).text = shuttleName

        // 3) stationListContainer 레퍼런스
        stationContainer = findViewById(R.id.stationListContainer)

        // 4) stationList를 순회하면서 item_station_line.xml을 inflate
        stationList.forEachIndexed { index, stationName ->
            val itemLayout = layoutInflater.inflate(
                R.layout.item_station_line,
                stationContainer,
                false
            )

            val textView = itemLayout.findViewById<TextView>(R.id.textStationName)
            val busIcon  = itemLayout.findViewById<ImageView>(R.id.busMarker)

            textView.text = stationName
            // 처음에는 모든 버스 아이콘을 숨겨둔다
            busIcon.visibility = View.GONE

            // 리스트에 저장해 두었다가 "명지대역 셔틀"일 때만 보여 주기
            busMarkerViews.add(busIcon)

            stationContainer.addView(itemLayout)
        }

        // 5) 하단 뒤로가기 버튼 처리
        findViewById<View>(R.id.btnBackToBottomSheet).setOnClickListener {
            finish()
        }

        // 6) “명지대역 셔틀”인 경우에만 폴링을 시작하고,
        //    “시내 셔틀”이나 “기흥 셔틀”일 경우에는 절대 폴링을 하지 않는다.
        if (shuttleName == "명지대역 셔틀") {
            startBusLocationPolling()
        }
        // else: 시내 셔틀, 기흥 셔틀 → busMarkerViews 모두 GONE 유지
    }

    /**
     * 3초마다 서버에서 버스의 최신 위치를 받아와 “가장 가까운 정류장 인덱스”를 구한 뒤,
     * 해당 인덱스에 해당하는 busMarker만 VISIBLE로, 나머지는 GONE으로 업데이트한다.
     * 단, "명지대역 셔틀"인 경우에만 호출되며,
     * "시내 셔틀", "기흥 셔틀"은 호출되지 않는다.
     */
    private fun startBusLocationPolling() {
        lifecycleScope.launch {
            while (isActive) {
                try {
                    // 서버에서 최신 버스 위치 받아오기
                    val loc = busApi.getLatestLocation()
                    val busLatLng = LatLng(loc.lat, loc.lng)

                    // “가장 가까운 정류장 인덱스” 계산
                    val nearestStationIdx = findNearestStationIndex(busLatLng)

                    // UI 업데이트는 메인 스레드에서
                    runOnUiThread {
                        busMarkerViews.forEachIndexed { idx, imageView ->
                            imageView.visibility = if (idx == nearestStationIdx) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RouteDetailActivity", "폴링 중 오류 발생", e)
                    Toast.makeText(
                        this@RouteDetailActivity,
                        "버스 위치를 가져오는 중 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                delay(3000L) // 3초마다 반복
            }
        }
    }

    /**
     * 주어진 버스 좌표(busLatLng)와 “정류장별 좌표 목록(locations)”을 비교하여
     * 가장 거리가 짧은 정류장 인덱스를 반환한다.
     */
    private fun findNearestStationIndex(busLatLng: LatLng): Int {
        var minIdx = 0
        var minDist = Double.MAX_VALUE
        locations.forEachIndexed { idx, stationLatLng ->
            val d = haversine(busLatLng, stationLatLng)
            if (d < minDist) {
                minDist = d
                minIdx = idx
            }
        }
        return minIdx
    }

    /**
     * 위경도 두 점 간 거리를 미터 단위로 계산하는 Haversine 함수
     */
    private fun haversine(a: LatLng, b: LatLng): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(a.latitude - b.latitude)
        val dLon = Math.toRadians(a.longitude - b.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val sinDlat = sin(dLat / 2); val sinDlon = sin(dLon / 2)
        val h = sinDlat * sinDlat + sinDlon * sinDlon * cos(lat1) * cos(lat2)
        return 2 * R * atan2(sqrt(h), sqrt(1 - h))
    }
}
