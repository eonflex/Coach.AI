package com.coachai.ui.screens.workout.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coachai.ui.components.ErrorMessage
import com.coachai.ui.components.LoadingIndicator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Workout history screen — paginated list of past workout logs.
 *
 * Each row is expandable to show exercises.
 *
 * Backend: GET /api/workouts?page=N
 * TODO: add date filter once API supports it.
 */
@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d yyyy") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Workout History") }) }
    ) { padding ->
        when {
            state.isLoading && state.workouts.isEmpty() -> LoadingIndicator()
            state.error != null && state.workouts.isEmpty() ->
                ErrorMessage(state.error!!, onRetry = { viewModel.loadNextPage() })
            state.workouts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No workouts logged yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.workouts, key = { it.id }) { workout ->
                        var expanded by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            workout.workoutType,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        val dateLabel = try {
                                            LocalDate.parse(workout.loggedAt.substring(0, 10))
                                                .format(dateFormatter)
                                        } catch (_: Exception) { workout.loggedAt }
                                        Text(
                                            dateLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (workout.cardioDurationMinutes != null) {
                                            Text(
                                                "Cardio: ${workout.cardioDurationMinutes}min",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    if (workout.exercises.isNotEmpty()) {
                                        IconButton(onClick = { expanded = !expanded }) {
                                            Icon(
                                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = if (expanded) "Collapse" else "Expand"
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteWorkout(workout.id) }) {
                                        Icon(Icons.Default.Delete, "Delete workout")
                                    }
                                }

                                if (expanded && workout.exercises.isNotEmpty()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    workout.exercises.forEach { ex ->
                                        val detail = buildString {
                                            append(ex.name)
                                            if (ex.sets != null && ex.reps != null) append(" · ${ex.sets}×${ex.reps}")
                                            ex.weightKg?.let { append(" @ ${it}kg") }
                                        }
                                        Text(
                                            detail,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                        )
                                    }
                                }

                                workout.notes?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Load more trigger
                    if (state.hasMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator()
                                } else {
                                    TextButton(onClick = { viewModel.loadNextPage() }) {
                                        Text("Load more")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
