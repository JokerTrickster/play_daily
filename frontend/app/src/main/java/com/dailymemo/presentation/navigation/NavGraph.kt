package com.dailymemo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Auth.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Auth.Login.route) {
            // LoginScreen placeholder
        }

        composable(Screen.Auth.Signup.route) {
            // SignupScreen placeholder
        }

        // Main screens
        composable(Screen.Main.Map.route) {
            // MapScreen placeholder
        }

        composable(Screen.Main.List.route) {
            // ListScreen placeholder
        }

        composable(Screen.Main.Timeline.route) {
            // TimelineScreen placeholder
        }

        // Memory screens
        composable(Screen.Memory.Create.route) {
            // MemoryCreateScreen placeholder
        }

        composable("${Screen.Memory.Detail.route}/{memoryId}") { backStackEntry ->
            // MemoryDetailScreen placeholder
        }

        // Collaboration screens
        composable(Screen.Collaboration.route) {
            // CollaborationScreen placeholder
        }
    }
}
