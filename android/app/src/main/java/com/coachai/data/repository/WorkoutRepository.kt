package com.coachai.data.repository

import com.coachai.data.api.CoachAIApi
import com.coachai.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(private val api: CoachAIApi) {
    suspend fun getWorkoutLogs(date: String? = null, page: Int = 1) = api.getWorkoutLogs(date, page)
    suspend fun createWorkoutLog(req: CreateWorkoutLogRequest) = api.createWorkoutLog(req)
    suspend fun deleteWorkoutLog(id: Int) = api.deleteWorkoutLog(id)
}
