package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.shuttlebusapplication.R
import com.naver.maps.map.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import android.widget.Toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
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