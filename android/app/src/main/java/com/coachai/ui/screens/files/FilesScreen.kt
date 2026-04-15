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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coachai.ui.components.ErrorMessage
import com.coachai.ui.components.LoadingIndicator
import com.coachai.ui.components.SectionHeader

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
            FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
                Icon(Icons.Default.Upload, "Upload document")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item { SectionHeader("Uploaded Documents") }

            if (state.isUploading) {
                item {
                    Row(modifier = Modifier.padding(16.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Uploading...")
                    }
                }
            }

            if (state.documents.isEmpty() && !state.isUploading) {
                item {
                    Text(
                        "No documents uploaded yet. Tap + to upload a PDF, Word doc, or image.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(state.documents) { doc ->
                ListItem(
                    headlineContent = { Text(doc.fileName) },
                    supportingContent = {
                        Text("${doc.contentType} · ${doc.fileSizeBytes / 1024} KB · ${doc.chunkCount} chunks · ${if (doc.extractionDone) "✓ Indexed" else "Processing..."}")
                    },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteDocument(doc.id) }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
