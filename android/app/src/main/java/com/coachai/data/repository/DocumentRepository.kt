package com.coachai.data.repository

import com.coachai.data.api.CoachAIApi
import com.coachai.data.model.DocumentResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(private val api: CoachAIApi) {
    suspend fun getDocuments(): List<DocumentResponse> = api.getDocuments()
    suspend fun uploadDocument(filePart: MultipartBody.Part, notes: RequestBody? = null) =
        api.uploadDocument(filePart, notes)
    suspend fun deleteDocument(id: Int) = api.deleteDocument(id)
}
