package com.example.fuelrecord.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fuelrecord.ui.screen.AddEditRecordScreen
import com.example.fuelrecord.ui.screen.RecordListScreen
import com.example.fuelrecord.ui.screen.StatisticsScreen
import com.example.fuelrecord.viewmodel.FuelRecordViewModel

@Composable
fun AppNavigation(viewModel: FuelRecordViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "records") {
        composable("records") {
            RecordListScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add") },
                onRecordClick = { recordId -> navController.navigate("edit/$recordId") },
                onStatsClick = { navController.navigate("statistics") }
            )
        }
        composable("add") {
            AddEditRecordScreen(
                viewModel = viewModel,
                recordId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId")
            AddEditRecordScreen(
                viewModel = viewModel,
                recordId = recordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("statistics") {
            StatisticsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
