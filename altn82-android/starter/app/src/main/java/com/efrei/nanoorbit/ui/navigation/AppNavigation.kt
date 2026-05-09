package com.efrei.nanoorbit.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.efrei.nanoorbit.ui.dashboard.DashboardScreen
import com.efrei.nanoorbit.ui.detail.DetailScreen
import com.efrei.nanoorbit.ui.map.MapScreen
import com.efrei.nanoorbit.ui.planning.PlanningScreen
import com.efrei.nanoorbit.viewmodel.NanoOrbitViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: NanoOrbitViewModel = viewModel()

    val bottomBarRoutes = listOf(
        Routes.Dashboard.route,
        Routes.Planning.route,
        Routes.Map.route
    )

    val currentDestination = navController
        .currentBackStackEntryAsState().value?.destination

    val showBottomBar = bottomBarRoutes.any {
        currentDestination?.hierarchy?.any { dest -> dest.route == it } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Satellites") },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == Routes.Dashboard.route
                        } == true,
                        onClick = {
                            navController.navigate(Routes.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Schedule, contentDescription = "Planning") },
                        label = { Text("Planning") },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == Routes.Planning.route
                        } == true,
                        onClick = {
                            navController.navigate(Routes.Planning.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = "Carte") },
                        label = { Text("Carte") },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == Routes.Map.route
                        } == true,
                        onClick = {
                            navController.navigate(Routes.Map.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onSatelliteClick = { satelliteId ->
                        navController.navigate(
                            Routes.Detail.createRoute(satelliteId)
                        )
                    }
                )
            }
            composable(Routes.Detail.route) { backStackEntry ->
                val satelliteId = backStackEntry.arguments
                    ?.getString("satelliteId") ?: return@composable
                DetailScreen(
                    satelliteId = satelliteId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.Planning.route) {
                PlanningScreen(viewModel = viewModel)
            }
            composable(Routes.Map.route) {
                MapScreen()
            }
        }
    }
}