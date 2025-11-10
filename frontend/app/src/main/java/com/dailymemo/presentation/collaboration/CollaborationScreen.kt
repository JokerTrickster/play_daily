package com.dailymemo.presentation.collaboration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailymemo.domain.models.RoomMember
import com.dailymemo.domain.models.RoomPermission
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationScreen(
    viewModel: CollaborationViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val showKickDialog by viewModel.showKickDialog.collectAsState()
    val kickTargetMember by viewModel.kickTargetMember.collectAsState()

    val currentUserMember = members.firstOrNull { it.userId == currentUserId }
    val isOwner = currentUserMember?.permission == RoomPermission.OWNER

    // Permission Dialog
    if (showPermissionDialog && selectedMember != null) {
        PermissionDialog(
            member = selectedMember!!,
            onDismiss = { viewModel.hidePermissionDialog() },
            onConfirm = { newPermission ->
                viewModel.updateMemberPermission(selectedMember!!.userId, newPermission)
            }
        )
    }

    // Kick Confirmation Dialog
    if (showKickDialog && kickTargetMember != null) {
        KickConfirmationDialog(
            memberName = kickTargetMember!!.userName,
            onDismiss = { viewModel.hideKickDialog() },
            onConfirm = {
                viewModel.kickMember(kickTargetMember!!.userId)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "참여자 관리",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isOwner) "방장 권한" else "참여자",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadMembers() }) {
                                Text("다시 시도")
                            }
                        }
                    }
                }
                members.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "참여자가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    MembersList(
                        members = members,
                        currentUserId = currentUserId,
                        isOwner = isOwner,
                        onPermissionClick = { member ->
                            viewModel.showPermissionDialog(member)
                        },
                        onKickClick = { member ->
                            viewModel.showKickDialog(member)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MembersList(
    members: List<RoomMember>,
    currentUserId: Long,
    isOwner: Boolean,
    onPermissionClick: (RoomMember) -> Unit,
    onKickClick: (RoomMember) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "전체 참여자",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${members.size}명",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PermissionSummary(
                        icon = Icons.Filled.Star,
                        count = members.count { it.permission == RoomPermission.OWNER },
                        label = "방장",
                        color = MaterialTheme.colorScheme.primary
                    )
                    PermissionSummary(
                        icon = Icons.Filled.Edit,
                        count = members.count { it.permission == RoomPermission.READ_WRITE },
                        label = "편집",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    PermissionSummary(
                        icon = Icons.Filled.Visibility,
                        count = members.count { it.permission == RoomPermission.READ_ONLY },
                        label = "읽기",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isOwner) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "참여자를 클릭하여 권한을 변경하거나 추방할 수 있습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Members List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members, key = { it.userId }) { member ->
                MemberCard(
                    member = member,
                    isCurrentUser = member.userId == currentUserId,
                    isOwner = isOwner,
                    canManage = isOwner && member.permission != RoomPermission.OWNER && member.userId != currentUserId,
                    onPermissionClick = { onPermissionClick(member) },
                    onKickClick = { onKickClick(member) }
                )
            }
        }
    }
}

@Composable
fun PermissionSummary(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = color
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCard(
    member: RoomMember,
    isCurrentUser: Boolean,
    isOwner: Boolean,
    canManage: Boolean,
    onPermissionClick: () -> Unit,
    onKickClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()) }
    val joinedDate = remember(member.joinedAt) {
        dateFormat.format(Date(member.joinedAt * 1000))
    }

    var showOptions by remember { mutableStateOf(false) }

    Card(
        onClick = {
            if (canManage) {
                showOptions = !showOptions
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            when (member.permission) {
                                RoomPermission.OWNER -> MaterialTheme.colorScheme.primary
                                RoomPermission.READ_WRITE -> MaterialTheme.colorScheme.secondary
                                RoomPermission.READ_ONLY -> MaterialTheme.colorScheme.tertiary
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.userName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = member.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "나",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PermissionBadge(permission = member.permission)

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = joinedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (canManage) {
                    IconButton(onClick = { showOptions = !showOptions }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "옵션",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Options Menu
            if (showOptions && canManage) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showOptions = false
                            onPermissionClick()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("권한 변경")
                    }

                    OutlinedButton(
                        onClick = {
                            showOptions = false
                            onKickClick()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Filled.PersonRemove,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("추방")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionBadge(permission: RoomPermission) {
    val (icon, text, color) = when (permission) {
        RoomPermission.OWNER -> Triple(
            Icons.Filled.Star,
            "방장",
            MaterialTheme.colorScheme.primary
        )
        RoomPermission.READ_WRITE -> Triple(
            Icons.Filled.Edit,
            "편집 가능",
            MaterialTheme.colorScheme.secondary
        )
        RoomPermission.READ_ONLY -> Triple(
            Icons.Filled.Visibility,
            "읽기 전용",
            MaterialTheme.colorScheme.tertiary
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PermissionDialog(
    member: RoomMember,
    onDismiss: () -> Unit,
    onConfirm: (RoomPermission) -> Unit
) {
    var selectedPermission by remember { mutableStateOf(member.permission) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "권한 변경",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = member.userName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PermissionOption(
                    permission = RoomPermission.READ_ONLY,
                    selected = selectedPermission == RoomPermission.READ_ONLY,
                    onSelect = { selectedPermission = RoomPermission.READ_ONLY }
                )
                PermissionOption(
                    permission = RoomPermission.READ_WRITE,
                    selected = selectedPermission == RoomPermission.READ_WRITE,
                    onSelect = { selectedPermission = RoomPermission.READ_WRITE }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedPermission)
                    onDismiss()
                },
                enabled = selectedPermission != member.permission
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionOption(
    permission: RoomPermission,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val (icon, title, description) = when (permission) {
        RoomPermission.OWNER -> Triple(
            Icons.Filled.Star,
            "방장",
            "모든 권한 (방장은 변경 불가)"
        )
        RoomPermission.READ_WRITE -> Triple(
            Icons.Filled.Edit,
            "편집 가능",
            "메모 조회, 작성, 수정, 삭제 가능"
        )
        RoomPermission.READ_ONLY -> Triple(
            Icons.Filled.Visibility,
            "읽기 전용",
            "메모 조회만 가능"
        )
    }

    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun KickConfirmationDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "참여자 추방",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "'$memberName' 님을 이 방에서 추방하시겠습니까?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "추방된 사용자는 이 방에 다시 참여할 수 없습니다.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("추방")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
