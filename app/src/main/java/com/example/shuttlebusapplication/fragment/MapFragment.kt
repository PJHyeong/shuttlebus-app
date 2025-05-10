package com.example.shuttlebusapplication.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.R
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var currentNaverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnMenu = view.findViewById<View>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        val btnMyLocation = view.findViewById<ImageButton>(R.id.btnMyLocation)
        btnMyLocation.setOnClickListener {
            moveToCurrentLocation()
        }

        mapView = view.findViewById(R.id.navermap_map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

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
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("MapDebug", "✅ onMapReady called!")
        currentNaverMap = naverMap

        val initialLocation = LatLng(37.2223, 127.1889)
        val cameraUpdate = CameraUpdate.scrollTo(initialLocation)
        naverMap.moveCamera(cameraUpdate)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

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
            val marker = Marker().apply {
                position = location
                captionText = title
                anchor = Marker.DEFAULT_ANCHOR
                icon = if (title.contains("지나가는길")) {
                    OverlayImage.fromResource(R.drawable.red_point)
                } else {
                    OverlayImage.fromResource(R.drawable.bus_marker)
                }
                map = naverMap
            }

            if (!title.contains("지나가는길")) {
                marker.setOnClickListener {
                    val update = CameraUpdate.scrollAndZoomTo(location, 17.0)
                        .animate(CameraAnimation.Easing)
                    naverMap.moveCamera(update)
                    Toast.makeText(context, "$title 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
    }

    private fun moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            Log.d("MapDebug", "❌ 위치 권한이 없음")
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdate.scrollTo(latLng)
                        .animate(CameraAnimation.Easing)
                    currentNaverMap.moveCamera(cameraUpdate)

                    Marker().apply {
                        position = latLng
                        icon = OverlayImage.fromResource(R.drawable.current_location)
                        width = 80
                        height = 80
                        map = currentNaverMap
                    }
                } else {
                    Toast.makeText(requireContext(), "요청한 위치도 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }, null)
    }

    override fun onStart() {
        super.onStart(); mapView.onStart()
    }

    override fun onResume() {
        super.onResume(); mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause(); super.onPause()
    }

    override fun onStop() {
        super.onStop(); mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView(); mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory(); mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
