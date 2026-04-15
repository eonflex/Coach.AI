package com.coachai.data.repository

import com.coachai.data.api.CoachAIApi
import com.coachai.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(private val api: CoachAIApi) {
    suspend fun getFoodItems(search: String? = null) = api.getFoodItems(search)
    suspend fun createFoodItem(req: CreateFoodItemRequest) = api.createFoodItem(req)
    suspend fun deleteFoodItem(id: Int) = api.deleteFoodItem(id)
    suspend fun getFoodLogs(date: String? = null) = api.getFoodLogs(date)
    suspend fun getDailySummary(date: String? = null) = api.getDailySummary(date)
    suspend fun logFood(req: LogFoodRequest) = api.logFood(req)
    suspend fun deleteFoodLog(id: Int) = api.deleteFoodLog(id)
}
