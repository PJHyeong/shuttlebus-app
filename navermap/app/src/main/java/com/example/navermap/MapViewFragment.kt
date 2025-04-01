package com.example.navermap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class MapViewFragment : Fragment(), OnMapReadyCallback {  // ✅ OnMapReadyCallback 구현
    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null  // NPE 방지

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)

        // 🔹 `getMapAsync(this)`를 호출하여 `onMapReady()`가 실행되도록 연결
        mapView.getMapAsync(this)
    }

    // ✅ `OnMapReadyCallback`의 `onMapReady()`를 정확하게 오버라이드
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap  // 네이버 맵 객체 저장

        // ✅ 대중교통 정보 켜기
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)

        // ✅ 야간 모드 켜기
        naverMap.isNightModeEnabled = true

    }

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
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}