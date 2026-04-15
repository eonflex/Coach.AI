package com.coachai.ui.screens.weight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coachai.ui.components.ErrorMessage
import com.coachai.ui.components.LoadingIndicator
import com.coachai.ui.components.SectionHeader

@Composable
fun WeightScreen(viewModel: WeightViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) { LoadingIndicator(); return }
    if (state.error != null) { ErrorMessage(state.error!!, onRetry = { viewModel.loadData() }); return }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setShowLogDialog(true) }) {
                Icon(Icons.Default.Add, "Log weight")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            state.latestWeight?.let { latest ->
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Current Weight", style = MaterialTheme.typography.labelMedium)
                            Text("${latest.weightKg} kg", style = MaterialTheme.typography.headlineMedium)
                            Text(latest.loggedAt.take(10), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
            item { SectionHeader("History") }
            if (state.weights.isEmpty()) {
                item { Text("No weight logs yet.", modifier = Modifier.padding(16.dp)) }
            }
            items(state.weights) { log ->
                ListItem(
                    headlineContent = { Text("${log.weightKg} kg") },
                    supportingContent = { Text(log.loggedAt.take(10)) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteWeightLog(log.id) }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (state.showLogDialog) {
        LogWeightDialog(
            onDismiss = { viewModel.setShowLogDialog(false) },
            onConfirm = { kg, notes -> viewModel.logWeight(kg, notes); viewModel.setShowLogDialog(false) }
        )
    }
}

@Composable
fun LogWeightDialog(onDismiss: () -> Unit, onConfirm: (Double, String?) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Weight") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { weight.toDoubleOrNull()?.let { onConfirm(it, notes.takeIf { n -> n.isNotBlank() }) } }, enabled = weight.toDoubleOrNull() != null) {
                Text("Log")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
