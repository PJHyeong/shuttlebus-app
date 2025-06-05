

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.PATCH
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
import com.example.shuttlebusapplication.model.CommentRequest
import com.example.shuttlebusapplication.model.CommentResponse
import com.example.shuttlebusapplication.model.DeleteCommentRequest
import com.example.shuttlebusapplication.model.UpdateCommentRequest

interface ApiService {

    // ──────────────── 버스, 경로 관련 ────────────────

    @GET("bus/latest")
    suspend fun getLatestLocation(): LocationResponse

    @GET("map-direction-15/v1/driving")
    suspend fun getRoute(
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("waypoints") waypoints: String?
    ): Response<DirectionResponse>

    // ──────────────── 로그인 및 회원가입 ────────────────

    @POST("auth/register")
    fun registerUser(@Body userRequest: UserRequest): Call<UserResponse>

    @POST("auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // ──────────────── 공지사항 ────────────────

    @GET("announcements")
    fun getAnnouncements(): Call<List<Announcement>>

    @DELETE("announcements/{id}")
    fun deleteAnnouncement(@Path("id") announcementId: String): Call<Void>

    @GET("notices")
    fun getNotices(): Call<List<NoticeItem>>

    @POST("notices")
    fun createNotice(@Header("Authorization") token: String, @Body req: NoticeRequest): Call<NoticeItem>

    @PUT("notices/{id}")
    fun updateNotice(@Header("Authorization") token: String, @Path("id") id: String, @Body req: NoticeRequest): Call<NoticeItem>

    @DELETE("notices/{id}")
    fun deleteNotice(@Header("Authorization") token: String, @Path("id") id: String): Call<Void>

    // ──────────────── 댓글 관련 ────────────────

    @POST("comments")
    fun addComment(@Body commentRequest: CommentRequest): Call<CommentResponse>

    @GET("comments")
    fun getComments(@Query("announcementId") announcementId: String): Call<List<CommentResponse>>

    @DELETE("comments/{id}")
    fun deleteCommentWithBody(
        @Path("id") commentId: String,
        @Body deleteRequest: DeleteCommentRequest
    ): Call<Void>
    @PATCH("comments/{id}")
    fun updateComment(
        @Path("id") commentId: String,
        @Body updateRequest: UpdateCommentRequest
    ): Call<CommentResponse>
}
