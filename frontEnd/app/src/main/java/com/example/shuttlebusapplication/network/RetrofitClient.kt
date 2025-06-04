package com.example.shuttlebusapplication.network

import ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.6:5000/api/"
    // 테스트 환경이 바뀔떄마다 base url 변경 필요


    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }



    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    /// Naver Direction 15 Api 전용
    private const val DIRECTION_BASE_URL = "https://maps.apigw.ntruss.com/"
    private const val NCP_CLIENT_ID     = "69hhpxw8f1"
    private const val NCP_CLIENT_SECRET = "3f7luEnC4nyW9UqGylL8at6sA6ImhScDxgocQjP7"

    private val naverClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", NCP_CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY",    NCP_CLIENT_SECRET)
                .build()
            chain.proceed(req)
        }
        .addInterceptor(logging)
        .build()

    val directionService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(DIRECTION_BASE_URL)
            .client(naverClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}