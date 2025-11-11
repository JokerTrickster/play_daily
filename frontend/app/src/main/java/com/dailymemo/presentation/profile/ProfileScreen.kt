package com.dailymemo.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToEdit: () -> Unit = {},
    onNavigateToParticipants: () -> Unit = {},
    onNavigateToRoomDiscovery: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val memoCount by viewModel.memoCount.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val myRoomId by viewModel.myRoomId.collectAsState()
    val roomIdInput by viewModel.roomIdInput.collectAsState()
    val showJoinDialog by viewModel.showJoinDialog.collectAsState()
    val memosWithLocation by viewModel.memosWithLocation.collectAsState()
    val likedRooms by viewModel.likedRooms.collectAsState()
    val roomPassword by viewModel.roomPassword.collectAsState()
    val receivedLikesCount by viewModel.receivedLikesCount.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Reload room info when screen comes back to foreground
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshRoomInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Header - Shows MY room info only
            ProfileHeader(
                roomId = myRoomId?.toString() ?: "로딩 중...",
                roomPassword = roomPassword,
                memoCount = memoCount,
                receivedLikesCount = receivedLikesCount,
                profileImageUrl = profileImageUrl
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Korea Map with Memo Locations
            if (memosWithLocation.isNotEmpty()) {
                KoreaMapSection(
                    memos = memosWithLocation
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Room Info Section
            currentRoom?.let { room ->
                // Check if current room is MY room
                val isMyRoom = room.id == myRoomId?.toString()
                android.util.Log.d("ProfileScreen", "Room comparison - currentRoom.id: ${room.id}, myRoomId: $myRoomId, isMyRoom: $isMyRoom")

                RoomInfoSection(
                    room = room,
                    isMyRoom = isMyRoom,
                    isOwner = viewModel.isOwner(),
                    currentUserId = viewModel.currentUserId.collectAsState().value,
                    onJoinRoomClick = { viewModel.showJoinDialog() },
                    onLeaveRoomClick = { viewModel.leaveRoom() },
                    onKickParticipant = { viewModel.kickParticipant(it) },
                    onPermissionChange = { userId, permission ->
                        viewModel.updateMemberPermission(userId, permission)
                    },
                    onRoomPublicChange = { isPublic ->
                        viewModel.updateRoomPublic(isPublic)
                    },
                    onNavigateToParticipants = onNavigateToParticipants,
                    onNavigateToRoomDiscovery = onNavigateToRoomDiscovery
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Liked Rooms Section
            if (likedRooms.isNotEmpty()) {
                LikedRoomsSection(
                    likedRooms = likedRooms
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Menu Items
            MenuSection(
                onProfileEditClick = onNavigateToEdit,
                onLogoutClick = { showLogoutDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Join Room Dialog
    if (showJoinDialog) {
        val roomPasswordInput by viewModel.roomPasswordInput.collectAsState()
        val joinRoomError by viewModel.joinRoomError.collectAsState()

        AlertDialog(
            onDismissRequest = { viewModel.hideJoinDialog() },
            title = { Text("방 참여하기") },
            text = {
                Column {
                    Text("참여할 방의 정보를 입력하세요")
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = roomIdInput,
                        onValueChange = { viewModel.onRoomIdInputChange(it) },
                        label = { Text("방 ID") },
                        singleLine = true,
                        isError = joinRoomError != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = roomPasswordInput,
                        onValueChange = { viewModel.onRoomPasswordInputChange(it) },
                        label = { Text("방 비밀번호") },
                        singleLine = true,
                        isError = joinRoomError != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (joinRoomError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = joinRoomError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.joinRoom(roomIdInput, roomPasswordInput)
                    },
                    enabled = roomIdInput.isNotBlank() && roomPasswordInput.isNotBlank()
                ) {
                    Text("참여")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideJoinDialog() }) {
                    Text("취소")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("정말 로그아웃 하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("로그아웃")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun ProfileHeader(
    roomId: String,
    roomPassword: String,
    memoCount: Int,
    receivedLikesCount: Int,
    profileImageUrl: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(64.dp)
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
                if (!profileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "기본 프로필",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Room ID and Password Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "내 방 ID",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = roomId,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "방 비밀번호",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = roomPassword.ifEmpty { "로딩 중..." },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Memo Count Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = memoCount.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "메모",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Received Likes Count Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = receivedLikesCount.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "좋아요",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MenuSection(
    onProfileEditClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "설정",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                MenuItemCard(
                    icon = Icons.Outlined.AccountCircle,
                    title = "프로필 수정",
                    subtitle = "이름, 비밀번호, 프로필 이미지 변경",
                    onClick = onProfileEditClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                MenuItemCard(
                    icon = Icons.Outlined.Notifications,
                    title = "알림 설정",
                    subtitle = "푸시 알림 관리",
                    onClick = { /* TODO: 알림 설정 화면으로 이동 */ }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                MenuItemCard(
                    icon = Icons.Outlined.Info,
                    title = "앱 정보",
                    subtitle = "버전, 라이선스",
                    onClick = { /* TODO: 앱 정보 화면으로 이동 */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ExitToApp,
                contentDescription = "로그아웃",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "로그아웃",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MenuItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = "이동",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun RoomInfoSection(
    room: com.dailymemo.domain.models.Room,
    isMyRoom: Boolean,
    isOwner: Boolean,
    currentUserId: Long,
    onJoinRoomClick: () -> Unit,
    onLeaveRoomClick: () -> Unit,
    onKickParticipant: (Long) -> Unit,
    onPermissionChange: (Long, com.dailymemo.domain.models.RoomPermission) -> Unit = { _, _ -> },
    onRoomPublicChange: (Boolean) -> Unit = {},
    onNavigateToParticipants: () -> Unit = {},
    onNavigateToRoomDiscovery: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = "방",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "접속 중인 방",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isOwner) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = "방장",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Room Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Room Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "방 이름",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Room ID
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tag,
                            contentDescription = "방 ID",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ID: ${room.id}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Room Public/Private Toggle (Only for Owner)
                    if (isOwner) {
                        // Local state for immediate UI feedback
                        var localIsPublic by remember(room.isPublic) { mutableStateOf(room.isPublic) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (localIsPublic) Icons.Outlined.Public else Icons.Outlined.Lock,
                                    contentDescription = "방 공개 설정",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (localIsPublic) "공개 방" else "비공개 방",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = localIsPublic,
                                onCheckedChange = { newValue ->
                                    localIsPublic = newValue
                                    onRoomPublicChange(newValue)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Participants count with manage button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.People,
                                contentDescription = "참여자",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${room.participants.size}명 참여중",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Manage participants button
                        TextButton(
                            onClick = onNavigateToParticipants
                        ) {
                            Text("관리")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (isMyRoom) {
                // 내 방에 접속 중: 방 검색 + 다른 방 참여하기 버튼 표시
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 방 검색 버튼
                    Button(
                        onClick = onNavigateToRoomDiscovery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "방 검색",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "방 검색",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 다른 방 참여하기 버튼
                    OutlinedButton(
                        onClick = onJoinRoomClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "방 참여",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "다른 방 참여하기",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // 다른 방에 접속 중: 방 검색 + 방 나가기 + 다른 방 참여하기 버튼 표시
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 방 검색 버튼
                    Button(
                        onClick = onNavigateToRoomDiscovery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "방 검색",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "방 검색",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 방 나가기 버튼
                    OutlinedButton(
                        onClick = onLeaveRoomClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExitToApp,
                            contentDescription = "방 나가기",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "방 나가기",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 다른 방 참여하기 버튼
                    OutlinedButton(
                        onClick = onJoinRoomClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "방 참여",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "다른 방 참여하기",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantsList(
    participants: List<com.dailymemo.domain.models.Participant>,
    currentUserId: Long,
    isOwner: Boolean,
    onKickParticipant: (Long) -> Unit,
    onPermissionChange: (Long, com.dailymemo.domain.models.RoomPermission) -> Unit = { _, _ -> }
) {
    var showAllParticipants by remember { mutableStateOf(false) }
    val maxPreviewCount = 5
    val displayParticipants = if (participants.size > maxPreviewCount && !showAllParticipants) {
        participants.take(maxPreviewCount)
    } else {
        participants
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "참여자 목록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (participants.size > maxPreviewCount) {
                TextButton(
                    onClick = { showAllParticipants = !showAllParticipants }
                ) {
                    Text(
                        text = if (showAllParticipants) "접기" else "전체보기 (${participants.size}명)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Icon(
                        imageVector = if (showAllParticipants) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                displayParticipants.forEachIndexed { index, participant ->
                    CompactParticipantItem(
                        participant = participant,
                        currentUserId = currentUserId,
                        isOwner = isOwner,
                        onKick = { onKickParticipant(participant.id) },
                        onPermissionChange = { permission ->
                            onPermissionChange(participant.id, permission)
                        }
                    )
                    if (index < displayParticipants.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactParticipantItem(
    participant: com.dailymemo.domain.models.Participant,
    currentUserId: Long,
    isOwner: Boolean,
    onKick: () -> Unit,
    onPermissionChange: (com.dailymemo.domain.models.RoomPermission) -> Unit = {}
) {
    var showPermissionMenu by remember { mutableStateOf(false) }
    val isMe = participant.id == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // 프로필 아이콘 (작게)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (participant.isOwner) {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    )
                    .border(
                        width = if (isMe) 1.5.dp else 0.dp,
                        color = if (isMe) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (participant.isOwner) Icons.Filled.Star else Icons.Filled.Person,
                    contentDescription = "참여자",
                    modifier = Modifier.size(20.dp),
                    tint = if (participant.isOwner) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                // 이름 + "나" 표시
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = participant.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (participant.isOwner) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )

                    if (isMe) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "나",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 권한 Badge (작게)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = when (participant.permission) {
                            com.dailymemo.domain.models.RoomPermission.OWNER -> Icons.Filled.Star
                            com.dailymemo.domain.models.RoomPermission.READ_WRITE -> Icons.Filled.Edit
                            com.dailymemo.domain.models.RoomPermission.READ_ONLY -> Icons.Outlined.Visibility
                        },
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = when (participant.permission) {
                            com.dailymemo.domain.models.RoomPermission.OWNER ->
                                MaterialTheme.colorScheme.primary
                            com.dailymemo.domain.models.RoomPermission.READ_WRITE ->
                                MaterialTheme.colorScheme.tertiary
                            com.dailymemo.domain.models.RoomPermission.READ_ONLY ->
                                MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = when (participant.permission) {
                            com.dailymemo.domain.models.RoomPermission.OWNER -> "방장"
                            com.dailymemo.domain.models.RoomPermission.READ_WRITE -> "읽기/쓰기"
                            com.dailymemo.domain.models.RoomPermission.READ_ONLY -> "읽기 전용"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 방장만 볼 수 있는 컨트롤 (본인 제외, 다른 방장 제외)
        if (isOwner && !participant.isOwner && !isMe) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 권한 변경 버튼 (작게)
                Box {
                    IconButton(
                        onClick = { showPermissionMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "권한 변경",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = showPermissionMenu,
                        onDismissRequest = { showPermissionMenu = false }
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("읽기/쓰기") },
                            onClick = {
                                onPermissionChange(com.dailymemo.domain.models.RoomPermission.READ_WRITE)
                                showPermissionMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Edit, contentDescription = null)
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("읽기 전용") },
                            onClick = {
                                onPermissionChange(com.dailymemo.domain.models.RoomPermission.READ_ONLY)
                                showPermissionMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Visibility, contentDescription = null)
                            }
                        )
                    }
                }

                // 추방 버튼 (작게)
                IconButton(
                    onClick = onKick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RemoveCircle,
                        contentDescription = "추방",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipantItem(
    participant: com.dailymemo.domain.models.Participant,
    currentUserId: Long,
    isOwner: Boolean,
    onKick: () -> Unit,
    onPermissionChange: (com.dailymemo.domain.models.RoomPermission) -> Unit = {}
) {
    var showPermissionMenu by remember { mutableStateOf(false) }
    val isMe = participant.id == currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            } else if (participant.isOwner) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 프로필 아이콘
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (participant.isOwner) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        )
                        .border(
                            width = if (isMe) 2.dp else 0.dp,
                            color = if (isMe) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (participant.isOwner) Icons.Filled.Star else Icons.Filled.Person,
                        contentDescription = "참여자",
                        modifier = Modifier.size(26.dp),
                        tint = if (participant.isOwner) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    // 이름 + "나" 표시
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = participant.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (participant.isOwner) FontWeight.Bold else FontWeight.Medium
                        )

                        if (isMe) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(
                                    text = "나",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 권한 Badge
                    Badge(
                        containerColor = when (participant.permission) {
                            com.dailymemo.domain.models.RoomPermission.OWNER ->
                                MaterialTheme.colorScheme.primaryContainer
                            com.dailymemo.domain.models.RoomPermission.READ_WRITE ->
                                MaterialTheme.colorScheme.tertiaryContainer
                            com.dailymemo.domain.models.RoomPermission.READ_ONLY ->
                                MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = when (participant.permission) {
                            com.dailymemo.domain.models.RoomPermission.OWNER ->
                                MaterialTheme.colorScheme.onPrimaryContainer
                            com.dailymemo.domain.models.RoomPermission.READ_WRITE ->
                                MaterialTheme.colorScheme.onTertiaryContainer
                            com.dailymemo.domain.models.RoomPermission.READ_ONLY ->
                                MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = when (participant.permission) {
                                    com.dailymemo.domain.models.RoomPermission.OWNER -> Icons.Filled.Star
                                    com.dailymemo.domain.models.RoomPermission.READ_WRITE -> Icons.Filled.Edit
                                    com.dailymemo.domain.models.RoomPermission.READ_ONLY -> Icons.Outlined.Visibility
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = when (participant.permission) {
                                    com.dailymemo.domain.models.RoomPermission.OWNER -> "방장"
                                    com.dailymemo.domain.models.RoomPermission.READ_WRITE -> "읽기/쓰기"
                                    com.dailymemo.domain.models.RoomPermission.READ_ONLY -> "읽기 전용"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 방장만 볼 수 있는 컨트롤 (본인 제외, 다른 방장 제외)
            if (isOwner && !participant.isOwner && !isMe) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 권한 변경 버튼
                    Box {
                        IconButton(
                            onClick = { showPermissionMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "권한 변경",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = showPermissionMenu,
                            onDismissRequest = { showPermissionMenu = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("읽기/쓰기") },
                                onClick = {
                                    onPermissionChange(com.dailymemo.domain.models.RoomPermission.READ_WRITE)
                                    showPermissionMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("읽기 전용") },
                                onClick = {
                                    onPermissionChange(com.dailymemo.domain.models.RoomPermission.READ_ONLY)
                                    showPermissionMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Visibility, contentDescription = null)
                                }
                            )
                        }
                    }

                    // 추방 버튼
                    IconButton(
                        onClick = onKick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveCircle,
                            contentDescription = "추방",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun KoreaMapSection(memos: List<com.dailymemo.domain.models.Memo>) {
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 내가 다녀온 곳",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${memos.size}곳",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Kakao Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 0.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                var mapView by remember { mutableStateOf<MapView?>(null) }

                DisposableEffect(Unit) {
                    onDispose {
                        android.util.Log.d("ProfileMap", "Cleaning up MapView")
                        mapView?.finish()
                        kakaoMap = null
                    }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { event ->
                            // 모든 터치 이벤트를 MapView가 처리하도록 함
                            // 부모 스크롤로 이벤트 전파 차단
                            android.util.Log.d("ProfileMap", "Touch event intercepted: ${event.action}")
                            // requestDisallowInterceptTouchEvent를 호출하여 부모의 터치 가로채기 방지
                            mapView?.parent?.requestDisallowInterceptTouchEvent(true)
                            false  // false를 반환하여 MapView가 이벤트를 처리하도록 함
                        },
                    factory = { context ->
                        MapView(context).apply {
                            mapView = this

                            // 터치 이벤트 리스너 추가 - 부모 스크롤 차단
                            setOnTouchListener { view, event ->
                                // 부모 뷰에게 터치 이벤트를 가로채지 말라고 요청
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                android.util.Log.d("ProfileMap", "MapView touch: action=${event.action}")
                                false  // false를 반환하여 MapView가 이벤트를 계속 처리하도록 함
                            }

                            start(object : MapLifeCycleCallback() {
                                override fun onMapDestroy() {}
                                override fun onMapError(error: Exception) {
                                    android.util.Log.e("ProfileMap", "Map error: ${error.message}", error)
                                }
                            }, object : KakaoMapReadyCallback() {
                                override fun onMapReady(map: KakaoMap) {
                                    kakaoMap = map

                                    // 지도 제스처는 기본적으로 활성화되어 있음 (확대/축소/드래그)
                                    android.util.Log.d("ProfileMap", "Map ready with gestures enabled")

                                    // 한국 중심 좌표로 카메라 이동 (서울 중심)
                                    map.moveCamera(
                                        CameraUpdateFactory.newCenterPosition(
                                            LatLng.from(36.5, 127.5),
                                            6  // 전국이 보이는 줌 레벨
                                        )
                                    )

                                    // 메모 마커 추가
                                    val labelManager = map.labelManager
                                    val layer = labelManager?.layer

                                    memos.forEach { memo ->
                                        val lat = memo.latitude ?: return@forEach
                                        val lon = memo.longitude ?: return@forEach

                                        val position = LatLng.from(lat, lon)

                                        // 카테고리별 색상
                                        val color = when (memo.category) {
                                            com.dailymemo.domain.models.PlaceCategory.RESTAURANT -> android.graphics.Color.parseColor("#FF6B6B")
                                            com.dailymemo.domain.models.PlaceCategory.CAFE -> android.graphics.Color.parseColor("#FFB84D")
                                            com.dailymemo.domain.models.PlaceCategory.SHOPPING -> android.graphics.Color.parseColor("#AB47BC")
                                            com.dailymemo.domain.models.PlaceCategory.CULTURAL -> android.graphics.Color.parseColor("#42A5F5")
                                            com.dailymemo.domain.models.PlaceCategory.ENTERTAINMENT -> android.graphics.Color.parseColor("#EC407A")
                                            com.dailymemo.domain.models.PlaceCategory.ACCOMMODATION -> android.graphics.Color.parseColor("#26A69A")
                                            else -> android.graphics.Color.parseColor("#2196F3")
                                        }

                                        val styles = LabelStyles.from(
                                            LabelStyle.from(android.R.drawable.star_on)
                                                .setTextStyles(28, color, 2, android.graphics.Color.WHITE)
                                        )

                                        val options = LabelOptions.from(position)
                                            .setStyles(styles)
                                            .setTexts("${memo.category.icon}")

                                        layer?.addLabel(options)
                                    }
                                }
                            })
                        }
                    }
                )

                // 안내 텍스트 (메모가 없을 때)
                if (memos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "장소를 방문하고\n메모를 남겨보세요!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun LikedRoomsSection(
    likedRooms: List<com.dailymemo.domain.models.LikedRoom>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "좋아요한 방",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "좋아요한 방",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Badge(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = "${likedRooms.size}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Liked Rooms List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    likedRooms.forEachIndexed { index, likedRoom ->
                        LikedRoomItem(likedRoom = likedRoom)
                        if (index < likedRooms.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LikedRoomItem(
    likedRoom: com.dailymemo.domain.models.LikedRoom
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "방",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = likedRoom.roomName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "방 코드: ${likedRoom.roomCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Likes Count
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "좋아요 수",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${likedRoom.likesCount}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
