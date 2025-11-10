package com.dailymemo.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Animation state for card scale on appearance
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (isCurrentUser) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = getPermissionColor(participant.permission).copy(alpha = 0.3f)
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Gradient border for owner
        if (participant.permission == com.dailymemo.domain.models.RoomPermission.OWNER) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFFA500),
                                Color(0xFFFFD700)
                            )
                        )
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Enhanced profile image with initials and gradient
                ProfileAvatar(
                    name = participant.name,
                    profileImageUrl = null, // Participant model doesn't have profileImageUrl yet
                    permission = participant.permission,
                    isCurrentUser = isCurrentUser
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = participant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        AnimatedVisibility(
                            visible = isCurrentUser,
                            enter = scaleIn(animationSpec = tween(300)) + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color.White
                                    )
                                    Text(
                                        text = "나",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    EnhancedPermissionBadge(permission = participant.permission)
                }
            }

            // Action buttons with enhanced design
            AnimatedVisibility(
                visible = isOwner && !isCurrentUser && participant.permission != com.dailymemo.domain.models.RoomPermission.OWNER,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Change permission button with filled tonal style
                    FilledTonalIconButton(
                        onClick = { showPermissionDialog = true },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "권한 변경",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Kick button with error color
                    FilledTonalIconButton(
                        onClick = { showKickDialog = true },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonRemove,
                            contentDescription = "내보내기",
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
fun EnhancedPermissionBadge(permission: com.dailymemo.domain.models.RoomPermission) {
    data class BadgeConfig(
        val icon: ImageVector,
        val text: String,
        val containerColor: Color,
        val contentColor: Color
    )

    val config = when (permission) {
        com.dailymemo.domain.models.RoomPermission.OWNER -> BadgeConfig(
            icon = Icons.Filled.Star,
            text = "방장",
            containerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
            contentColor = Color(0xFFFF8C00)
        )
        com.dailymemo.domain.models.RoomPermission.READ_WRITE -> BadgeConfig(
            icon = Icons.Filled.Edit,
            text = "편집 가능",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        com.dailymemo.domain.models.RoomPermission.READ_ONLY -> BadgeConfig(
            icon = Icons.Filled.RemoveRedEye,
            text = "읽기 전용",
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = config.containerColor,
        modifier = Modifier.shadow(1.dp, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = config.contentColor
            )
            Text(
                text = config.text,
                style = MaterialTheme.typography.labelMedium,
                color = config.contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProfileAvatar(
    name: String,
    profileImageUrl: String?,
    permission: com.dailymemo.domain.models.RoomPermission,
    isCurrentUser: Boolean
) {
    val gradientColors = getGradientForName(name)
    val initials = getInitials(name)

    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring for owner with golden gradient
        if (permission == com.dailymemo.domain.models.RoomPermission.OWNER) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFFA500),
                                Color(0xFFFFD700)
                            )
                        )
                    )
            )
        }

        // Profile image or gradient placeholder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (profileImageUrl.isNullOrEmpty()) {
                        Brush.linearGradient(colors = gradientColors)
                    } else {
                        Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .then(
                    if (isCurrentUser) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!profileImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "프로필 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        // Small crown icon for owner
        if (permission == com.dailymemo.domain.models.RoomPermission.OWNER) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700))
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "방장",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
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

// Helper function to get permission color
@Composable
fun getPermissionColor(permission: com.dailymemo.domain.models.RoomPermission): Color {
    return when (permission) {
        com.dailymemo.domain.models.RoomPermission.OWNER -> Color(0xFFFFD700)
        com.dailymemo.domain.models.RoomPermission.READ_WRITE -> MaterialTheme.colorScheme.tertiary
        com.dailymemo.domain.models.RoomPermission.READ_ONLY -> MaterialTheme.colorScheme.surfaceVariant
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
