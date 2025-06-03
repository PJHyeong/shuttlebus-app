// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/fragment/MapFragment.kt

package com.example.shuttlebusapplication.fragment

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.Arrival
import com.example.shuttlebusapplication.network.RetrofitClient
import com.google.android.gms.location.*
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

    // ───────────────────────────────────────────────
    // 1) 네이버 맵 뷰, FusedLocationClient, Alarm 등 필수 변수 선언
    // ───────────────────────────────────────────────
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    // GPS 기반 현재 위치 받아오는 용도 (버튼 클릭 시 한 번만)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 내 위치 마커 (버튼 클릭할 때마다 생성/이전에 있으면 삭제)
    private var locationMarker: Marker? = null
    // “버스 위치”를 서버에서 3초마다 받아와 마커를 이동시키는 용도
    private val busApi = RetrofitClient.apiService
    private lateinit var busMarker: Marker

    // 경로(Polyline) 및 ETA 계산용
    private var routeLine: PolylineOverlay? = null
    private lateinit var routePath: List<LatLng>
    private var avgSpeed = 0.0                             // m/sec
    private var currentBusIndex = 0                        // 경로 상 버스 인덱스
    private val pollingInterval = 3_000L                   // 3초 주기
    private var pollingJob: Job? = null

    // 정류장(Station) 마커과 그 인덱스 맵
    private val stationMarkers = mutableListOf<Marker>()
    private val stationIndices = mutableMapOf<Marker, List<Int>>()

    // 누적 ETA 계산용
    private lateinit var guideList: List<com.example.shuttlebusapplication.model.Guide>
    private lateinit var cumDurationMap: Map<Int, List<Long>>
    private var summaryGoalIdx: Int = 0
    private var summaryDurationMs: Long = 0L
    private var pivotRouteIdx: Int = 0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        // SharedPreferences 파일명
        private const val PREFS_NAME = "alarm_prefs"
    }

    // ───────────────────────────────────────────────
    // 2) 노선별 정류장 좌표 및 이름 리스트
    // ───────────────────────────────────────────────
    private val locations = listOf(
        LatLng(37.2242, 127.1876),       // 0: 기점(버스관리사무소)
        LatLng(37.2305, 127.1881),       // 1: 이마트 앞
        LatLng(37.233863, 127.188726),   // 2: 역북동 행정복지센터 건너편
        LatLng(37.238471, 127.189537),   // 3: 명지대역 사거리
        LatLng(37.234104, 127.188628),   // 4: 역북동 행정복지센터 앞
        LatLng(37.2313, 127.1882),       // 5: 광장 정류장
        LatLng(37.2223, 127.1889),       // 6: 명진당 앞
        LatLng(37.2195, 127.1836)        // 7: 제3공학관 앞
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

    // ───────────────────────────────────────────────
    // 3) 정류장별 셔틀 운행 정보 (List<String>)
    //    • “명지대역 셔틀”만 ETA 계산 가능
    //    • “시내 셔틀", "기흥 셔틀" → ETA 없음(-1)
    // ───────────────────────────────────────────────
    private val stationShuttleMap: Map<Int, List<String>> = mapOf(
        0 to listOf("명지대역 셔틀", "시내 셔틀", "기흥 셔틀"),    // 기점
        1 to listOf("명지대역 셔틀", "시내 셔틀"),               // 이마트 앞
        2 to listOf("명지대역 셔틀", "시내 셔틀"),               // 역북동 행정복지센터 건너편
        3 to listOf("명지대역 셔틀"),                            // 명지대역 사거리
        4 to listOf("명지대역 셔틀", "시내 셔틀"),               // 역북동 행정복지센터 앞
        5 to listOf("명지대역 셔틀", "시내 셔틀"),               // 광장 정류장
        6 to listOf("명지대역 셔틀"),                            // 명진당 앞
        7 to listOf("명지대역 셔틀", "시내 셔틀")                // 3공학관 앞
    )

    // ───────────────────────────────────────────────
    // 4) Fragment 라이프사이클: onCreateView / onViewCreated
    // ───────────────────────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (1) 메인 메뉴로 이동 버튼
        view.findViewById<View>(R.id.btnMenu).setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        // (2) 현재 위치(내 위치) 버튼 → 클릭 시 한 번만 위치 마커 찍기
        view.findViewById<ImageButton>(R.id.btnMyLocation).setOnClickListener {
            centerCameraAndPlaceMarker()
        }

        // (3) FusedLocationClient 초기화 및 권한 요청
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // (4) MapView 초기화
        mapView = view.findViewById(R.id.navermap_map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    // 권한 요청 결과 콜백: 허용 시 아무 동작도 안 하고, 버튼 클릭 시에만 위치 얻도록 함
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // ───────────────────────────────────────────────
    // 5) OnMapReadyCallback 구현: 지도가 준비되면 drawRouteOnMap() 실행
    // ───────────────────────────────────────────────
    override fun onMapReady(map: NaverMap) {
        this.naverMap = map

        // (1) 카메라를 첫 정류장(기점)으로 이동
        naverMap.moveCamera(
            CameraUpdate.scrollTo(locations.first())
                .animate(CameraAnimation.Easing)
        )

        // (2) 경로 요청 및 초기화
        drawRouteOnMap()
    }

    // ───────────────────────────────────────────────
    // 6) drawRouteOnMap(): 네이버 Direction API 호출 후 Polyline/Guide/StationMarkers 초기화
    // ───────────────────────────────────────────────
    private fun drawRouteOnMap() {
        // (1) “경로 요청”을 위한 좌표 문자열 생성
        val coordStrings = locations.map { "${it.longitude},${it.latitude}" }
        val start = coordStrings.first()
        val goal = coordStrings.last()
        val waypoints = coordStrings.drop(1).dropLast(1)
            .takeIf { it.isNotEmpty() }
            ?.joinToString("|")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // (2) 네이버 Direction API 호출
                val resp = RetrofitClient.directionService
                    .getRoute(start, goal, waypoints)
                if (!resp.isSuccessful) {
                    Log.e("RouteError", "code=${resp.code()} body=${resp.errorBody()?.string()}")
                    return@launch
                }

                // (3) 최적 경로 정보 꺼내기
                val optimal = resp.body()?.route?.traoptimal?.firstOrNull()
                val rawPath = optimal?.path
                if (rawPath.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "경로 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // (4) “[lon, lat] → LatLng 리스트” 변환
                routePath = rawPath.map { LatLng(it[1], it[0]) }

                // (5) 평균 속도 계산 (m/sec)
                optimal.summary?.let {
                    avgSpeed = it.distance.toDouble() / (it.duration.toDouble() / 1000.0)
                }

                // (6) Polyline 그리기
                routeLine?.map = null
                routeLine = PolylineOverlay().apply {
                    coords = routePath
                    width = 15
                    color = Color.GREEN
                    map = naverMap
                }

                // (7) Guide 및 summary 정보를 기반으로 P턴(좌회전 이벤트) 계산
                guideList = optimal.guide.orEmpty()
                val leftEvents = guideList.filter { it.type == 2 }
                pivotRouteIdx = when {
                    leftEvents.size >= 2 -> leftEvents[2].pointIndex
                    leftEvents.isNotEmpty() -> leftEvents.last().pointIndex
                    else -> snapToRoute(locations[3]).second
                }

                // (8) 누적 시간(cumDurationMap) 계산
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
                    summaryDurationMs = summ.duration.toLong()
                }
                cumDurationMap = temp

                // (9) 정류장 마커 및 버스 마커 초기화
                initStationMarkers()
                initBusMarker()

                // (10) 서버 폴링 시작
                startPolling()

            } catch (e: Exception) {
                Log.e("RouteException", "경로 처리 중 예외", e)
                Toast.makeText(
                    requireContext(),
                    "경로 처리 오류: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ───────────────────────────────────────────────
    // 7) initStationMarkers(): 정류장마다 파란색 마커 찍기, 클릭 시 BottomSheet 호출
    // ───────────────────────────────────────────────
    private fun initStationMarkers() {
        stationMarkers.clear()
        stationIndices.clear()

        locations.forEachIndexed { ordinal, loc ->
            val name = stationNames.getOrNull(ordinal) ?: "정류장 $ordinal"

            // (1) 이 loc 에 대응하는 경로상 인덱스 리스트(스냅 오프셋)
            val idxList = routePath.mapIndexedNotNull { i, pt ->
                if (haversine(pt, loc) < 5.0) i else null
            }.ifEmpty { listOf(snapToRoute(loc).second) }

            // (2) 마지막 정류장이면 summaryGoalIdx 포함
            val finalIdxList = if (ordinal == locations.lastIndex) idxList + summaryGoalIdx else idxList

            // (3) Marker 생성 및 지도에 올리기
            val marker = Marker().apply {
                position = loc
                icon = OverlayImage.fromResource(R.drawable.bus_marker)
                captionText = name
                map = naverMap
            }

            stationMarkers += marker
            stationIndices[marker] = finalIdxList

            // (4) 클릭 시 BottomSheet 호출: getUpcomingArrivalsForStation(ordinal)
            marker.setOnClickListener {
                StationInfoBottomSheetFragment
                    .newInstance(name, ordinal)
                    .show(childFragmentManager, "StationInfo")
                true
            }
        }
    }

    // ───────────────────────────────────────────────
    // 8) initBusMarker(): 버스 아이콘 마커 최초 생성
    // ───────────────────────────────────────────────
    private fun initBusMarker() {
        busMarker = Marker().apply {
            position = locations.first()   // 처음에는 기점(0) 위치
            icon = OverlayImage.fromResource(R.drawable.bus_icon)
            map = naverMap
        }
    }

    // ───────────────────────────────────────────────
    // 9) snapToRoute(): 원 좌표를 경로상 가장 가까운 위치로 스냅
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
    // 10) computeEtaForStation(): 실제 버스 위치 기준으로 ETA(초) 계산
    // ───────────────────────────────────────────────
    private fun computeEtaForStation(marker: Marker): Long {
        val ordinal = stationMarkers.indexOf(marker)
        if (ordinal < 0) return -1L

        val stationPos = locations[ordinal]
        val busPos = busMarker.position

        val isOutbound = currentBusIndex < pivotRouteIdx
        val (snappedBusPos, snappedBusIdx) = if (isOutbound) {
            snapToRoute(busPos, minIdx = 0, maxIdx = pivotRouteIdx)
        } else {
            snapToRoute(busPos, minIdx = pivotRouteIdx, maxIdx = routePath.lastIndex)
        }

        val snappedStationIdx = snapToRoute(stationPos).second
        if (snappedStationIdx <= snappedBusIdx) return -1L

        val headDist = haversine(busPos, snappedBusPos)
        var tailDist = 0.0
        for (i in snappedBusIdx until snappedStationIdx) {
            tailDist += haversine(routePath[i], routePath[i + 1])
        }

        val totalDist = headDist + tailDist
        if (avgSpeed <= 0.0) return -1L
        val etaSec = (totalDist / avgSpeed).toLong()
        return if (etaSec >= 0) etaSec else -1L
    }

    // ───────────────────────────────────────────────
    // 11) getUpcomingArrivalsForStation(): 정류장별 셔틀 목록 반환
    // ───────────────────────────────────────────────
    fun getUpcomingArrivalsForStation(stationIdx: Int): List<Arrival> {
        val shuttleNames = stationShuttleMap[stationIdx] ?: emptyList()
        val arrivals = mutableListOf<Arrival>()
        shuttleNames.forEach { shuttleName ->
            if (shuttleName == "명지대역 셔틀") {
                val eta = computeEtaForStationByIdx(stationIdx)
                if (eta < 0) {
                    arrivals.add(Arrival(shuttleName, -1))
                } else {
                    arrivals.add(Arrival(shuttleName, eta))
                }
            } else {
                arrivals.add(Arrival(shuttleName, -1))
            }
        }
        return arrivals
    }

    // ───────────────────────────────────────────────
    // 12) computeEtaForStationByIdx(): index 받아 computeEtaForStation 호출
    // ───────────────────────────────────────────────
    fun computeEtaForStationByIdx(ordinal: Int): Long {
        val marker = stationMarkers.getOrNull(ordinal) ?: return -1L
        return computeEtaForStation(marker)
    }

    // ───────────────────────────────────────────────
    // 13) haversine(): 두 LatLng 간 거리 계산 (m 단위)
    // ───────────────────────────────────────────────
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

    // ───────────────────────────────────────────────
    // 14) startPolling(): 서버에서 주기적으로 버스 위치 받아오기 및 ETA < 0인 알람 취소
    // ───────────────────────────────────────────────
    private fun startPolling() {
        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            while (isActive) {
                try {
                    val loc = busApi.getLatestLocation()
                    val raw = LatLng(loc.lat, loc.lng)

                    val (snapped, idx) = if (currentBusIndex < pivotRouteIdx) {
                        snapToRoute(raw, minIdx = 0, maxIdx = pivotRouteIdx)
                    } else {
                        snapToRoute(raw, minIdx = pivotRouteIdx, maxIdx = routePath.lastIndex)
                    }
                    busMarker.position = snapped
                    currentBusIndex = idx

                    val allEntries = prefs.all
                    for ((key, _) in allEntries) {
                        val parts = key.split("|", limit = 2)
                        if (parts.size < 2) continue
                        val stationIdx = parts[0].toIntOrNull() ?: continue
                        val shuttleName = parts[1]
                        if (shuttleName == "명지대역 셔틀") {
                            val etaForStation = computeEtaForStationByIdx(stationIdx)
                            if (etaForStation < 0) {
                                cancelScheduledAlarm(stationIdx, shuttleName)
                                prefs.edit().remove(key).apply()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MapDebug", "폴링 에러", e)
                }
                delay(pollingInterval)
            }
        }
    }

    // ───────────────────────────────────────────────
    // 15) cancelScheduledAlarm(): 예약된 알람 즉시 취소
    // ───────────────────────────────────────────────
    private fun cancelScheduledAlarm(stationIdx: Int, shuttleName: String) {
        val alarmMgr = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = stationIdx * 1000 + shuttleName.hashCode()
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmMgr.cancel(pendingIntent)
    }

    // ───────────────────────────────────────────────
    // 16) centerCameraAndPlaceMarker(): 버튼 클릭 시 현재 위치로 카메라 이동 후 위치 마커 생성
    // ───────────────────────────────────────────────
    private fun centerCameraAndPlaceMarker() {
        // 위치 권한 체크
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && ::naverMap.isInitialized) {
                val latLng = LatLng(location.latitude, location.longitude)

                // (1) 기존 locationMarker가 있으면 제거
                locationMarker?.map = null

                // (2) 새로운 위치 마커 생성
                locationMarker = Marker().apply {
                    position = latLng
                    icon = OverlayImage.fromResource(R.drawable.current_location)
                    width = 80
                    height = 80
                    map = naverMap
                }

                // (3) 카메라를 현재 위치로 이동 (애니메이션)
                naverMap.moveCamera(
                    CameraUpdate.scrollTo(latLng)
                        .animate(CameraAnimation.Easing)
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "현재 위치를 가져올 수 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ───────────────────────────────────────────────
    // 17) Fragment 라이프사이클 오버라이드 메서드들
    // ───────────────────────────────────────────────
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        // locationMarker를 그대로 두고, 추가 업데이트는 하지 않음
        pollingJob?.cancel()
        mapView.onPause()
    }
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    override fun onDestroyView() {
        pollingJob?.cancel()
        mapView.onDestroy()
        super.onDestroyView()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
