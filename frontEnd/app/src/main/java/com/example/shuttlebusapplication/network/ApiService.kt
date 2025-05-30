import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.Response
import com.example.shuttlebusapplication.model.UserRequest
import com.example.shuttlebusapplication.model.UserResponse
import com.example.shuttlebusapplication.model.LoginRequest
import com.example.shuttlebusapplication.model.Announcement
import com.example.shuttlebusapplication.model.LoginResponse
import com.example.shuttlebusapplication.model.NoticeRequest
import com.example.shuttlebusapplication.model.LocationResponse
import com.example.shuttlebusapplication.model.NoticeItem
import com.example.shuttlebusapplication.model.DirectionResponse


interface ApiService {
    // 버스, Direction Api 관련

    @GET("bus/latest")
    suspend fun getLatestLocation(): LocationResponse

    @GET("map-direction-15/v1/driving")
    suspend fun getRoute(
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("waypoints") waypoints: String?
    ): Response<DirectionResponse>

    // 로그인, 회원가입 관련

    @POST("auth/register")
    fun registerUser(@Body userRequest: UserRequest): Call<UserResponse>

    @POST("auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("announcements")
    fun getAnnouncements(): Call<List<Announcement>>

    @DELETE("announcements/{id}")
    fun deleteAnnouncement(@Path("id") announcementId: String): Call<Void>

    //공지사항 관련

    @GET("notices")
    fun getNotices(): Call<List<NoticeItem>>

    @POST("notices")
    fun createNotice(
        @Header("Authorization") token: String,
        @Body req: NoticeRequest
    ): Call<NoticeItem>

    @PUT("notices/{id}")
    fun updateNotice(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body req: NoticeRequest
    ): Call<NoticeItem>

    @DELETE("notices/{id}")
    fun deleteNotice(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<Void>
}
