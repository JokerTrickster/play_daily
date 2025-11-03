package com.dailymemo.presentation.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dailymemo.R
import com.dailymemo.presentation.map.JoinRoomState
import com.dailymemo.presentation.utils.toUserMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoomDialog(
    joinRoomState: JoinRoomState,
    onJoinRoom: (roomId: Long, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var roomId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.join_room),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Room ID Input
                OutlinedTextField(
                    value = roomId,
                    onValueChange = { roomId = it },
                    label = { Text(stringResource(R.string.room_id)) },
                    placeholder = { Text(stringResource(R.string.room_id_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = joinRoomState !is JoinRoomState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { if (it.length <= 4) password = it },
                    label = { Text(stringResource(R.string.room_password)) },
                    placeholder = { Text(stringResource(R.string.room_password_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = joinRoomState !is JoinRoomState.Loading,
                    supportingText = {
                        Text("${password.length}/4")
                    }
                )

                // Error Message
                if (joinRoomState is JoinRoomState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = joinRoomState.error.toUserMessage(context),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = joinRoomState !is JoinRoomState.Loading
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    // Join Button
                    Button(
                        onClick = {
                            val roomIdLong = roomId.toLongOrNull()
                            if (roomIdLong != null && password.length == 4) {
                                onJoinRoom(roomIdLong, password)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = roomId.isNotBlank() &&
                                 password.length == 4 &&
                                 joinRoomState !is JoinRoomState.Loading
                    ) {
                        if (joinRoomState is JoinRoomState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.join_button))
                        }
                    }
                }
            }
        }
    }
}
