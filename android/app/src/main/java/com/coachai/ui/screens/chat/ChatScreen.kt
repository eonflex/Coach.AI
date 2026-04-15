package com.coachai.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coachai.ui.components.ContextSummaryRow
import com.coachai.ui.components.LoadingIndicator

/**
 * AI Coach chat screen.
 *
 * Messages are rendered in distinct bubbles (user = primary, assistant = surface).
 * When the backend returns a non-null [ChatResponse.contextSummary], it is shown
 * beneath the assistant bubble so the user can see what data was used to ground
 * the response (uploaded docs, recent food logs, etc.).
 *
 * Backend endpoint: POST /api/chat  →  ChatResponse
 * History endpoint: GET /api/chat/history  →  List<ChatHistoryItem>
 *
 * TODO: contextSummary is shown as raw text for now.  In a follow-up, parse it
 *       to extract individual source names and render them as chips.
 */
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var message by remember { mutableStateOf("") }

    LaunchedEffect(state.history.size) {
        if (state.history.isNotEmpty()) listState.animateScrollToItem(state.history.size - 1)
    }

    if (state.isLoading) { LoadingIndicator(); return }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Coach") },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.Delete, "Clear history")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask your coach...") },
                    maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (message.isNotBlank() && !state.isSending) {
                            viewModel.sendMessage(message)
                            message = ""
                        }
                    },
                    enabled = message.isNotBlank() && !state.isSending
                ) {
                    if (state.isSending) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Icon(Icons.Default.Send, "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (state.history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ask your AI coach anything about your diet or fitness!\n\nResponses are grounded in your uploaded plans and recent logs.",
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            items(state.history) { msg ->
                val isUser = msg.role == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 300.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = msg.content,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    // Show context summary under assistant messages if available
                    if (!isUser) {
                        state.lastContextSummary?.let { summary ->
                            if (state.history.lastOrNull { it.role != "user" } == msg) {
                                ContextSummaryRow(
                                    contextSummary = summary,
                                    modifier = Modifier.widthIn(max = 300.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        state.error?.let { err ->
            Snackbar(modifier = Modifier.padding(padding)) { Text(err) }
        }
    }
}
