package com.example.navermap

import android.util.Log
import com.naver.maps.geometry.LatLng
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


class TmapApiClient(private val apiKey: String) {

    fun getRouteData(startLat: Double, startLng: Double, endLat: Double, endLng: Double, callback: (List<LatLng>) -> Unit) {
        val urlString = "https://apis.openapi.sk.com/tmap/routes?version=1&format=json&startX=$startLng&startY=$startLat&endX=$endLng&endY=$endLat&apiKey=$apiKey"
        val url = URL(urlString)

        thread {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                Log.d("API Response", "Response Code: $responseCode")  // 응답 코드 출력

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = reader.readText()

                    Log.d("API Response", "Response: $response")  // 응답 내용 출력

                    // 응답 데이터를 파싱하여 좌표 리스트 반환
                    val routeCoordinates = parseRouteData(response)
                    callback(routeCoordinates)
                } else {
                    Log.e("API Error", "Failed to get route data: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // API 응답 데이터를 파싱하여 LatLng 리스트로 반환
    private fun parseRouteData(response: String): List<LatLng> {
        val jsonResponse = JSONObject(response)
        val coordinates = jsonResponse.getJSONArray("features")
            .getJSONObject(0)
            .getJSONObject("geometry")
            .getJSONArray("coordinates")

        val routeCoordinates = mutableListOf<LatLng>()
        for (i in 0 until coordinates.length()) {
            val coord = coordinates.getJSONArray(i)
            val lng = coord.getDouble(0)
            val lat = coord.getDouble(1)
            routeCoordinates.add(LatLng(lat, lng))
        }
        return routeCoordinates
    }
}

