package com.example.navermap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.naver.maps.map.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PolylineOverlay
import android.graphics.Color
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import com.example.navermap.TmapApiClient

class MapViewFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var tmapApiClient: TmapApiClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MapView 초기화
        mapView = view.findViewById(R.id.main)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // T맵 API 클라이언트 초기화
        tmapApiClient = TmapApiClient("sGYdTlTNVaKdL1r1C5Jy7pr9fz4v6n23bTbBf6vj")
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("MapDebug", "✅ onMapReady called!")

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

        locations.forEach { (location, title) ->
            val marker = Marker()
            marker.position = location
            marker.captionText = title
            marker.map = naverMap
        }

        // T맵 API 호출 및 경로 받아오기
        val startLat = 37.2223
        val startLng = 127.1889
        val endLat = 37.2195
        val endLng = 127.1836

        // T맵 API를 호출하여 경로를 받아옵니다.
        tmapApiClient.getRouteData(startLat, startLng, endLat, endLng) { routeCoordinates ->
            // UI 스레드에서 경로 그리기
            activity?.runOnUiThread {
                val polyline = PolylineOverlay()
                polyline.coords = routeCoordinates
                polyline.color = Color.RED  // 빨간색 선
                polyline.width = 10
                polyline.map = naverMap
            }
        }
    }

    // 생명주기 메서드들
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