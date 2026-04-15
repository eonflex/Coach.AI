package com.coachai.data.repository

import com.coachai.data.api.CoachAIApi
import com.coachai.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(private val api: CoachAIApi) {
    suspend fun getWeightLogs() = api.getWeightLogs()
    suspend fun getLatestWeight() = runCatching { api.getLatestWeight() }.getOrNull()
    suspend fun logWeight(req: LogWeightRequest) = api.logWeight(req)
    suspend fun deleteWeightLog(id: Int) = api.deleteWeightLog(id)
}
