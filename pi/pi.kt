import okhttp3.*
import org.json.JSONArray
import java.io.IOException

fun fetchGpsData() {
    val client = OkHttpClient()

    // Node.js 서버의 IP와 포트를 자신의 서버 정보로 변경
    val request = Request.Builder()
        .url("http://<서버_IP주소>:3000/gps")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // 네트워크 요청 실패시 에러 처리
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { responseData ->
                try {
                    // 반환된 문자열을 JSONArray로 파싱 (데이터 구조에 따라 JSONObject나 모델 클래스로 파싱할 수 있음)
                    val jsonArray = JSONArray(responseData)
                    for (i in 0 until jsonArray.length()) {
                        val gpsObject = jsonArray.getJSONObject(i)
                        // 예시: "lat"와 "lng" 등의 필드에 접근하여 처리
                        val latitude = gpsObject.getDouble("lat")
                        val longitude = gpsObject.getDouble("lng")
                        println("GPS 데이터: lat = $latitude, lng = $longitude")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    })
}
