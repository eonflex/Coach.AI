package com.coachai.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocumentResponse(
    val id: Int,
    val fileName: String,
    val contentType: String,
    val fileSizeBytes: Long,
    val notes: String?,
    val extractionDone: Boolean,
    val chunkCount: Int,
    val uploadedAt: String
)
