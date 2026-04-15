package com.coachai.ui.screens.food

import androidx.compose.foundation.clickable
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
import com.coachai.data.model.FoodItemResponse
import com.coachai.ui.components.ErrorMessage
import com.coachai.ui.components.LoadingIndicator
import com.coachai.ui.components.MacroRow
import com.coachai.ui.components.SectionHeader

@Composable
fun FoodScreen(viewModel: FoodViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) { LoadingIndicator(); return }
    if (state.error != null) { ErrorMessage(state.error!!, onRetry = { viewModel.loadData() }); return }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setShowAddFoodDialog(true) }) {
                Icon(Icons.Default.Add, "Add food item")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            state.dailySummary?.let { summary ->
                item {
                    SectionHeader("Today's Summary")
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            MacroRow(summary.totalCalories, summary.totalProteinGrams,
                                summary.totalCarbsGrams, summary.totalFatGrams, summary.totalFiberGrams)
                        }
                    }
                }

                if (summary.entries.isNotEmpty()) {
                    item { SectionHeader("Today's Logs") }
                    items(summary.entries) { log ->
                        ListItem(
                            headlineContent = { Text(log.foodItemName) },
                            supportingContent = { Text("${log.calories.toInt()} cal · ${log.servingsConsumed}x ${log.meal ?: ""}") },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteFoodLog(log.id) }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }

            item { SectionHeader("Saved Foods") }
            item {
                var q by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = q, onValueChange = { q = it; viewModel.searchFoods(it) },
                    placeholder = { Text("Search foods...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true
                )
            }
            items(state.foodItems) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text("${item.calories.toInt()} cal / ${item.servingSizeGrams}g") },
                    trailingContent = {
                        TextButton(onClick = { viewModel.setShowLogFoodDialog(true, item) }) {
                            Text("Log")
                        }
                    },
                    modifier = Modifier.clickable { viewModel.setShowLogFoodDialog(true, item) }
                )
                HorizontalDivider()
            }
        }
    }

    if (state.showAddFoodDialog) {
        AddFoodItemDialog(onDismiss = { viewModel.setShowAddFoodDialog(false) }, onConfirm = { name, brand, serving, cal, prot, carb, fat, fiber ->
            viewModel.createFoodItem(name, brand, serving, cal, prot, carb, fat, fiber)
            viewModel.setShowAddFoodDialog(false)
        })
    }

    if (state.showLogFoodDialog && state.selectedFoodItem != null) {
        LogFoodDialog(
            item = state.selectedFoodItem!!,
            onDismiss = { viewModel.setShowLogFoodDialog(false) },
            onConfirm = { servings, meal -> viewModel.logFood(state.selectedFoodItem!!.id, servings, meal); viewModel.setShowLogFoodDialog(false) }
        )
    }
}

@Composable
fun AddFoodItemDialog(onDismiss: () -> Unit, onConfirm: (String, String?, Double, Double, Double, Double, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var serving by remember { mutableStateOf("100") }
    var cal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, singleLine = true)
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = serving, onValueChange = { serving = it }, label = { Text("Serving (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedTextField(value = cal, onValueChange = { cal = it }, label = { Text("Calories") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Protein (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Carbs (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fat (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedTextField(value = fiber, onValueChange = { fiber = it }, label = { Text("Fiber (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && cal.isNotBlank()) {
                        onConfirm(name, brand.takeIf { it.isNotBlank() }, serving.toDoubleOrNull() ?: 100.0,
                            cal.toDoubleOrNull() ?: 0.0, protein.toDoubleOrNull() ?: 0.0, carbs.toDoubleOrNull() ?: 0.0,
                            fat.toDoubleOrNull() ?: 0.0, fiber.toDoubleOrNull() ?: 0.0)
                    }
                },
                enabled = name.isNotBlank() && cal.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun LogFoodDialog(item: FoodItemResponse, onDismiss: () -> Unit, onConfirm: (Double, String?) -> Unit) {
    var servings by remember { mutableStateOf("1") }
    var meal by remember { mutableStateOf("") }
    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log ${item.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = servings, onValueChange = { servings = it }, label = { Text("Servings") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                Text("Meal:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    meals.forEach { m ->
                        FilterChip(selected = meal == m, onClick = { meal = if (meal == m) "" else m }, label = { Text(m) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(servings.toDoubleOrNull() ?: 1.0, meal.takeIf { it.isNotBlank() }) }) {
                Text("Log")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
