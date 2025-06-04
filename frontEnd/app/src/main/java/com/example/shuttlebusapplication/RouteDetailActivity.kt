// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/RouteDetailActivity.kt

package com.example.shuttlebusapplication

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RouteDetailActivity : AppCompatActivity() {

    // "정류장 목록" 컨테이너 (vertical LinearLayout)
    private lateinit var stationContainer: LinearLayout

    // 인텐트로 전달된 정류장 리스트
    private lateinit var stationList: List<String>
    // 인텐트로 전달된 최초 버스 인덱스 (MapFragment에서 RouteDetail로 넘겨준 값, 보통 1)
    private var startBusIndex: Int = 0

    // "각 정류장마다 표시할 버스 아이콘 ImageView" 목록
    private val busMarkerViews = mutableListOf<ImageView>()

    // 순차 이동을 제어하는 Job
    private var sequenceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)

        // 1) 인텐트에서 stationList, shuttleName, busIndex 값 받아오기
        stationList = intent.getStringArrayListExtra("stationList") ?: emptyList()
        val shuttleName = intent.getStringExtra("shuttleName") ?: "셔틀 노선"
        startBusIndex = intent.getIntExtra("busIndex", 0)

        // 2) 타이틀(셔틀명) 세팅
        findViewById<TextView>(R.id.textRouteTitle).text = shuttleName

        // 3) stationContainer에 stationList를 순서대로 inflate + 초기 상태: 모든 버스 아이콘 GONE
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
            busIcon.visibility = View.GONE
            busMarkerViews.add(busIcon)
            stationContainer.addView(itemLayout)
        }

        // 4) "이마트 앞(1)" 인덱스로 RouteDetail을 열었으므로, 첫 화면에서 이마트 앞에 마커 보이게 설정
        if (startBusIndex in busMarkerViews.indices) {
            busMarkerViews[startBusIndex].visibility = View.VISIBLE
        }

        // 5) 이후에는 "수동 시퀀스"로 버스 아이콘을 다음 정류장으로 옮김
        //    (단, Activity가 살아 있는 동안에만 작동하도록 lifecycleScope를 사용)
        startSequentialBusMovement()

        // 6) 뒤로가기 버튼 처리 (만약 XML에 버튼이 있다면)
        findViewById<View>(R.id.btnBackToBottomSheet)?.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity가 소멸될 때 시퀀스 코루틴도 같이 취소
        sequenceJob?.cancel()
    }

    /**
     * “이마트 앞(인덱스 1) → 그 다음 정류장(인덱스 2) … → 마지막 정류장(인덱스 7) → 기점(인덱스 0)” 순서로
     * 지정된 딜레이(밀리초)만큼 기다린 뒤 버스 아이콘을 이동시키는 로직
     */
    private fun startSequentialBusMovement() {
        // 이미 실행 중인 시퀀스가 있으면 취소
        sequenceJob?.cancel()

        // 정해진 “각 정류장 사이 대기 시간(밀리초)”을 설정 (순서: 1→2, 2→3, 3→4, 4→5, 5→6, 6→7, 7→0)
        val delaysInMs = listOf(
            70_000L,   // 이마트 앞(1)→역북동 행정복지센터 건너편(2)까지 70초 대기
            100_000L,  // (2)→명지대역 사거리(3)까지 100초
            200_000L,  // (3)→역북동 행정복지센터 앞(4)까지 200초
            60_000L,   // (4)→광장 정류장(5)까지 60초
            190_000L,  // (5)→명진당 앞(6)까지 190초
            170_000L,  // (6)→3공학관 앞(7)까지 170초
            60_000L    // (7)→기점(0)까지 60초
        )

        // “이마트 앞(인덱스 1)”부터 시작해서 위 delays 순서대로 이동
        // "이마트 앞(1)→역북동 행정복지센터 건너편(2)→..." 식으로 인덱스 시퀀스를 미리 만들어둡니다.
        val sequenceOfStationIndices = listOf(
            1,  // 이미 이마트 앞(1)에 있음
            2,
            3,
            4,
            5,
            6,
            7,
            0   // 마지막에는 기점(0)으로 돌아옴
        )

        sequenceJob = lifecycleScope.launch {
            // 0번째는 이미 화면에 표시했으므로 idx = 1부터 처리
            for (i in 1 until sequenceOfStationIndices.size) {
                // (1) 이전 마커 숨김
                val prevIdx = sequenceOfStationIndices[i - 1]
                if (prevIdx in busMarkerViews.indices) {
                    busMarkerViews[prevIdx].visibility = View.GONE
                }

                // (2) delaysInMs[i - 1] 만큼 대기 (예: i=1이면 delaysInMs[0]=70_000L 대기)
                val waitTime = delaysInMs.getOrNull(i - 1) ?: 0L
                if (waitTime > 0 && isActive) {
                    delay(waitTime)
                }

                // (3) 새로운 위치(현재 인덱스) 마커 보이기
                val newIdx = sequenceOfStationIndices[i]
                if (newIdx in busMarkerViews.indices) {
                    busMarkerViews[newIdx].visibility = View.VISIBLE
                }
            }
            // 반복이 끝나면 sequenceJob이 자동으로 취소됩니다.
            // 필요한 경우 여기서 추가 동작을 넣을 수도 있습니다.
        }
    }
}
