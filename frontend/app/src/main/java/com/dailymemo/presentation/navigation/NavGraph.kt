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
import com.dailymemo.presentation.profile.ProfileEditScreen

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
            route = "${Screen.Memory.Create.route}?placeName={placeName}&address={address}&latitude={latitude}&longitude={longitude}&category={category}&isWishlist={isWishlist}",
            arguments = listOf(
                navArgument("placeName") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("address") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("latitude") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("longitude") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("isWishlist") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val placeName = backStackEntry.arguments?.getString("placeName")
            val address = backStackEntry.arguments?.getString("address")
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
            val categoryName = backStackEntry.arguments?.getString("category")
            val isWishlist = backStackEntry.arguments?.getBoolean("isWishlist") ?: false

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
                categoryName = categoryName,
                isWishlist = isWishlist
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

        // Profile screens
        composable(Screen.Profile.Edit.route) {
            ProfileEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Collaboration screens
        composable(Screen.Collaboration.route) {
            CollaborationScreen()
        }
    }
}
