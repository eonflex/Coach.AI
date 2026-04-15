package com.coachai.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val message: String,
    val includeRecentLogs: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val id: Int,
    val reply: String,
    val contextSummary: String?,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class ChatHistoryItem(
    val id: Int,
    val role: String,
    val content: String,
    val createdAt: String
)
