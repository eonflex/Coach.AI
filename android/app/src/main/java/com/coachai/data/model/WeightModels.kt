package com.coachai.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LogWeightRequest(
    val weightKg: Double,
    val notes: String?,
    val loggedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class WeightLogResponse(
    val id: Int,
    val weightKg: Double,
    val notes: String?,
    val loggedAt: String
)

@JsonClass(generateAdapter = true)
data class PagedWeightResponse(
    val items: List<WeightLogResponse>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
