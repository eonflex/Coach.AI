package com.coachai.ui.screens.food.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coachai.ui.components.ErrorMessage
import com.coachai.ui.components.LoadingIndicator
import com.coachai.ui.components.MacroRow
import com.coachai.ui.components.SectionHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Meal history screen — browse food logs day by day.
 *
 * Displays the daily summary and individual log entries for a selected date.
 * The user can step backward/forward one day at a time using the arrow buttons.
 *
 * Backend: GET /api/food/summary?date=yyyy-MM-dd
 * TODO: add a proper date-picker dialog when Material3 DatePicker is stable.
 */
@Composable
fun MealHistoryScreen(
    viewModel: MealHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val displayFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Meal History") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Date navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.previousDay() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous day")
                }
                Text(
                    text = try {
                        LocalDate.parse(state.selectedDate).format(displayFormatter)
                    } catch (_: Exception) {
                        state.selectedDate
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = { viewModel.nextDay() },
                    enabled = state.selectedDate < LocalDate.now().toString()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next day")
                }
            }

            HorizontalDivider()

            when {
                state.isLoading -> LoadingIndicator()
                state.error != null -> ErrorMessage(state.error!!, onRetry = { viewModel.reload() })
                state.summary == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nothing logged for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    val summary = state.summary!!
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Daily Totals", style = MaterialTheme.typography.titleSmall)
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
                            item { SectionHeader("Logged Items") }
                            items(summary.entries, key = { it.id }) { entry ->
                                ListItem(
                                    headlineContent = { Text(entry.foodItemName) },
                                    supportingContent = {
                                        Text("${entry.calories.toInt()} cal · ${entry.servingsConsumed}× · ${entry.meal ?: "—"}")
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
