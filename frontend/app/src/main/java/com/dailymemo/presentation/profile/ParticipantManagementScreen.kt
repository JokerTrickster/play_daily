package com.dailymemo.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "참여자 ${room.participants.size}명",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
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
    val isParticipantOwner = participant.permission == com.dailymemo.domain.models.RoomPermission.OWNER

    // Determine profile size based on role
    val profileSize = if (isParticipantOwner) 80.dp else 64.dp
    val initialsSize = if (isParticipantOwner) 28.sp else 24.sp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile circle with image or initial
        Box(
            modifier = Modifier.size(profileSize),
            contentAlignment = Alignment.Center
        ) {
            if (!participant.profileImageUrl.isNullOrBlank()) {
                // Show profile image from URL
                coil.compose.AsyncImage(
                    model = participant.profileImageUrl,
                    contentDescription = "${participant.name} 프로필",
                    modifier = Modifier
                        .size(profileSize)
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                // Show gradient circle with initials
                Box(
                    modifier = Modifier
                        .size(profileSize)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = getGradientForName(participant.name))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getInitials(participant.name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = initialsSize
                    )
                }
            }

            // Owner badge - small golden circle with star on top-right
            if (isParticipantOwner) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "방장",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Edit icon on the top-left (only for owner viewing non-owner participants)
            if (isOwner && !isParticipantOwner && !isCurrentUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-6).dp, y = (-6).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { showPermissionDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "권한 편집",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Kick icon on top-right (only for owner viewing non-owner participants)
            if (isOwner && !isParticipantOwner && !isCurrentUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { showKickDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "추방",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Name
        Text(
            text = participant.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
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


// Helper function to generate consistent gradient colors based on name
@Composable
fun getGradientForName(name: String): List<Color> {
    val gradients = listOf(
        listOf(Color(0xFF667eea), Color(0xFF764ba2)), // Purple Blue
        listOf(Color(0xFFf093fb), Color(0xFFf5576c)), // Pink Red
        listOf(Color(0xFF4facfe), Color(0xFF00f2fe)), // Blue Cyan
        listOf(Color(0xFF43e97b), Color(0xFF38f9d7)), // Green Cyan
        listOf(Color(0xFFfa709a), Color(0xFFfee140)), // Pink Yellow
        listOf(Color(0xFF30cfd0), Color(0xFF330867)), // Cyan Purple
        listOf(Color(0xFFa8edea), Color(0xFFfed6e3)), // Cyan Pink
        listOf(Color(0xFFff9a9e), Color(0xFFfecfef)), // Coral Pink
        listOf(Color(0xFFffecd2), Color(0xFFfcb69f)), // Peach
        listOf(Color(0xFFff6e7f), Color(0xFFbfe9ff))  // Red Blue
    )

    val hash = name.hashCode()
    val index = Math.abs(hash % gradients.size)
    return gradients[index]
}

// Helper function to get initials from name
fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
        parts.isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "?"
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
