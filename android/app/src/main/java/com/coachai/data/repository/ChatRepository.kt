package com.coachai.data.repository

import com.coachai.data.api.CoachAIApi
import com.coachai.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(private val api: CoachAIApi) {
    suspend fun getHistory(limit: Int = 50) = api.getChatHistory(limit)
    suspend fun sendMessage(req: ChatRequest) = api.sendMessage(req)
    suspend fun clearHistory() = api.clearChatHistory()
}
