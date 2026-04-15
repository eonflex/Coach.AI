package com.coachai.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateExerciseRequest(
    val name: String,
    val sets: Int?,
    val reps: Int?,
    val weightKg: Double?,
    val notes: String?
)

@JsonClass(generateAdapter = true)
data class CreateWorkoutLogRequest(
    val workoutType: String,
    val notes: String?,
    val cardioDurationMinutes: Int?,
    val loggedAt: String?,
    val exercises: List<CreateExerciseRequest>
)

@JsonClass(generateAdapter = true)
data class ExerciseResponse(
    val id: Int,
    val name: String,
    val sets: Int?,
    val reps: Int?,
    val weightKg: Double?,
    val notes: String?
)

@JsonClass(generateAdapter = true)
data class WorkoutLogResponse(
    val id: Int,
    val workoutType: String,
    val notes: String?,
    val cardioDurationMinutes: Int?,
    val loggedAt: String,
    val exercises: List<ExerciseResponse>
)

@JsonClass(generateAdapter = true)
data class PagedWorkoutResponse(
    val items: List<WorkoutLogResponse>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
