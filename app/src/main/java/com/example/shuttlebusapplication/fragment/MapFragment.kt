package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.fragment.StationInfoBottomSheetFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import androidx.navigation.fragment.findNavController

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var busMarker: Marker
    private val handler = Handler(Looper.getMainLooper())
    private var currentIndex = 0
    private val pollingInterval = 3000L
    private lateinit var routeLine: PolylineOverlay

    private val busRouteCoordinates = listOf(
        LatLng(37.2195, 127.1836),
        LatLng(37.220252, 127.186613),
        LatLng(37.221042, 127.186816),
        LatLng(37.221343, 127.187862),
        LatLng(37.222043, 127.189143),
        LatLng(37.223287, 127.188026),
        LatLng(37.224392, 127.187806),
        LatLng(37.224212, 127.187576),
        LatLng(37.224392, 127.187806),
        LatLng(37.224973, 127.187808),
        LatLng(37.225849, 127.187947),
        LatLng(37.228031, 127.187671),
        LatLng(37.236138, 127.189164),
        LatLng(37.238266, 127.189874),
        LatLng(37.238731, 127.186211),
        LatLng(37.236948, 127.185178),
        LatLng(37.236138, 127.189164)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 메뉴 버튼 연결
        view.findViewById<View>(R.id.btnMenu)?.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        mapView = view.findViewById(R.id.navermap_map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        naverMap.moveCamera(CameraUpdate.scrollTo(busRouteCoordinates[0]))

        // 경로 라인
        routeLine = PolylineOverlay().apply {
            coords = busRouteCoordinates
            color = android.graphics.Color.BLUE
            width = 10
            map = naverMap
        }

        // 버스 마커
        busMarker = Marker().apply {
            position = busRouteCoordinates[0]
            icon = OverlayImage.fromResource(R.drawable.bus_icon)
            map = naverMap
        }

        // 정류장 마커 + 팝업
        val locations = listOf(
            LatLng(37.2242, 127.1876) to "버스관리사무소 정류장 (기점)",
            LatLng(37.2305, 127.1881) to "이마트 상공회의소 앞",
            LatLng(37.234, 127.1888) to "역북동행정복지센터 건너편",
            LatLng(37.234, 127.1886) to "역북동행정복지센터 앞",
            LatLng(37.2313, 127.1882) to "이마트 상공회의소 건너편",
            LatLng(37.2223, 127.1889) to "명진당",
            LatLng(37.2195, 127.1836) to "제3공학관"
        )

        locations.forEach { (location, title) ->
            val marker = Marker().apply {
                position = location
                captionText = title
                icon = OverlayImage.fromResource(R.drawable.bus_marker)
                map = naverMap
            }

            marker.setOnClickListener {
                naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(location, 17.0).animate(CameraAnimation.Easing))
                StationInfoBottomSheetFragment.newInstance(title)
                    .show(parentFragmentManager, "StationInfoBottomSheet")
                true
            }
        }

        startPolling()
    }

    private fun startPolling() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!::busMarker.isInitialized) return
                currentIndex = (currentIndex + 1) % busRouteCoordinates.size
                busMarker.position = busRouteCoordinates[currentIndex]
                handler.postDelayed(this, pollingInterval)
            }
        }, pollingInterval)
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onDestroyView() { super.onDestroyView(); mapView.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
