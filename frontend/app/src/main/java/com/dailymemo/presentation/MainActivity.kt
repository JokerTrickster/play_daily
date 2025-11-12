package com.dailymemo.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dailymemo.data.network.TokenRefreshInterceptor
import com.dailymemo.presentation.components.dialogs.SessionExpiredDialog
import com.dailymemo.presentation.navigation.NavGraph
import com.dailymemo.presentation.navigation.Screen
import com.dailymemo.presentation.theme.DailyMemoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), TokenRefreshInterceptor.SessionExpiredCallback {

    @Inject
    lateinit var tokenRefreshInterceptor: TokenRefreshInterceptor

    private var showSessionExpiredDialog by mutableStateOf(false)
    private var navigationHandler: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the session expired callback
        tokenRefreshInterceptor.setSessionExpiredCallback(this)

        setContent {
            val navController = rememberNavController()

            // Store navigation handler
            LaunchedEffect(navController) {
                navigationHandler = {
                    navController.navigate(Screen.Auth.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            DailyMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController = navController)

                    // Session Expired Dialog
                    SessionExpiredDialog(
                        showDialog = showSessionExpiredDialog,
                        onDismiss = { showSessionExpiredDialog = false },
                        onNavigateToLogin = {
                            showSessionExpiredDialog = false
                            navigationHandler?.invoke()
                        }
                    )
                }
            }
        }
    }

    /**
     * Callback from TokenRefreshInterceptor when session expires
     * Called from OkHttp thread, so we need to switch to main thread
     */
    override fun onSessionExpired() {
        Handler(Looper.getMainLooper()).post {
            showSessionExpiredDialog = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear callback to prevent memory leaks
        tokenRefreshInterceptor.setSessionExpiredCallback(null)
    }
}
