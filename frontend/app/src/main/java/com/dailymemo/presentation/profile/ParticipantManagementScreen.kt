package com.dailymemo.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentRoom by viewModel.currentRoom.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isOwner = viewModel.isOwner()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("참여자 관리") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        currentRoom?.let { room ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // Room info summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = room.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ID: ${room.id}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${room.participants.size}명",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "참여자 목록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(room.participants) { participant ->
                    ParticipantCard(
                        participant = participant,
                        isCurrentUser = participant.id == currentUserId,
                        isOwner = isOwner,
                        onKick = { viewModel.kickParticipant(participant.id) },
                        onPermissionChange = { permission ->
                            viewModel.updateMemberPermission(participant.id, permission)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ParticipantCard(
    participant: com.dailymemo.domain.models.Participant,
    isCurrentUser: Boolean,
    isOwner: Boolean,
    onKick: () -> Unit,
    onPermissionChange: (com.dailymemo.domain.models.RoomPermission) -> Unit
) {
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showKickDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "기본 프로필",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = participant.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        if (isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "나",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    PermissionBadge(permission = participant.permission)
                }
            }

            // Action buttons (only for owner and not current user)
            if (isOwner && !isCurrentUser && participant.permission != com.dailymemo.domain.models.RoomPermission.OWNER) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Change permission button
                    IconButton(
                        onClick = { showPermissionDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "권한 변경",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Kick button
                    IconButton(
                        onClick = { showKickDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonRemove,
                            contentDescription = "내보내기",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Permission change dialog
    if (showPermissionDialog) {
        PermissionChangeDialog(
            currentPermission = participant.permission,
            participantName = participant.name,
            onDismiss = { showPermissionDialog = false },
            onConfirm = { newPermission ->
                onPermissionChange(newPermission)
                showPermissionDialog = false
            }
        )
    }

    // Kick confirmation dialog
    if (showKickDialog) {
        AlertDialog(
            onDismissRequest = { showKickDialog = false },
            title = { Text("참여자 내보내기") },
            text = { Text("${participant.name}님을 방에서 내보내시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onKick()
                        showKickDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("내보내기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKickDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun PermissionBadge(permission: com.dailymemo.domain.models.RoomPermission) {
    val (text, color) = when (permission) {
        com.dailymemo.domain.models.RoomPermission.OWNER -> "방장" to MaterialTheme.colorScheme.primary
        com.dailymemo.domain.models.RoomPermission.READ_WRITE -> "편집 가능" to MaterialTheme.colorScheme.tertiary
        com.dailymemo.domain.models.RoomPermission.READ_ONLY -> "읽기 전용" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PermissionChangeDialog(
    currentPermission: com.dailymemo.domain.models.RoomPermission,
    participantName: String,
    onDismiss: () -> Unit,
    onConfirm: (com.dailymemo.domain.models.RoomPermission) -> Unit
) {
    var selectedPermission by remember { mutableStateOf(currentPermission) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("권한 변경") },
        text = {
            Column {
                Text("${participantName}님의 권한을 변경하시겠습니까?")
                Spacer(modifier = Modifier.height(16.dp))

                // Permission options
                com.dailymemo.domain.models.RoomPermission.values()
                    .filter { it != com.dailymemo.domain.models.RoomPermission.OWNER }
                    .forEach { permission ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPermission == permission,
                                onClick = { selectedPermission = permission }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (permission) {
                                        com.dailymemo.domain.models.RoomPermission.READ_WRITE -> "편집 가능"
                                        com.dailymemo.domain.models.RoomPermission.READ_ONLY -> "읽기 전용"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (permission) {
                                        com.dailymemo.domain.models.RoomPermission.READ_WRITE -> "메모를 생성하고 수정할 수 있습니다"
                                        com.dailymemo.domain.models.RoomPermission.READ_ONLY -> "메모를 볼 수만 있습니다"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedPermission) },
                enabled = selectedPermission != currentPermission
            ) {
                Text("변경")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
