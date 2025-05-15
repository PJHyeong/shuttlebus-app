import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.example.shuttlebusapplication.model.UserRequest
import com.example.shuttlebusapplication.model.UserResponse
import com.example.shuttlebusapplication.model.LoginRequest
import com.example.shuttlebusapplication.model.Announcement
import com.example.shuttlebusapplication.model.LoginResponse


interface ApiService {
    @POST("auth/register")
    fun registerUser(@Body userRequest: UserRequest): Call<UserResponse>

    @POST("auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("announcements")
    fun getAnnouncements(): Call<List<Announcement>>

    @DELETE("announcements/{id}")
    fun deleteAnnouncement(@Path("id") announcementId: String): Call<Void>
}