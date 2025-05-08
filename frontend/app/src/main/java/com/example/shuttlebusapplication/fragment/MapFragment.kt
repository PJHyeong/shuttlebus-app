package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.model.LocationResponse
import com.example.shuttlebusapplication.network.RetrofitClient
import com.example.shuttlebusapplication.network.BusApiService
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.launch
import com.naver.maps.map.overlay.PolylineOverlay
import android.graphics.Color


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val POLLING_INTERVAL = 5000L  // 5초

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null  // 맵 참조 저장
    private lateinit var routeLine: PolylineOverlay

    // 1) 폴링용 핸들러와 Runnable
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var pollingRunnable: Runnable
    // 2) 버스 마커 하나만 계속 이동시키기 위해 미리 만들어 둡니다
    private lateinit var busMarker: Marker
    // 3) Retrofit API 서비스
    private val busApi: BusApiService = RetrofitClient.busApi


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MapView 초기화
        mapView = view.findViewById(R.id.navermap_map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("MapDebug", "✅ onMapReady called!")

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.2223, 127.1889))
        naverMap.moveCamera(cameraUpdate)


        // 마커 추가
        val locations = listOf(
            LatLng(37.2242, 127.1876) to "버스관리사무소 정류장 (기점)",
            LatLng(37.2305, 127.1881) to "이마트.상공회의소 앞",
            LatLng(37.234, 127.1888) to "역북동행정복지센터 건너편",
            LatLng(37.237831, 127.189690) to "지나가는길, 다리 전",
            LatLng(37.2385, 127.1896) to "명지대역 사거리",
            LatLng(37.238741, 127.186195) to "지나가는길",
            LatLng(37.236736, 127.186406) to "지나가는길",
            LatLng(37.236125, 127.189144) to "지나가는길, 명지대사거리",
            LatLng(37.234, 127.1886) to "역북동행정복지센터 앞",
            LatLng(37.2313, 127.1882) to "이마트.상공회의소 건너편",
            LatLng(37.2223, 127.1889) to "명진당",
            LatLng(37.2195, 127.1836) to "제3공학관"
        )

        val buslocations = listOf(
            LatLng(37.2195, 127.1836) , // 3공
            LatLng(37.220252, 127.186613) , // 3공 -> 명진
            LatLng(37.221042, 127.186816) , // 3공 -> 명진
            LatLng(37.221343, 127.187862) , // 함박앞
            LatLng(37.222043, 127.189143) ,
            LatLng(37.223287, 127.188026) ,
            LatLng(37.224392, 127.187806) , // 학교정문
            LatLng(37.224212, 127.187576) , // 기점
            LatLng(37.224392, 127.187806) , // 학교정문
            LatLng(37.224973, 127.187808) , //학교앞 로터리
            LatLng(37.225849, 127.187947) , //삼거리
            LatLng(37.228031, 127.187671) , // 고가밑
            LatLng(37.236138, 127.189164) , //명지대입구사거리
            LatLng(37.238266, 127.189874) , // 명지대역사거리
            LatLng(37.238731, 127.186211) , // 좌회전
            LatLng(37.236948, 127.185178) , // 우회전
            LatLng(37.236138, 127.189164) , //명지대입구사거리
        )

        val routeLine = PolylineOverlay().apply {
            coords = buslocations                    // 경로 좌표
            color  = Color.BLUE                     // 파란색 선 :contentReference[oaicite:0]{index=0}
            width  = 15                              // 선 굵기 8px :contentReference[oaicite:1]{index=1}
            map    = naverMap                       // 지도에 추가
        }

        locations.forEach { (location, title) ->
            val marker = Marker()
            marker.position = location
            marker.captionText = title
            marker.anchor = Marker.DEFAULT_ANCHOR
            marker.map = naverMap

            // 아이콘 설정 및 클릭 가능 여부
            val isMainStop = when (title) {
                "지나가는길, 명지대사거리", "지나가는길", "지나가는길, 다리 전" -> false
                else -> true
            }

            marker.icon = if (isMainStop) {
                OverlayImage.fromResource(R.drawable.bus_marker)
            } else {
                OverlayImage.fromResource(R.drawable.red_point)
            }



            // 클릭 리스너는 주요 정류장만
            if (isMainStop) {
                marker.setOnClickListener {
                    val cameraUpdate = CameraUpdate.scrollAndZoomTo(location, 17.0)
                        .animate(CameraAnimation.Easing)
                    naverMap.moveCamera(cameraUpdate)

                    Toast.makeText(context, "$title 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }

        // --- 실시간 이동 마커 초기화 ---
        busMarker = Marker().apply {
            // 초기엔 지도가 표시하는 곳(예: 사무소 정류장)으로 세팅
            position = LatLng(37.2242, 127.1876)
            icon = OverlayImage.fromResource(R.drawable.mjbusicon)
            map = naverMap
        }

        // 카메라는 움직이지 않고 마커만 따라가려면 주석 처리
        // naverMap.moveCamera(CameraUpdate.scrollTo(busMarker.position))

        // --- 폴링 시작 ---
        startPolling(naverMap)
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

                        // 카메라도 함께 이동하고 싶으면 아래 주석 해제
                        // naverMap.moveCamera(CameraUpdate.scrollTo(newPos))

                    } catch (e: Exception) {
                        Log.e("MapDebug", "폴링 중 에러", e)
                    }
                }
                handler.postDelayed(this, 5000)
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