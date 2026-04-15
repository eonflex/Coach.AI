package com.coachai.data.api

import com.coachai.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface CoachAIApi {
    // Food items
    @GET("api/foods/items")
    suspend fun getFoodItems(@Query("search") search: String? = null): List<FoodItemResponse>

    @GET("api/foods/items/{id}")
    suspend fun getFoodItem(@Path("id") id: Int): FoodItemResponse

    @POST("api/foods/items")
    suspend fun createFoodItem(@Body request: CreateFoodItemRequest): FoodItemResponse

    @DELETE("api/foods/items/{id}")
    suspend fun deleteFoodItem(@Path("id") id: Int): Response<Unit>

    // Food logs
    @GET("api/foods/logs")
    suspend fun getFoodLogs(@Query("date") date: String? = null): List<FoodLogResponse>

    @GET("api/foods/logs/summary")
    suspend fun getDailySummary(@Query("date") date: String? = null): DailySummaryResponse

    @POST("api/foods/logs")
    suspend fun logFood(@Body request: LogFoodRequest): FoodLogResponse

    @DELETE("api/foods/logs/{id}")
    suspend fun deleteFoodLog(@Path("id") id: Int): Response<Unit>

    // Workouts
    @GET("api/workouts/")
    suspend fun getWorkoutLogs(
        @Query("date") date: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): PagedWorkoutResponse

    @GET("api/workouts/{id}")
    suspend fun getWorkoutLog(@Path("id") id: Int): WorkoutLogResponse

    @POST("api/workouts/")
    suspend fun createWorkoutLog(@Body request: CreateWorkoutLogRequest): WorkoutLogResponse

    @DELETE("api/workouts/{id}")
    suspend fun deleteWorkoutLog(@Path("id") id: Int): Response<Unit>

    // Weight
    @GET("api/weight/")
    suspend fun getWeightLogs(@Query("page") page: Int = 1, @Query("pageSize") pageSize: Int = 30): PagedWeightResponse

    @GET("api/weight/latest")
    suspend fun getLatestWeight(): WeightLogResponse

    @POST("api/weight/")
    suspend fun logWeight(@Body request: LogWeightRequest): WeightLogResponse

    @DELETE("api/weight/{id}")
    suspend fun deleteWeightLog(@Path("id") id: Int): Response<Unit>

    // Documents
    @GET("api/documents/")
    suspend fun getDocuments(): List<DocumentResponse>

    @Multipart
    @POST("api/documents/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("notes") notes: okhttp3.RequestBody? = null
    ): DocumentResponse

    @DELETE("api/documents/{id}")
    suspend fun deleteDocument(@Path("id") id: Int): Response<Unit>

    // Targets
    @GET("api/targets/active")
    suspend fun getActiveTarget(): TargetResponse

    @POST("api/targets/")
    suspend fun setTarget(@Body request: SetTargetRequest): TargetResponse

    // Chat
    @GET("api/chat/history")
    suspend fun getChatHistory(@Query("limit") limit: Int = 50): List<ChatHistoryItem>

    @POST("api/chat/")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @DELETE("api/chat/history")
    suspend fun clearChatHistory(): Response<Unit>

    // Health
    @GET("health")
    suspend fun healthCheck(): Response<Unit>
}
