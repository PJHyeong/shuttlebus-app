package com.example.shuttlebusapplication.network

data class MaplineResponse(
    val route: Route
)

data class Route(
    val traoptimal: List<TraoptimalRoute>
)

data class TraoptimalRoute(
    val path: List<List<Double>>   // ["경도,위도", ...]
)