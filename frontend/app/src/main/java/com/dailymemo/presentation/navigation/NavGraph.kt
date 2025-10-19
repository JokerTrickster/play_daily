package com.dailymemo.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dailymemo.presentation.auth.LoginScreen
import com.dailymemo.presentation.auth.SignupScreen
import com.dailymemo.presentation.collaboration.CollaborationScreen
import com.dailymemo.presentation.map.MapScreen
import com.dailymemo.presentation.memo.CreateMemoScreen
import com.dailymemo.presentation.memo.MemoDetailScreen

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
        composable(Screen.Memory.Create.route) {
            CreateMemoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMemoCreated = {
                    navController.popBackStack()
                }
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
