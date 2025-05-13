package com.example.shuttlebusapplication.network

import com.example.shuttlebusapplication.model.LocationResponse
import retrofit2.http.GET

interface BusApiService {
    @GET("api/bus/latest")
    suspend fun getLatestLocation(): LocationResponse
}