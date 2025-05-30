package com.example.shuttlebusapplication.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.network.RetrofitClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var guideList: List<com.example.shuttlebusapplication.model.Guide>
    private lateinit var cumDurationMap: Map<Int, List<Long>>
    private var summaryDurationMs: Long = 0L

    // guide 에서 잡은 실제 회차 지점 인덱스
    private var pivotRouteIdx: Int = 0
    private var summaryGoalIdx: Int = 0

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    // 서버에서 3초마다 받아올 버스 위치용
    private val busApi = RetrofitClient.apiService
    private lateinit var busMarker: Marker

    // 경로 그리기 및 ETA 계산용
    private var routeLine: PolylineOverlay? = null
    private lateinit var routePath: List<LatLng>
    private var avgSpeed = 0.0                  // m/sec
    private var currentBusIndex = 0             // 현재 버스가 경로 상 몇 번째 인덱스인지
    private val stationMarkers = mutableListOf<Marker>()
    private val stationIndices = mutableMapOf<Marker, List<Int>>()

    // 폴링 제어
    private var pollingJob: Job? = null
    private val pollingInterval = 3_000L        // 3초

    // 노선상의 정류장 좌표들
    private val locations = listOf(
        LatLng(37.2242, 127.1876),  // 기점
        LatLng(37.2305, 127.1881),  // 이마트
        LatLng(37.233863, 127.188726), // 행정센터 맞은편
        LatLng(37.238471, 127.189537), // 명지대역
        LatLng(37.234104, 127.188628), // 행정센터
        LatLng(37.2313, 127.1882), // 광장
        LatLng(37.2223, 127.1889), // 명진당
        LatLng(37.2195, 127.1836)   // 종점
    )

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.navermap_map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map
        // 카메라는 기점으로 이동
        naverMap.moveCamera(CameraUpdate.scrollTo(locations.first())
            .animate(CameraAnimation.Easing))

        drawRouteOnMap()
    }

    // 1) 경로 요청 → Polyline, routePath, avgSpeed 초기화
    private fun drawRouteOnMap() {
        val coordStrings = locations.map { "${it.longitude},${it.latitude}" }
        val start     = coordStrings.first()
        val goal      = coordStrings.last()
        val waypoints = coordStrings.drop(1).dropLast(1)
            .takeIf { it.isNotEmpty() }
            ?.joinToString("|")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = RetrofitClient.directionService
                    .getRoute(start, goal, waypoints)
                if (!resp.isSuccessful) {
                    Log.e("RouteError", "code=${resp.code()} body=${resp.errorBody()?.string()}")
                    return@launch
                }
                val optimal = resp.body()?.route
                    ?.traoptimal
                    ?.firstOrNull()

                val rawPath = optimal?.path
                if (rawPath.isNullOrEmpty()) {
                    Toast.makeText(requireContext(),
                        "경로 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // [lon, lat] → LatLng 리스트
                routePath = rawPath.map { LatLng(it[1], it[0]) }

                // 평균 속도 계산 (m/sec)
                optimal.summary?.let {
                    avgSpeed = it.distance.toDouble() / (it.duration.toDouble() / 1000.0)
                }

                // 폴리라인 그리기
                routeLine?.map = null
                routeLine = PolylineOverlay().apply {
                    coords = routePath
                    width  = 15
                    color  = Color.GREEN
                    map    = naverMap
                }

                // ── B 방식: guide 누적합산 준비 ──
                guideList = optimal.guide.orEmpty()
                val guideList = optimal.guide.orEmpty()

                // ── P턴(3번 좌회전) 기반 pivot 검출 ──
                // ① 좌회전(type=2) 안내만 뽑고, pivotLoc(명지대역 사거리) 근처만 필터
                // proximity 필터 없이, 순수 guideList에서 type=2(좌회전) 이벤트만 뽑아서
                val leftEvents = guideList.filter { it.type == 2 }
                // ② 3번째 좌회전 이벤트가 있으면 그 지점, 없으면 마지막 지점, 또 없으면 snap fallback
                pivotRouteIdx = when {
                    leftEvents.size >= 2 -> leftEvents[2].pointIndex
                    leftEvents.isNotEmpty() -> leftEvents.last().pointIndex
                    else -> snapToRoute(locations[3]).second
                }

                val temp = mutableMapOf<Int, MutableList<Long>>()
                var acc = 0L
                guideList.forEach { g ->
                    acc += g.duration
                    // 경유지(type=87)나 목적지(type=88) 만 저장
                    if (g.type == 87 || g.type == 88) {
                        temp.getOrPut(g.pointIndex) { mutableListOf() }
                            .add(acc)
                    }
                }
                // ▶ 추가: summary.goal 에 담긴 마지막 목적지 누적 ms 도 저장
                optimal.summary?.let { summ ->
                    val goalIdx = summ.goal.pointIndex
                    val goalMs  = summ.duration.toLong()   // ms 단위 총 소요
                    summaryGoalIdx = goalIdx
                    temp.getOrPut(goalIdx) { mutableListOf() }.add(goalMs)
                    summaryDurationMs = summ.duration.toLong()
                }
                cumDurationMap = temp

                initStationMarkers()
                initBusMarker()
                startPolling()

            } catch (e: Exception) {
                Log.e("RouteException", "경로 처리 중 예외", e)
                Toast.makeText(requireContext(),
                    "경로 처리 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initStationMarkers() {
        stationMarkers.clear()
        stationIndices.clear()
        // pivotOrdinal 만 선언 (pivotRouteIdx 는 drawRouteOnMap() 에서 이미 할당됨)
        val pivotOrdinal = 3

        locations.forEachIndexed { ordinal, loc ->
            val name = stationNames.getOrNull(ordinal) ?: "정류장 $ordinal"

            // 2) 이 loc 에 대응하는 모든 routePath 上 인덱스들
            val idxList = routePath.mapIndexedNotNull { i, pt ->
                if (haversine(pt, loc) < 5.0) i else null
            }.ifEmpty { listOf(snapToRoute(loc).second) }

            val finalIdxList = if (ordinal == locations.lastIndex) idxList + summaryGoalIdx else idxList

            // 3) Marker 생성
            val marker = Marker().apply {
                position    = loc
                icon        = OverlayImage.fromResource(R.drawable.bus_marker)
                captionText = name
                map         = naverMap
            }

            stationMarkers += marker
            stationIndices[marker] = finalIdxList

            // 실제 버스 좌표 기준으로 computeEtaForStation() 호출
            marker.setOnClickListener {
                val etaSec = computeEtaForStation(marker)
                // ① BottomSheet 생성
                StationInfoBottomSheetFragment
                    .newInstance(name, etaSec, ordinal)
                // 👇 여기만 childFragmentManager 로 바꿔 줍니다.
                    .show(childFragmentManager, "StationInfo")
                true
            }
        }
    }

    // 3) 처음 버스 마커 세팅
    private fun initBusMarker() {
        busMarker = Marker().apply {
            position = locations.first()
            icon     = OverlayImage.fromResource(R.drawable.bus_icon)
            map      = naverMap
        }
    }

    /** 원 좌표를 경로上 가장 가까운 지점으로 스냅 → (LatLng, 인덱스) 반환 */
    private fun snapToRoute(
        raw: LatLng,
        minIdx: Int? = null,
        maxIdx: Int? = null
    ): Pair<LatLng, Int> {
        var bestIdx  = minIdx ?: 0
        var bestDist = Double.MAX_VALUE
        routePath.forEachIndexed { i, pt ->
            if ((minIdx != null && i < minIdx) || (maxIdx != null && i > maxIdx)) return@forEachIndexed
            val d = haversine(raw, pt)
            if (d < bestDist) {
                bestDist = d
                bestIdx  = i
            }
        }
        return routePath[bestIdx] to bestIdx
    }



    /**
     * 실제 버스 위치 기준으로 남은 ETA(초) 계산
     */
    private fun computeEtaForStation(marker: Marker): Long {
        // 1) 이 마커가 맵에 찍힌 station 좌표 리스트 중 몇 번째인지 찾기
        val ordinal = stationMarkers.indexOf(marker)
        if (ordinal < 0) return -1L

        // 2) station 위치 LatLng
        val stationPos = locations[ordinal]

        // 3) 현재 버스 위치 LatLng
        val busPos = busMarker.position

        // 1) P턴 전·후 구분
        val isOutbound = currentBusIndex < pivotRouteIdx

        // 2) busPos 스냅
        val (snappedBusPos, snappedBusIdx) = if (isOutbound) {
            snapToRoute(busPos, minIdx = 0, maxIdx = pivotRouteIdx)
        } else {
            snapToRoute(busPos, minIdx = pivotRouteIdx, maxIdx = routePath.lastIndex)
        }

        // 4) stationPos 스냅 (전체 경로) → 인덱스만
        val snappedStationIdx = snapToRoute(stationPos).second

        // ★ 이미 지난 정류장은 -1L 리턴
        if (snappedStationIdx <= snappedBusIdx) {
            return -1L
        }

        // 4) headDist, tailDist 계산 (기존 로직과 동일)
        val headDist = haversine(busPos, snappedBusPos)
        var tailDist = 0.0
        for (i in snappedBusIdx until snappedStationIdx) {
            tailDist += haversine(routePath[i], routePath[i+1])
        }

        // 5) 총 거리 → 초 단위 ETA
        val totalDist = headDist + tailDist
        if (avgSpeed <= 0.0) return -1L
        val etaSec = (totalDist / avgSpeed).toLong()
        return if (etaSec >= 0) etaSec else -1L
    }



    /** 위경도 두 지점間 거리 계산 (m 단위, Haversine) */
    private fun haversine(a: LatLng, b: LatLng): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(a.latitude  - b.latitude)
        val dLon = Math.toRadians(a.longitude - b.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val sinDlat = sin(dLat/2); val sinDlon = sin(dLon/2)
        val h = sinDlat*sinDlat + sinDlon*sinDlon * cos(lat1)*cos(lat2)
        return 2 * R * atan2(sqrt(h), sqrt(1-h))
    }

    // 4) 3초마다 서버 호출 → 스냅 + ETA 업데이트
    private fun startPolling() {
        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                try {
                    val loc = busApi.getLatestLocation()
                    val raw = LatLng(loc.lat, loc.lng)

                    // P턴 전(상행): 0..pivotRouteIdx 까지만 스냅
                    // P턴 후(하행): pivotRouteIdx..end 까지만 스냅
                    val (snapped, idx) = if (currentBusIndex < pivotRouteIdx) {
                        snapToRoute(raw, minIdx = 0, maxIdx = pivotRouteIdx)
                    } else {
                        snapToRoute(raw, minIdx = pivotRouteIdx, maxIdx = routePath.lastIndex)
                    }

                    busMarker.position  = snapped
                    currentBusIndex     = idx
                } catch (e: Exception) {
                    Log.e("MapDebug", "폴링 에러", e)
                }
                delay(pollingInterval)
            }
        }
    }

    fun computeEtaForStationByIdx(ordinal: Int): Long {
        val marker = stationMarkers.getOrNull(ordinal) ?: return -1L
        return computeEtaForStation(marker)
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() {
        super.onPause()
        pollingJob?.cancel()
        mapView.onPause()
    }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onDestroyView() {
        pollingJob?.cancel()
        mapView.onDestroy()
        super.onDestroyView()
    }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
