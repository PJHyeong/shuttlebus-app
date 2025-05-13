package com.example.shuttlebusapplication.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MaplineApi {
    @GET("map-direction-15/v1/driving")
    suspend fun getDrivingPath(
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("waypoints") waypoints: String? = null
    ): MaplineResponse
}