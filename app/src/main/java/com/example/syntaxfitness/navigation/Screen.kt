package com.example.syntaxfitness.navigation

sealed class Screen(val route: String) {
    object Running: Screen("running")
    object Settings: Screen("settings")
    object RunDetail : Screen("run_detail/{runId}") {
        fun createRoute(runId: Long) = "run_detail/$runId"
    }
}