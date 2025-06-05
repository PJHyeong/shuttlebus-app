// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/RouteDetailActivity.kt

package com.example.shuttlebusapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shuttlebusapplication.model.LocationResponse
import com.example.shuttlebusapplication.network.RetrofitClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.PolylineOverlay
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

class RouteDetailActivity : AppCompatActivity() {

    // ───────────────────────────────────────────────
    // 1) Retrofit API 호출용 인스턴스
    // ───────────────────────────────────────────────
    private val busApi = RetrofitClient.apiService

    // ───────────────────────────────────────────────
    // 2) 화면에 세로로 정류장 리스트를 그릴 LinearLayout
    //    (activity_route_detail.xml 에 id="stationListContainer" 로 정의)
    // ───────────────────────────────────────────────
    private lateinit var stationContainer: LinearLayout

    // ───────────────────────────────────────────────
    // 3) Intent 로 넘어온 “정류장 이름 리스트”
    // ───────────────────────────────────────────────
    private lateinit var stationList: List<String>

    // ───────────────────────────────────────────────
    // 4) “각 정류장 옆에 표시할 ImageView(버스 아이콘)” 모아두는 리스트
    // ───────────────────────────────────────────────
    private val busMarkerViews = mutableListOf<ImageView>()

    // ───────────────────────────────────────────────
    // 5) 현재 셔틀 이름 저장용 변수
    // ───────────────────────────────────────────────
    private lateinit var shuttleName: String


    // ───────────────────────────────────────────────
    // 6) MapFragment 와 동일하게 사용할 경로(Polyline) 관련 필드들
    // ───────────────────────────────────────────────

    /** (a) 경로를 구성할 정류장 좌표 목록 (8개) */
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

    /** (b) 정류장 이름 리스트 (MapFragment 와 동일) */
    private val stationNames = listOf(
        "기점(버스관리사무소)",
        "이마트 앞",
        "역북동 행정복지센터 건너편",
        "명지대역 사거리",
        "역북동 행정복지센터 앞",
        "광장 정류장",
        "명진당 앞",
        "3공학관 앞"
    )

    /** (c) Direction API 호출 결과 → 파싱 후 저장할 LatLng 경로 */
    private lateinit var routePath: List<LatLng>

    /** (d) PolylineOverlay (지도를 실제로 그릴 때 필요하지만, 여기서는 내부 로직용으로만 남겨둠) */
    private var routeLine: PolylineOverlay? = null

    /** (e) 평균 속도 (m/sec) */
    private var avgSpeed = 0.0

    /** (f) 회전(턴) 지점 인덱스 */
    private var pivotRouteIdx: Int = 0

    /** (g) guideList: Direction API 가 보내준 “회전 정보 리스트” */
    private lateinit var guideList: List<com.example.shuttlebusapplication.model.Guide>

    /** (h) cumDurationMap: 경로 인덱스별 누적 시간(밀리초) 매핑 */
    private lateinit var cumDurationMap: Map<Int, List<Long>>

    /** (i) summary 정보의 최종 반환점 인덱스 */
    private var summaryGoalIdx: Int = 0

    /** (j) 현재 버스가 경로상 어느 인덱스에 스냅됐는지 저장 */
    private var currentBusIndex = 0

    /** (k) 버스 좌표를 3초마다 갱신할 폴링 주기 (밀리초) */
    private val pollingInterval = 3_000L

    /** (l) 폴링 Job */
    private var pollingJob: Job? = null

    /** (m) “정류장 ordinal → routePath 상 인덱스 리스트” 매핑 */
    private val stationRouteIdxMap = mutableMapOf<Int, List<Int>>()


    // ───────────────────────────────────────────────
    // onCreate: 레이아웃 세팅 + Intent 데이터 수신 + UI 초기화 + 경로 초기화/폴링 시작
    // ───────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)

        // ① 뒤로가기 버튼 리스너: 누르면 Activity 종료 → 바텀시트(이전 화면)로 복귀
        findViewById<ImageButton>(R.id.btnBackToBottomSheet).setOnClickListener {
            finish()
        }

        // ② Intent에서 정류장 리스트와 셔틀 이름 받아오기
        stationList = intent.getStringArrayListExtra("stationList") ?: emptyList()
        shuttleName = intent.getStringExtra("shuttleName") ?: "셔틀 노선"

        // ③ 셔틀 이름 받아와서 화면 타이틀 세팅
        findViewById<TextView>(R.id.textRouteTitle).text = shuttleName

        // ④ stationContainer에 정류장 이름 + Bus Icon(ImageView) 초기화
        stationContainer = findViewById(R.id.stationListContainer)
        busMarkerViews.clear()
        stationList.forEachIndexed { index, stationName ->
            val itemLayout = layoutInflater.inflate(
                R.layout.item_station_line,
                stationContainer,
                false
            )
            val textView = itemLayout.findViewById<TextView>(R.id.textStationName)
            val busIcon  = itemLayout.findViewById<ImageView>(R.id.busMarker)

            textView.text = stationName
            // 우선 모두 숨김 상태로 추가
            busIcon.visibility = View.GONE

            busMarkerViews.add(busIcon)
            stationContainer.addView(itemLayout)
        }

        // ──────────────────────────────────────────────────
        // ⑤ 추가: 명지대역 셔틀이 아닐 경우엔 마커를 표시하지 않고, 로직 중단
        if (shuttleName != "명지대역 셔틀") {
            // 명지대역 셔틀이 아닐 땐, 기점에도 마커 안 보이도록 그냥 반환
            return
        }
        // 명지대역 셔틀인 경우에만 “기점(정류장 0번)에 아이콘” 미리 표시
        if (busMarkerViews.isNotEmpty()) {
            busMarkerViews[0].visibility = View.VISIBLE
        }
        // ──────────────────────────────────────────────────

        // ⑥ 명지대역 셔틀일 때만 경로 초기화 → stationRouteIdxMap 구축 → 폴링 시작
        drawRouteAndInit()
    }

    // ───────────────────────────────────────────────
    // onResume: 화면 재진입 시 폴링이 꺼져 있으면 재시작
    // ───────────────────────────────────────────────
    override fun onResume() {
        super.onResume()
        // 명지대역 셔틀일 때만 폴링 재개
        if (shuttleName == "명지대역 셔틀" &&
            ::routePath.isInitialized &&
            (pollingJob == null || pollingJob?.isCancelled == true)
        ) {
            startPolling()
        }
    }

    // ───────────────────────────────────────────────
    // onPause: 화면 벗어날 때 폴링 취소 (옵션)
    // ───────────────────────────────────────────────
    override fun onPause() {
        super.onPause()
        // 명지대역 셔틀일 때만 폴링 취소
        if (shuttleName == "명지대역 셔틀") {
            pollingJob?.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 명지대역 셔틀일 때만 폴링 취소
        if (shuttleName == "명지대역 셔틀") {
            pollingJob?.cancel()
        }
    }

    // ───────────────────────────────────────────────
    // drawRouteAndInit(): Direction API 호출 → routePath 생성 → stationRouteIdxMap 구축 → startPolling()
    // ───────────────────────────────────────────────
    private fun drawRouteAndInit() {
        lifecycleScope.launch {
            try {
                // (1) “lon,lat” 문자열 만들기 (MapFragment와 동일)
                val coordStrings = locations.map { "${it.longitude},${it.latitude}" }
                val start = coordStrings.first()
                val goal  = coordStrings.last()
                val waypoints = coordStrings
                    .drop(1)
                    .dropLast(1)
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString("|")

                // (2) Retrofit 호출: Direction API 호출
                val resp = RetrofitClient.directionService.getRoute(
                    start = start,
                    goal = goal,
                    waypoints = waypoints
                )
                if (!resp.isSuccessful) {
                    Log.e("RouteDetail", "Direction API error: code=${resp.code()}")
                    Toast.makeText(
                        this@RouteDetailActivity,
                        "경로 정보를 가져올 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // (3) 최적 경로 꺼내기
                val optimal = resp.body()?.route?.traoptimal?.firstOrNull()
                val rawPath = optimal?.path
                if (rawPath.isNullOrEmpty()) {
                    Toast.makeText(
                        this@RouteDetailActivity,
                        "경로 데이터가 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // (4) “[lon, lat] → LatLng 리스트”로 변환
                routePath = rawPath.map { LatLng(it[1], it[0]) }

                // (5) 평균 속도 계산 (m/sec)
                optimal.summary?.let {
                    avgSpeed = it.distance.toDouble() / (it.duration.toDouble() / 1000.0)
                }

                // (6) guideList / pivotRouteIdx 계산
                guideList = optimal.guide.orEmpty()
                val leftEvents = guideList.filter { it.type == 2 }
                pivotRouteIdx = when {
                    leftEvents.size >= 2 -> leftEvents[2].pointIndex
                    leftEvents.isNotEmpty() -> leftEvents.last().pointIndex
                    else -> snapToRoute(locations[3]).second
                }

                // (7) cumDurationMap 계산 (회전 이벤트 기준 누적 시간)
                val temp = mutableMapOf<Int, MutableList<Long>>()
                var acc = 0L
                guideList.forEach { g ->
                    acc += g.duration
                    if (g.type == 87 || g.type == 88) {
                        temp.getOrPut(g.pointIndex) { mutableListOf() }
                            .add(acc)
                    }
                }
                optimal.summary?.let { summ ->
                    val goalIdx = summ.goal.pointIndex
                    val goalMs = summ.duration.toLong()
                    summaryGoalIdx = goalIdx
                    temp.getOrPut(goalIdx) { mutableListOf() }.add(goalMs)
                }
                cumDurationMap = temp

                // (8) “정류장 ordinal → routePath 인덱스 리스트” 계산
                initStationRouteIdxMap()

                // (9) 폴링 시작
                startPolling()

            } catch (e: Exception) {
                Log.e("RouteDetail", "drawRoute 예외", e)
                Toast.makeText(
                    this@RouteDetailActivity,
                    "경로 처리 오류: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ───────────────────────────────────────────────
    // initStationRouteIdxMap(): “정류장 좌표 → routePath 인덱스” 매핑
    // ───────────────────────────────────────────────
    private fun initStationRouteIdxMap() {
        stationRouteIdxMap.clear()

        locations.forEachIndexed { ordinal, loc ->
            // (1) routePath에서 loc과 5m 이하로 가까운 포인트 인덱스 모으기
            val idxList = routePath.mapIndexedNotNull { i, pt ->
                if (haversine(pt, loc) < 5.0) i else null
            }.ifEmpty {
                // 가까운 포인트가 없으면 snapToRoute를 통해 하나라도 가져오기
                listOf(snapToRoute(loc).second)
            }

            // (2) 마지막 정류장인 경우 summaryGoalIdx도 포함
            val finalIdxList = if (ordinal == locations.lastIndex) {
                idxList + summaryGoalIdx
            } else {
                idxList
            }

            stationRouteIdxMap[ordinal] = finalIdxList
        }
    }

    // ───────────────────────────────────────────────
    // startPolling(): 3초마다 서버에서 LocationResponse 받아와 LatLng 생성 → snapToRoute → currentBusIndex 갱신 → updateBusMarkerView
    // ───────────────────────────────────────────────
    private fun startPolling() {
        pollingJob = lifecycleScope.launch {
            while (isActive) {
                try {
                    // ① Suspend함수 호출: getLatestLocation() → LocationResponse
                    val loc: LocationResponse = busApi.getLatestLocation()

                    // ② LocationResponse가 (val lat: Double, val lng: Double)이므로 이를 사용
                    val rawBusLatLng = LatLng(loc.lat, loc.lng)

                    // ③ snapToRoute 호출 → snappedLatLng, 인덱스 계산
                    val (snapped, idx) = if (currentBusIndex < pivotRouteIdx) {
                        snapToRoute(rawBusLatLng, minIdx = 0, maxIdx = pivotRouteIdx)
                    } else {
                        snapToRoute(rawBusLatLng, minIdx = pivotRouteIdx, maxIdx = routePath.lastIndex)
                    }
                    currentBusIndex = idx

                    // ④ UI 갱신: runOnUiThread { updateBusMarkerView() }
                    runOnUiThread {
                        updateBusMarkerView()
                    }

                } catch (e: Exception) {
                    Log.e("RouteDetail", "폴링 예외", e)
                }
                delay(pollingInterval)
            }
        }
    }

    // ───────────────────────────────────────────────
    // updateBusMarkerView(): currentBusIndex 기준으로 ImageView(VISIBLE/GONE) 전환
    // ───────────────────────────────────────────────
    private fun updateBusMarkerView() {
        // (1) “currentBusIndex ≥ routeIdx”인 모든 ordinal 필터링
        val passedOrdinals = stationRouteIdxMap.entries
            .filter { (_, idxList) ->
                idxList.any { routeIdx -> currentBusIndex >= routeIdx }
            }
            .map { entry -> entry.key }

        // (2) 그중 가장 큰 ordinal 선택, 없으면 0
        val currentOrdinal = passedOrdinals.maxOrNull() ?: 0

        // (3) 모든 ImageView를 숨기고
        busMarkerViews.forEach { it.visibility = View.GONE }

        // (4) 해당 ordinal의 ImageView만 보이게
        if (currentOrdinal in busMarkerViews.indices) {
            busMarkerViews[currentOrdinal].visibility = View.VISIBLE
        }
    }

    // ───────────────────────────────────────────────
    // snapToRoute(): raw(LatLng) → routePath 상 가장 가까운 지점으로 스냅 → (LatLng, index) 반환
    // ───────────────────────────────────────────────
    private fun snapToRoute(
        raw: LatLng,
        minIdx: Int? = null,
        maxIdx: Int? = null
    ): Pair<LatLng, Int> {
        var bestIdx = minIdx ?: 0
        var bestDist = Double.MAX_VALUE

        routePath.forEachIndexed { i, pt ->
            if ((minIdx != null && i < minIdx) || (maxIdx != null && i > maxIdx)) return@forEachIndexed
            val d = haversine(raw, pt)
            if (d < bestDist) {
                bestDist = d
                bestIdx = i
            }
        }
        return routePath[bestIdx] to bestIdx
    }

    // ───────────────────────────────────────────────
    // haversine(): 두 LatLng 간 거리 계산 (미터 단위)
    // ───────────────────────────────────────────────
    private fun haversine(a: LatLng, b: LatLng): Double {
        val R = 6371000.0 // 지구 반지름(m)
        val dLat = Math.toRadians(a.latitude - b.latitude)
        val dLon = Math.toRadians(a.longitude - b.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)

        val sinDlat = sin(dLat / 2)
        val sinDlon = sin(dLon / 2)
        val h = sinDlat * sinDlat + sinDlon * sinDlon * cos(lat1) * cos(lat2)
        return 2 * R * atan2(sqrt(h), sqrt(1 - h))
    }
}
