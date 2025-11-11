package com.dailymemo.presentation.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dailymemo.presentation.theme.DailyMemoTheme

/**
 * Preview and usage example for RoomPasswordResetDialog
 *
 * Usage in your screen:
 *
 * ```kotlin
 * var showResetDialog by remember { mutableStateOf(false) }
 * var newPassword by remember { mutableStateOf<String?>(null) }
 * var isResetting by remember { mutableStateOf(false) }
 *
 * RoomPasswordResetDialog(
 *     showDialog = showResetDialog,
 *     onDismiss = {
 *         showResetDialog = false
 *         newPassword = null
 *     },
 *     onConfirmReset = {
 *         // API call will happen in task #63
 *         // For now, simulate password reset:
 *         isResetting = true
 *         // viewModel.resetRoomPassword(roomId)
 *     },
 *     newPassword = newPassword,
 *     isLoading = isResetting
 * )
 * ```
 */

@Preview(showBackground = true, name = "Confirmation Step")
@Composable
fun RoomPasswordResetDialogConfirmationPreview() {
    DailyMemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RoomPasswordResetDialog(
                showDialog = true,
                onDismiss = {},
                onConfirmReset = {},
                newPassword = null,
                isLoading = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Confirmation Step - Loading")
@Composable
fun RoomPasswordResetDialogLoadingPreview() {
    DailyMemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RoomPasswordResetDialog(
                showDialog = true,
                onDismiss = {},
                onConfirmReset = {},
                newPassword = null,
                isLoading = true
            )
        }
    }
}

@Preview(showBackground = true, name = "Result Step - New Password")
@Composable
fun RoomPasswordResetDialogResultPreview() {
    DailyMemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RoomPasswordResetDialog(
                showDialog = true,
                onDismiss = {},
                onConfirmReset = {},
                newPassword = "1234",
                isLoading = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Interactive Example")
@Composable
fun RoomPasswordResetDialogInteractivePreview() {
    DailyMemoTheme {
        var showDialog by remember { mutableStateOf(false) }
        var newPassword by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = {
                    showDialog = true
                    newPassword = null
                    isLoading = false
                }) {
                    Text("Open Password Reset Dialog")
                }

                Button(onClick = {
                    showDialog = true
                    newPassword = "5678"
                    isLoading = false
                }) {
                    Text("Show Result (New Password)")
                }
            }

            RoomPasswordResetDialog(
                showDialog = showDialog,
                onDismiss = {
                    showDialog = false
                    newPassword = null
                    isLoading = false
                },
                onConfirmReset = {
                    isLoading = true
                    newPassword = "9876"
                    isLoading = false
                },
                newPassword = newPassword,
                isLoading = isLoading
            )
        }
    }
}
