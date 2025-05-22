import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import com.example.shuttlebusapplication.model.UserRequest
import com.example.shuttlebusapplication.model.UserResponse
import com.example.shuttlebusapplication.model.LoginRequest
import com.example.shuttlebusapplication.model.Announcement
import com.example.shuttlebusapplication.model.LoginResponse
import com.example.shuttlebusapplication.model.NoticeItem
import com.example.shuttlebusapplication.model.NoticeRequest
import com.example.shuttlebusapplication.model.LocationResponse


interface ApiService {
    @GET("bus/latest")
    fun getLatestLocation(): LocationResponse

    @POST("auth/register")
    fun registerUser(@Body userRequest: UserRequest): Call<UserResponse>

    @POST("auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("announcements")
    fun getAnnouncements(): Call<List<Announcement>>

    @DELETE("announcements/{id}")
    fun deleteAnnouncement(@Path("id") announcementId: String): Call<Void>

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