package com.dailymemo.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dailymemo.presentation.map.MapScreen
import com.dailymemo.presentation.memo.CreateMemoScreen
import com.dailymemo.presentation.memo.MemoDetailScreen
import com.dailymemo.presentation.memo.MemoListScreen
import com.dailymemo.presentation.profile.ProfileScreen
import com.dailymemo.presentation.profile.ParticipantManagementScreen

@Composable
fun MainScreen(
    mainNavController: NavHostController,
    startDestination: String = Screen.Main.Map.route
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            composable(Screen.Main.Map.route) {
                MapScreen(
                    onNavigateToCreateMemo = {
                        mainNavController.navigate(Screen.Memory.Create.route)
                    },
                    onNavigateToCreateMemoWithPlace = { placeName, address, latitude, longitude, categoryName, naverPlaceUrl ->
                        val encodedUrl = naverPlaceUrl?.let { android.net.Uri.encode(it) } ?: ""
                        mainNavController.navigate(
                            "${Screen.Memory.Create.route}?placeName=$placeName&address=$address&latitude=$latitude&longitude=$longitude&category=$categoryName&naverPlaceUrl=$encodedUrl"
                        )
                    },
                    onNavigateToDetail = { memoId ->
                        mainNavController.navigate("${Screen.Memory.Detail.route}/$memoId")
                    }
                )
            }

            composable(Screen.Main.List.route) {
                MemoListScreen(
                    onNavigateToCreate = {
                        mainNavController.navigate(Screen.Memory.Create.route)
                    },
                    onNavigateToCreateWishlist = {
                        mainNavController.navigate("${Screen.Memory.Create.route}?isWishlist=true")
                    },
                    onNavigateToDetail = { memoId ->
                        mainNavController.navigate("${Screen.Memory.Detail.route}/$memoId")
                    }
                )
            }

            composable(Screen.Main.Timeline.route) {
                ProfileScreen(
                    onLogout = {
                        // Navigate to login screen
                        mainNavController.navigate(Screen.Auth.Login.route) {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    onNavigateToEdit = {
                        mainNavController.navigate(Screen.Profile.Edit.route)
                    },
                    onNavigateToParticipants = {
                        bottomNavController.navigate(Screen.Profile.Participants.route)
                    }
                )
            }

            composable(Screen.Profile.Participants.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    bottomNavController.getBackStackEntry(Screen.Main.Timeline.route)
                }
                ParticipantManagementScreen(
                    onNavigateBack = {
                        bottomNavController.popBackStack()
                    },
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(parentEntry)
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Main.Map.route,
        icon = Icons.Filled.Map,
        label = "지도"
    ),
    BottomNavItem(
        route = Screen.Main.List.route,
        icon = Icons.Filled.List,
        label = "목록"
    ),
    BottomNavItem(
        route = Screen.Main.Timeline.route,
        icon = Icons.Filled.Person,
        label = "내 정보"
    )
)
