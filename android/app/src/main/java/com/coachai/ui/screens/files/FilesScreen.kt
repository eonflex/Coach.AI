package com.coachai.ui.screens.files

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coachai.ui.components.ErrorMessage
import com.coachai.ui.components.ExtractionStatusBadge
import com.coachai.ui.components.FileTypeIcon
import com.coachai.ui.components.LoadingIndicator
import com.coachai.ui.components.SectionHeader

/**
 * Files screen — upload documents and view their extraction status.
 *
 * File picker accepts any MIME type ("*\/*") so the user can upload
 * PDFs, images, Word docs, plain text, spreadsheets, etc.
 *
 * Each document shows:
 *   - File type icon (via [FileTypeIcon])
 *   - File name and size
 *   - Extraction status badge (via [ExtractionStatusBadge])
 *   - Chunk count once extraction is complete
 *
 * Backend endpoints:
 *   POST /api/documents  (multipart/form-data)
 *   GET  /api/documents
 *   DELETE /api/documents/{id}
 */
@Composable
fun FilesScreen(viewModel: FilesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadDocument(it, null) }
    }

    if (state.isLoading) { LoadingIndicator(); return }
    if (state.error != null) { ErrorMessage(state.error!!, onRetry = { viewModel.loadDocuments() }); return }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") },
                icon = { Icon(Icons.Default.Upload, null) },
                text = { Text("Upload") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item { SectionHeader("Uploaded Documents") }

            if (state.isUploading) {
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Uploading…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (state.documents.isEmpty() && !state.isUploading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No documents uploaded yet.\nTap Upload to add a PDF, Word doc, image, or text file.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            items(state.documents, key = { it.id }) { doc ->
                ListItem(
                    leadingContent = {
                        FileTypeIcon(
                            contentType = doc.contentType,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    headlineContent = { Text(doc.fileName) },
                    supportingContent = {
                        Column {
                            Text(
                                "${doc.fileSizeBytes / 1024} KB · ${doc.contentType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            ExtractionStatusBadge(
                                extractionDone = doc.extractionDone,
                                chunkCount = doc.chunkCount
                            )
                            doc.notes?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteDocument(doc.id) }) {
                            Icon(Icons.Default.Delete, "Delete document")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
