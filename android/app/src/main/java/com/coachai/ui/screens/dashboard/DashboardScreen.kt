package com.coachai.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coachai.ui.components.ErrorMessage
import com.coachai.ui.components.LoadingIndicator
import com.coachai.ui.components.MacroRow
import com.coachai.ui.components.SectionHeader

/**
 * Dashboard — today's summary at a glance.
 *
 * Shows:
 *   - Today's macro totals (calories / protein / carbs / fat / fiber)
 *   - Today's food log entries with quick-delete
 *   - Today's workouts
 *   - Quick-add FAB that opens the food-log screen
 *
 * Navigation:
 *   - FAB navigates to NavRoutes.FOOD (handled by parent NavHost via onAddFood callback)
 */
@Composable
fun DashboardScreen(
    onNavigateToFood: () -> Unit = {},
    onNavigateToWorkout: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) { LoadingIndicator(); return }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Today") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToFood) {
                Icon(Icons.Default.Add, contentDescription = "Log food")
            }
        }
    ) { padding ->
        if (state.error != null) {
            ErrorMessage(state.error!!, onRetry = { viewModel.refresh() })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Macro summary card
            state.dailySummary?.let { summary ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Macros", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            MacroRow(
                                summary.totalCalories,
                                summary.totalProteinGrams,
                                summary.totalCarbsGrams,
                                summary.totalFatGrams,
                                summary.totalFiberGrams
                            )
                        }
                    }
                }

                if (summary.entries.isNotEmpty()) {
                    item {
                        SectionHeader("Food Logs")
                    }
                    items(summary.entries, key = { it.id }) { entry ->
                        ListItem(
                            leadingContent = { Icon(Icons.Default.Restaurant, null) },
                            headlineContent = { Text(entry.foodItemName) },
                            supportingContent = {
                                Text("${entry.calories.toInt()} cal · ${entry.servingsConsumed}x${entry.meal?.let { " · $it" } ?: ""}")
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    }
                }
            }

            // Today's workouts
            if (state.todayWorkouts.isNotEmpty()) {
                item { SectionHeader("Workouts") }
                items(state.todayWorkouts, key = { it.id }) { workout ->
                    ListItem(
                        leadingContent = { Icon(Icons.Default.FitnessCenter, null) },
                        headlineContent = { Text(workout.workoutType) },
                        supportingContent = {
                            val detail = buildString {
                                if (workout.exercises.isNotEmpty()) append("${workout.exercises.size} exercise(s)")
                                workout.cardioDurationMinutes?.let { append(" · ${it}min cardio") }
                            }
                            Text(detail)
                        },
                        trailingContent = {
                            TextButton(onClick = onNavigateToWorkout) { Text("Details") }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }

            if (state.dailySummary == null && state.todayWorkouts.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            "Nothing logged today yet.\nTap + to log your first meal.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
