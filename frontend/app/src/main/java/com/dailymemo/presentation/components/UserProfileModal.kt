package com.dailymemo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Star
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
import com.dailymemo.domain.models.Participant
import com.dailymemo.domain.models.Profile
import com.dailymemo.domain.models.RoomPermission

/**
 * Context for displaying the UserProfileModal
 */
enum class ProfileModalContext {
    /**
     * Displayed from participant management screen - shows participant info only
     */
    PARTICIPANT_MANAGEMENT,

    /**
     * Displayed from room discovery - shows join button
     */
    ROOM_DISCOVERY
}

/**
 * State for join room action
 */
sealed class JoinRoomState {
    object Idle : JoinRoomState()
    object Loading : JoinRoomState()
    data class Success(val message: String = "방에 성공적으로 참여했습니다!") : JoinRoomState()
    data class Error(val message: String) : JoinRoomState()
}

/**
 * Reusable user profile modal that can be used in different contexts
 *
 * @param participant The participant to display
 * @param context The context from which this modal is displayed
 * @param onDismiss Callback when modal is dismissed
 * @param onJoinRoom Optional callback when join room button is clicked (only used in ROOM_DISCOVERY context)
 * @param isUserAlreadyInRoom Whether the current user is already in the room (only used in ROOM_DISCOVERY context)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileModal(
    participant: Participant,
    context: ProfileModalContext = ProfileModalContext.PARTICIPANT_MANAGEMENT,
    onDismiss: () -> Unit,
    onJoinRoom: ((String) -> Unit)? = null,
    isUserAlreadyInRoom: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    // Join room state management (only for discovery context)
    var joinRoomState by remember { mutableStateOf<JoinRoomState>(JoinRoomState.Idle) }

    // Auto-dismiss on success
    LaunchedEffect(joinRoomState) {
        if (joinRoomState is JoinRoomState.Success) {
            kotlinx.coroutines.delay(1500)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large profile image
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!participant.profileImageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = participant.profileImageUrl,
                        contentDescription = "${participant.name} 프로필",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors = getGradientForName(participant.name))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(participant.name),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 48.sp
                        )
                    }
                }

                // Owner badge if applicable
                if (participant.permission == RoomPermission.OWNER) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "방장",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // User name
            Text(
                text = participant.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Permission badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (participant.permission) {
                    RoomPermission.OWNER -> Color(0xFFFFD700).copy(alpha = 0.2f)
                    RoomPermission.READ_WRITE -> MaterialTheme.colorScheme.primaryContainer
                    RoomPermission.READ_ONLY -> MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = when (participant.permission) {
                        RoomPermission.OWNER -> "방장"
                        RoomPermission.READ_WRITE -> "편집 가능"
                        RoomPermission.READ_ONLY -> "읽기 전용"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = when (participant.permission) {
                        RoomPermission.OWNER -> Color(0xFF855A00)
                        RoomPermission.READ_WRITE -> MaterialTheme.colorScheme.onPrimaryContainer
                        RoomPermission.READ_ONLY -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bio section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "소개",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // TODO: Replace with actual bio field when available from backend
                    Text(
                        text = "아직 자기소개가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Join room button (only shown in ROOM_DISCOVERY context)
            if (context == ProfileModalContext.ROOM_DISCOVERY) {
                when (val state = joinRoomState) {
                    is JoinRoomState.Success -> {
                        // Success message
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✅ ${state.message}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    is JoinRoomState.Error -> {
                        // Error message
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { joinRoomState = JoinRoomState.Idle }
                                ) {
                                    Text("다시 시도")
                                }
                            }
                        }
                    }
                    else -> {
                        // Join button
                        Button(
                            onClick = {
                                if (onJoinRoom != null) {
                                    joinRoomState = JoinRoomState.Loading
                                    // TODO: Replace with actual room ID from participant's room
                                    // For now, using a placeholder - will be implemented in task #63
                                    onJoinRoom("placeholder-room-id")
                                    // Simulate API call for now
                                    joinRoomState = JoinRoomState.Success()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isUserAlreadyInRoom && state !is JoinRoomState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            if (state is JoinRoomState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.MeetingRoom,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isUserAlreadyInRoom) "이미 참여 중" else "방 참여하기",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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
        listOf(Color(0xFFa8edea), Color(0xFEfed6e3)), // Cyan Pink
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

/**
 * Overload for UserProfileModal that accepts Profile instead of Participant
 * Used in RoomDiscoveryScreen for user search results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileModal(
    profile: Profile,
    onDismiss: () -> Unit,
    onJoinRoom: ((String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    // Join room state management
    var joinRoomState by remember { mutableStateOf<JoinRoomState>(JoinRoomState.Idle) }

    // Auto-dismiss on success
    LaunchedEffect(joinRoomState) {
        if (joinRoomState is JoinRoomState.Success) {
            kotlinx.coroutines.delay(1500)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large profile image
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!profile.profileImageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = profile.profileImageUrl,
                        contentDescription = "${profile.nickname} 프로필",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors = getGradientForName(profile.nickname))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(profile.nickname),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 48.sp
                        )
                    }
                }
            }

            // User nickname
            Text(
                text = profile.nickname,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User ID badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "@${profile.accountId}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bio section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "소개",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // TODO: Replace with actual bio field when available from backend
                    Text(
                        text = "아직 자기소개가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        lineHeight = 22.sp
                    )
                }
            }

            // Show room stats if available
            if (profile.receivedLikesCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
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
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "받은 좋아요 ${profile.receivedLikesCount}개",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Join room button
            when (val state = joinRoomState) {
                is JoinRoomState.Success -> {
                    // Success message
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ ${state.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                is JoinRoomState.Error -> {
                    // Error message
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { joinRoomState = JoinRoomState.Idle }
                            ) {
                                Text("다시 시도")
                            }
                        }
                    }
                }
                else -> {
                    // Join button (only show if user has a default room)
                    Button(
                        onClick = {
                            if (onJoinRoom != null && profile.defaultRoomId != null) {
                                joinRoomState = JoinRoomState.Loading
                                // TODO: Replace with actual room ID from profile
                                onJoinRoom(profile.defaultRoomId.toString())
                                // Simulate API call for now
                                joinRoomState = JoinRoomState.Success()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = profile.defaultRoomId != null && state !is JoinRoomState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (state is JoinRoomState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.MeetingRoom,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (profile.defaultRoomId == null) "방이 없습니다" else "방 참여하기",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
