package com.coachai.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coachai.data.model.CreateExerciseRequest
import com.coachai.ui.components.ErrorMessage
import com.coachai.ui.components.LoadingIndicator
import com.coachai.ui.components.SectionHeader

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) { LoadingIndicator(); return }
    if (state.error != null) { ErrorMessage(state.error!!, onRetry = { viewModel.loadData() }); return }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setShowLogDialog(true) }) {
                Icon(Icons.Default.Add, "Log workout")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item { SectionHeader("Recent Workouts") }
            if (state.workouts.isEmpty()) {
                item { Text("No workouts logged yet.", modifier = Modifier.padding(16.dp)) }
            }
            items(state.workouts) { workout ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(workout.workoutType, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { viewModel.deleteWorkout(workout.id) }) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                        Text(workout.loggedAt.take(10), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        if (workout.exercises.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            workout.exercises.forEach { ex ->
                                Text("  · ${ex.name} ${ex.sets?.let { s -> "${s}x" } ?: ""}${ex.reps?.toString() ?: ""} ${ex.weightKg?.let { w -> "@ ${w}kg" } ?: ""}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        workout.cardioDurationMinutes?.let { Text("Cardio: $it min", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }

    if (state.showLogDialog) {
        LogWorkoutDialog(
            onDismiss = { viewModel.setShowLogDialog(false) },
            onConfirm = { type, notes, cardio, exercises ->
                viewModel.logWorkout(type, notes, cardio, exercises)
                viewModel.setShowLogDialog(false)
            }
        )
    }
}

@Composable
fun LogWorkoutDialog(onDismiss: () -> Unit, onConfirm: (String, String?, Int?, List<CreateExerciseRequest>) -> Unit) {
    var workoutType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var cardioMinutes by remember { mutableStateOf("") }
    val exercises = remember { mutableStateListOf<CreateExerciseRequest>() }
    var exName by remember { mutableStateOf("") }
    var exSets by remember { mutableStateOf("") }
    var exReps by remember { mutableStateOf("") }
    var exWeight by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = workoutType, onValueChange = { workoutType = it }, label = { Text("Workout Type*") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, singleLine = true)
                OutlinedTextField(value = cardioMinutes, onValueChange = { cardioMinutes = it }, label = { Text("Cardio (min)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)

                Text("Exercises:", style = MaterialTheme.typography.labelMedium)
                exercises.forEachIndexed { idx, ex ->
                    Text("  ${idx + 1}. ${ex.name} ${ex.sets?.let { s -> "${s}x" } ?: ""}${ex.reps?.toString() ?: ""}", style = MaterialTheme.typography.bodySmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(value = exName, onValueChange = { exName = it }, label = { Text("Ex. Name") }, modifier = Modifier.weight(2f), singleLine = true)
                    OutlinedTextField(value = exSets, onValueChange = { exSets = it }, label = { Text("Sets") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    OutlinedTextField(value = exReps, onValueChange = { exReps = it }, label = { Text("Reps") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = exWeight, onValueChange = { exWeight = it }, label = { Text("Weight (kg)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (exName.isNotBlank()) {
                            exercises.add(CreateExerciseRequest(exName, exSets.toIntOrNull(), exReps.toIntOrNull(), exWeight.toDoubleOrNull(), null))
                            exName = ""; exSets = ""; exReps = ""; exWeight = ""
                        }
                    }) { Text("+ Add") }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (workoutType.isNotBlank()) onConfirm(workoutType, notes.takeIf { it.isNotBlank() }, cardioMinutes.toIntOrNull(), exercises.toList()) },
                enabled = workoutType.isNotBlank()
            ) { Text("Log") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
