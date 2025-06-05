package com.example.syntaxfitness.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableOpenTarget
import androidx.compose.ui.Modifier
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.syntaxfitness.ui.running.screen.RunDetailScreen
import com.example.syntaxfitness.ui.running.screen.RunningScreen
import com.example.syntaxfitness.ui.settings.screen.SettingsScreen

@Composable
fun SyntaxFitnessNavigation(
    modifier: Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Running.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Running.route) {
            RunningScreen(
                modifier = Modifier,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                modifier = Modifier,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RunDetail.route,
            arguments = listOf(
                navArgument("runId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {backStackEntry ->
            val runId = backStackEntry.arguments?.getLong("runId") ?: 0L
            RunDetailScreen(
                runId = runId,
                modifier = Modifier,
                onNavigateBack = { navController.popBackStack() }
            )

        }
    }
}