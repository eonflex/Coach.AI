package com.coachai.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SetTargetRequest(
    val phase: String,
    val targetCalories: Double?,
    val targetProteinGrams: Double?,
    val targetCarbsGrams: Double?,
    val targetFatGrams: Double?,
    val targetFiberGrams: Double?,
    val targetWeightKg: Double?
)

@JsonClass(generateAdapter = true)
data class TargetResponse(
    val id: Int,
    val phase: String,
    val targetCalories: Double?,
    val targetProteinGrams: Double?,
    val targetCarbsGrams: Double?,
    val targetFatGrams: Double?,
    val targetFiberGrams: Double?,
    val targetWeightKg: Double?,
    val isActive: Boolean,
    val createdAt: String
)
