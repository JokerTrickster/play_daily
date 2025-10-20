package com.dailymemo.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dailymemo.presentation.auth.LoginScreen
import com.dailymemo.presentation.auth.SignupScreen
import com.dailymemo.presentation.collaboration.CollaborationScreen
import com.dailymemo.presentation.map.MapScreen
import com.dailymemo.presentation.memo.CreateMemoScreen
import com.dailymemo.presentation.memo.MemoDetailScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "main" // Temporarily bypass login for testing
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Auth.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo(Screen.Auth.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Auth.Signup.route)
                }
            )
        }

        composable(Screen.Auth.Signup.route) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate("main") {
                        popUpTo(Screen.Auth.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Main screen with bottom navigation
        composable("main") {
            MainScreen(
                mainNavController = navController
            )
        }

        // Memory screens
        composable(
            route = "${Screen.Memory.Create.route}?placeName={placeName}&address={address}&latitude={latitude}&longitude={longitude}&category={category}",
            arguments = listOf(
                navArgument("placeName") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("address") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("latitude") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("longitude") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val placeName = backStackEntry.arguments?.getString("placeName")
            val address = backStackEntry.arguments?.getString("address")
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
            val categoryName = backStackEntry.arguments?.getString("category")

            CreateMemoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMemoCreated = {
                    navController.popBackStack()
                },
                placeName = placeName,
                address = address,
                latitude = latitude,
                longitude = longitude,
                categoryName = categoryName
            )
        }

        composable("${Screen.Memory.Detail.route}/{memoId}") {
            MemoDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMemoDeleted = {
                    navController.popBackStack("main", inclusive = false)
                }
            )
        }

        // Collaboration screens
        composable(Screen.Collaboration.route) {
            CollaborationScreen()
        }
    }
}
