package com.example.shuttlebusapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.shuttlebusapplication.model.LoginRequest
import com.example.shuttlebusapplication.model.LoginResponse

interface AuthApi {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}