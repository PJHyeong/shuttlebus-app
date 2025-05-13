package com.example.shuttlebusapplication.network

import com.example.shuttlebusapplication.network.BusApiService
import com.example.shuttlebusapplication.network.MaplineApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 로컬 테스트용 서버 URL
    private const val BASE_URL = "http://10.0.2.2:5000/"

    // 네이버 Directions API용 URL
    private const val NAVER_DIRECTIONS_BASE_URL =  "https://maps.apigw.ntruss.com/"

    // 공통 로깅 인터셉터 (BODY 레벨로 헤더/바디 모두 찍히도록)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 실시간 버스 위치 조회용 OkHttpClient
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // BusApiService
    val busApi: BusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BusApiService::class.java)
    }

    // Directions API 전용 OkHttpClient (인증 헤더 포함)
    private val naverClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", "69hhpxw8f1")
                .addHeader("X-NCP-APIGW-API-KEY",    "3f7luEnC4nyW9UqGylL8at6sA6ImhScDxgocQjP7")
                .build()
            chain.proceed(req)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // MaplineApi (네이버 Directions) 서비스
    val maplineApi: MaplineApi by lazy {
        Retrofit.Builder()
            .baseUrl(NAVER_DIRECTIONS_BASE_URL)
            .client(naverClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MaplineApi::class.java)
    }
}
