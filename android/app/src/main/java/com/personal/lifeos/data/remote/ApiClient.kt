package com.personal.lifeos.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface LifeOsApiService {
    @GET("api/v1/planning/daily-overview")
    suspend fun getDailyOverview(): DailyOverviewDto

    @GET("api/v1/planning/what-next")
    suspend fun getWhatNext(): RecommendedActionDto

    @POST("api/v1/planning/command")
    suspend fun sendCommand(@Body command: NaturalLanguageCommandDto): CommandResponseDto

    @POST("api/v1/food/parse")
    suspend fun parseFood(@Body query: Map<String, String>): FoodEstimateDto

    @POST("api/v1/health/steps/sync")
    suspend fun syncSteps(@Body steps: StepSyncDto)
}

object ApiClient {
    // 10.0.2.2 is Android Emulator localhost, replace with local IP for physical phone Wi-Fi
    var baseUrl: String = "http://10.0.2.2:8000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val apiService: LifeOsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LifeOsApiService::class.java)
    }
}
