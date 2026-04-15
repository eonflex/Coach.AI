package com.coachai.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coachai.ui.screens.chat.ChatScreen
import com.coachai.ui.screens.files.FilesScreen
import com.coachai.ui.screens.food.FoodScreen
import com.coachai.ui.screens.settings.SettingsScreen
import com.coachai.ui.screens.weight.WeightScreen
import com.coachai.ui.screens.workout.WorkoutScreen

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val navItems = listOf(
        BottomNavItem(NavRoutes.FOOD, "Food", Icons.Default.Restaurant),
        BottomNavItem(NavRoutes.WORKOUT, "Workout", Icons.Default.FitnessCenter),
        BottomNavItem(NavRoutes.WEIGHT, "Weight", Icons.Default.Scale),
        BottomNavItem(NavRoutes.CHAT, "AI Chat", Icons.Default.Chat),
        BottomNavItem(NavRoutes.FILES, "Files", Icons.Default.Upload),
        BottomNavItem(NavRoutes.SETTINGS, "Settings", Icons.Default.Settings),
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.FOOD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.FOOD) { FoodScreen() }
            composable(NavRoutes.WORKOUT) { WorkoutScreen() }
            composable(NavRoutes.WEIGHT) { WeightScreen() }
            composable(NavRoutes.CHAT) { ChatScreen() }
            composable(NavRoutes.FILES) { FilesScreen() }
            composable(NavRoutes.SETTINGS) { SettingsScreen() }
        }
    }
}
