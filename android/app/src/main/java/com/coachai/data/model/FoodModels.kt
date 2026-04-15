package com.coachai.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FoodItemResponse(
    val id: Int,
    val name: String,
    val brand: String?,
    val servingSizeGrams: Double,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val isCustom: Boolean,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateFoodItemRequest(
    val name: String,
    val brand: String?,
    val servingSizeGrams: Double,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double
)

@JsonClass(generateAdapter = true)
data class LogFoodRequest(
    val foodItemId: Int,
    val servingsConsumed: Double,
    val meal: String?,
    val notes: String?,
    val loggedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class FoodLogResponse(
    val id: Int,
    val foodItemId: Int,
    val foodItemName: String,
    val servingsConsumed: Double,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val meal: String?,
    val notes: String?,
    val loggedAt: String
)

@JsonClass(generateAdapter = true)
data class DailySummaryResponse(
    val date: String,
    val totalCalories: Double,
    val totalProteinGrams: Double,
    val totalCarbsGrams: Double,
    val totalFatGrams: Double,
    val totalFiberGrams: Double,
    val entries: List<FoodLogResponse>
)
