package com.dailymemo.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dailymemo.presentation.auth.LoginScreen
import com.dailymemo.presentation.auth.SignupScreen

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
                    navController.navigate(Screen.Main.Map.route) {
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
                    navController.navigate(Screen.Main.Map.route) {
                        popUpTo(Screen.Auth.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Main screens
        composable(Screen.Main.Map.route) {
            // Redirect to List for now
            com.dailymemo.presentation.memo.MemoListScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.Memory.Create.route)
                },
                onNavigateToDetail = { memoId ->
                    navController.navigate("${Screen.Memory.Detail.route}/$memoId")
                }
            )
        }

        composable(Screen.Main.List.route) {
            com.dailymemo.presentation.memo.MemoListScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.Memory.Create.route)
                },
                onNavigateToDetail = { memoId ->
                    navController.navigate("${Screen.Memory.Detail.route}/$memoId")
                }
            )
        }

        composable(Screen.Main.Timeline.route) {
            // TimelineScreen placeholder
            Text("Timeline Screen - 타임라인 화면")
        }

        // Memory screens
        composable(Screen.Memory.Create.route) {
            // MemoryCreateScreen placeholder
            Text("Memory Create Screen - 메모리 생성")
        }

        composable("${Screen.Memory.Detail.route}/{memoryId}") { backStackEntry ->
            // MemoryDetailScreen placeholder
            Text("Memory Detail Screen - 메모리 상세")
        }

        // Collaboration screens
        composable(Screen.Collaboration.route) {
            // CollaborationScreen placeholder
            Text("Collaboration Screen - 협업 화면")
        }
    }
}
