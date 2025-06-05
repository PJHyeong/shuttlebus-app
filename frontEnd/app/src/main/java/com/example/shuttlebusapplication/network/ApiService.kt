package com.example.shuttlebusapplication.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import com.example.shuttlebusapplication.model.*

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

    // ──────────────── 공지사항(Announcements) ────────────────

    @GET("announcements")
    fun getAnnouncements(): Call<List<Announcement>>

    @DELETE("announcements/{id}")
    fun deleteAnnouncement(@Path("id") announcementId: String): Call<Void>

    // ──────────────── 공지사항(Notices) ────────────────

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

    // ──────────────── 댓글(Comment) 관련 ────────────────

    @POST("comments")
    fun addComment(@Body commentRequest: CommentRequest): Call<CommentResponse>

    @GET("comments")
    fun getComments(@Query("announcementId") announcementId: String): Call<List<CommentResponse>>

    @DELETE("comments/{id}")
    fun deleteComment(@Path("id") commentId: String): Call<Void> // ✅ 댓글 삭제 추가
}
