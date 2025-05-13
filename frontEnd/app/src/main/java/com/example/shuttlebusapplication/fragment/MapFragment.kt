package com.example.shuttlebusapplication.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.LocationResponse
import com.example.shuttlebusapplication.network.RetrofitClient
import com.example.shuttlebusapplication.network.BusApiService
import com.example.shuttlebusapplication.network.MaplineResponse
import com.example.shuttlebusapplication.fragment.StationInfoBottomSheetFragment
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import kotlinx.coroutines.launch


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var currentNaverMap: NaverMap
    private var naverMap: NaverMap? = null  // 맵 참조 저장
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var busMarker: Marker
    private val busApi: BusApiService = RetrofitClient.busApi
    private var myLocationMarker: Marker? = null

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var pollingRunnable: Runnable
    private val pollingInterval = 5000L

    private val maplineApi by lazy { RetrofitClient.maplineApi }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    // 네이버 길찾기 api 좌표
    private val busRouteCoordinates = listOf(
        LatLng(37.22417, 127.18761),  // 버스관리사무소 정류장 근처 도로
        LatLng(37.23051, 127.18809),  // 이마트 앞
        LatLng(37.23401, 127.18863),  // 역북동행정복지센터 앞 도로
        LatLng(37.23850, 127.18960),  // 명지대역 사거리 (도로)
        LatLng(37.23405, 127.18880),  // 역북동행정복지센터 건너편 (도로)
        LatLng(37.23128, 127.18820),   // 광장 정류장 부근 도로
        LatLng(37.2223,127.1889), // 명진당
        LatLng(37.2195,127.1836) // 3공학관
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnMenu)?.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        view.findViewById<ImageButton>(R.id.btnMyLocation)?.setOnClickListener {
            moveToCurrentLocation()
        }

        mapView = view.findViewById(R.id.navermap_map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // 위치 권한 요청
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
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
    }

    override fun onMapReady(naverMap: NaverMap) {
        currentNaverMap = naverMap
        currentNaverMap.moveCamera(CameraUpdate.scrollTo(LatLng(37.2242, 127.1876)))

        drawRouteFromLocations(busRouteCoordinates)

        // 정류장 마커
        val locations = listOf(
            LatLng(37.2242,127.1876) to "버스관리사무소 정류장 (기점)",
            LatLng(37.2305,127.1881) to "이마트 앞",
            LatLng(37.234,127.1886) to "역북동행정복지센터 앞",
            LatLng(37.2385,127.1896) to "명지대역 사거리",
            LatLng(37.234,127.1888) to "역북동행정복지센터 건너편",
            LatLng(37.2313,127.1882) to "광장",
            LatLng(37.2223,127.1889) to "명진당",
            LatLng(37.2195,127.1836) to "제3공학관"
        )


        locations.forEach { (location, title) ->
            Marker().apply {
                position = location
                captionText = title
                icon = OverlayImage.fromResource(R.drawable.bus_marker)
                map = currentNaverMap

                setOnClickListener {
                    currentNaverMap.moveCamera(
                        CameraUpdate.scrollAndZoomTo(location, 17.0).animate(CameraAnimation.Easing)
                    )
                    StationInfoBottomSheetFragment.newInstance(title)
                        .show(parentFragmentManager, "StationInfoBottomSheet")
                    true
                }
            }
        }
        // --- 실시간 이동 마커 초기화 ---
        busMarker = Marker().apply {
            // 초기엔 지도가 표시하는 곳(예: 사무소 정류장)으로 세팅
            position = LatLng(37.2242, 127.1876)
            icon = OverlayImage.fromResource(R.drawable.bus_icon)
            map = naverMap
        }

        // 카메라는 움직이지 않고 마커만 따라가려면 주석 처리
        // naverMap.moveCamera(CameraUpdate.scrollTo(busMarker.position))

        // --- 폴링 시작 ---
        startPolling(naverMap)
    }

    /** locations 리스트를 받아 네이버 Directions API로 경로를 그리는 함수 */
    private fun drawRouteFromLocations(locations: List<LatLng>) {
        lifecycleScope.launch {
            try {
                val start = "${locations.first().longitude},${locations.first().latitude}"
                val goal  = "${locations.last().longitude},${locations.last().latitude}"
                val waypoints = locations
                    .subList(1, minOf(11, locations.size - 1))
                    .joinToString("|") { "${it.longitude},${it.latitude}" }
                    .takeIf { it.isNotEmpty() }

                val res: MaplineResponse = maplineApi.getDrivingPath(
                    start = start,
                    goal = goal,
                    waypoints = waypoints ,
                )

                // 응답의 path: List<List<Double>> 형태로 바뀜
                val pathCoords = res.route.traoptimal
                .flatMap { it.path }
                .map { coordPair ->
                // coordPair[0] = longitude, coordPair[1] = latitude
                LatLng(coordPair[1], coordPair[0])
                }

                PolylineOverlay().apply {
                    coords = pathCoords
                    color = Color.GREEN
                    width = 15
                    map = currentNaverMap
                }
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                val body = e.response()?.errorBody()?.string()
                Log.e("MapDebug", "경로 가져오기 실패: $code, $body", e)
                Toast.makeText(
                    requireContext(),
                    "경로 로딩 실패: HTTP $code",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("MapDebug", "경로 처리 오류", e)
                Toast.makeText(
                    requireContext(),
                    "경로 처리 중 오류가 발생했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)

                currentNaverMap.moveCamera(CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Easing))

                if (myLocationMarker == null) {
                    myLocationMarker = Marker().apply {
                        position = latLng // ✅ 먼저 위치 설정
                        icon = OverlayImage.fromResource(R.drawable.current_location)
                        width = 80
                        height = 80
                        map = currentNaverMap
                    }
                } else {
                    myLocationMarker?.position = latLng
                }
            }
        }, Looper.getMainLooper())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            moveToCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    /** 주기적으로 서버에서 최신 좌표를 가져와 마커를 옮기는 함수 */
    private fun startPolling(naverMap: NaverMap) {
        pollingRunnable = object : Runnable {
            override fun run() {
                lifecycleScope.launch {
                    try {
                        val loc: LocationResponse = busApi.getLatestLocation()
                        val newPos = LatLng(loc.lat, loc.lng)

                        // 마커 위치 업데이트 (UI 스레드)
                        busMarker.position = newPos

                        // 카메라도 함께 이동하고 싶으면 아래 주석 설정
                        // naverMap.moveCamera(CameraUpdate.scrollTo(newPos))

                    } catch (e: Exception) {
                        Log.e("MapDebug", "폴링 중 에러", e)
                    }
                }
                handler.postDelayed(this, pollingInterval)
            }
        }
        handler.post(pollingRunnable)
    }

    /** 폴링을 중단할 때 호출 */
    private fun stopPolling() {
        handler.removeCallbacks(pollingRunnable)
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        // Map이 준비된 상태라면 폴링 재개
        naverMap?.let { startPolling(it) }}
    override fun onPause() {
        stopPolling()
        mapView.onPause()
        super.onPause()}
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onDestroyView() {
        // 반드시 폴링 중단 후 MapView 해제
        stopPolling()
        mapView.onDestroy()
        super.onDestroyView()
    }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
