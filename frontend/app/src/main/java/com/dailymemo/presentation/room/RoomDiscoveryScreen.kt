package com.dailymemo.presentation.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailymemo.presentation.components.CategoryChipRow
import com.dailymemo.presentation.components.UserProfileModal
import com.dailymemo.presentation.components.getGradientForName
import com.dailymemo.presentation.components.getInitials
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDiscoveryScreen(
    viewModel: RoomDiscoveryViewModel = hiltViewModel(),
    onNavigateToRoomDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val hasMore by viewModel.hasMore.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    // User search state
    val userSearchQuery by viewModel.userSearchQuery.collectAsStateWithLifecycle()
    val userSearchState by viewModel.userSearchState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "방 탐색",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(
                    selectedTabIndex = when (currentTab) {
                        RoomTab.POPULAR -> 0
                        RoomTab.NEW -> 1
                        RoomTab.SEARCH -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = currentTab == RoomTab.POPULAR,
                        onClick = { viewModel.switchTab(RoomTab.POPULAR) },
                        text = { Text("인기방") }
                    )
                    Tab(
                        selected = currentTab == RoomTab.NEW,
                        onClick = { viewModel.switchTab(RoomTab.NEW) },
                        text = { Text("새로운 방") }
                    )
                    Tab(
                        selected = currentTab == RoomTab.SEARCH,
                        onClick = { viewModel.switchTab(RoomTab.SEARCH) },
                        text = { Text("검색") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            // Search tab shows both room and user search
            if (currentTab == RoomTab.SEARCH) {
                // Room search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 4.dp),
                    placeholder = { Text("방 이름, 설명, 태그 검색...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "방 검색")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "검색어 지우기")
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )

                // User ID search bar
                OutlinedTextField(
                    value = userSearchQuery,
                    onValueChange = { viewModel.onUserSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp, bottom = 8.dp),
                    placeholder = { Text("사용자 ID로 검색...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "사용자 검색")
                    },
                    trailingIcon = {
                        if (userSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onUserSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "검색어 지우기")
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )
            } else {
                // Search bar (disabled on non-SEARCH tabs)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("검색하려면 검색 탭으로 이동하세요") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "검색")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "검색어 지우기")
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true,
                    enabled = false
                )
            }

            // Content area - show user search results if user query is active, otherwise show rooms
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Show user search results when user search query is present
                if (currentTab == RoomTab.SEARCH && userSearchQuery.isNotEmpty()) {
                    UserSearchContent(
                        searchState = userSearchState,
                        onDismiss = { /* User profile modal dismissed */ }
                    )
                } else {
                    // Show room list
                    RoomListContent(
                        uiState = uiState,
                        currentTab = currentTab,
                        searchQuery = searchQuery,
                        hasMore = hasMore,
                        isLoadingMore = isLoadingMore,
                        onNavigateToRoomDetail = onNavigateToRoomDetail,
                        onLoadMore = { viewModel.loadMoreRooms() }
                    )
                }
            }
        }
    }
}

// Extracted room list content for better organization
@Composable
private fun RoomListContent(
    uiState: RoomDiscoveryUiState,
    currentTab: RoomTab,
    searchQuery: String,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onNavigateToRoomDetail: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is RoomDiscoveryUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) {
                        RoomCardSkeleton()
                    }
                }
            }
            is RoomDiscoveryUiState.Success -> {
                if (uiState.rooms.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (currentTab) {
                                RoomTab.SEARCH -> "🔍"
                                else -> "🏠"
                            },
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (currentTab) {
                                RoomTab.SEARCH -> if (searchQuery.isBlank()) {
                                    "검색어를 입력해주세요"
                                } else {
                                    "검색 결과가 없습니다"
                                }
                                RoomTab.POPULAR -> "아직 인기 있는 방이 없습니다"
                                RoomTab.NEW -> "아직 새로운 방이 없습니다"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentTab != RoomTab.SEARCH || searchQuery.isNotBlank()) {
                            Text(
                                text = "첫 번째 방을 만들어보세요!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val listState = rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.rooms) { room ->
                            RoomCard(
                                room = room,
                                onCardClick = { onNavigateToRoomDetail(room.id) }
                            )
                        }

                        // Loading indicator at bottom
                        if (hasMore && isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Infinite scroll logic
                    LaunchedEffect(listState, uiState.rooms.size, hasMore, isLoadingMore) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .distinctUntilChanged()
                            .collect { lastVisibleIndex ->
                                val threshold = uiState.rooms.size - 5
                                if (lastVisibleIndex != null &&
                                    lastVisibleIndex >= threshold &&
                                    hasMore &&
                                    !isLoadingMore) {
                                    onLoadMore()
                                }
                            }
                    }
                }
            }
            is RoomDiscoveryUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "새로고침하려면 탭을 다시 선택하세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// User search content composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserSearchContent(
    searchState: UserSearchState,
    onDismiss: () -> Unit
) {
    var showProfileModal by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf<com.dailymemo.domain.models.Profile?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (searchState) {
            is UserSearchState.Idle -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "👤",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "사용자 ID를 입력해주세요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "예: user123, foodie, cafe",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is UserSearchState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "검색 중...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is UserSearchState.Success -> {
                UserProfilePreviewCard(
                    profile = searchState.user,
                    onClick = {
                        selectedProfile = searchState.user
                        showProfileModal = true
                    }
                )
            }
            is UserSearchState.NotFound -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "😔",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "사용자를 찾을 수 없습니다",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "다른 ID로 검색해보세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is UserSearchState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = searchState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Show profile modal when user card is clicked
    if (showProfileModal && selectedProfile != null) {
        UserProfileModal(
            profile = selectedProfile!!,
            onDismiss = {
                showProfileModal = false
                onDismiss()
            },
            onJoinRoom = { roomId ->
                // TODO: Navigate to room detail or join room (task #63)
            }
        )
    }
}

// User profile preview card
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserProfilePreviewCard(
    profile: com.dailymemo.domain.models.Profile,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile image
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!profile.profileImageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = profile.profileImageUrl,
                        contentDescription = "${profile.nickname} 프로필",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors = getGradientForName(profile.nickname))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(profile.nickname),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 36.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nickname
            Text(
                text = profile.nickname,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User ID
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "@${profile.accountId}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Room stats if available
            if (profile.receivedLikesCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "좋아요 ${profile.receivedLikesCount}개",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tap to view text
            Text(
                text = "탭하여 프로필 보기",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCard(
    room: RoomCardData,
    onCardClick: () -> Unit
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Title and visibility icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (room.isPublic) Icons.Default.Public else Icons.Default.Lock,
                    contentDescription = if (room.isPublic) "공개방" else "비공개방",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Description
            if (room.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = room.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Categories
            if (room.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                CategoryChipRow(
                    categories = room.categories,
                    maxVisible = 3
                )
            }

            // Footer: participant count and owner
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Participant count
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "참여자 수",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${room.participantCount}명",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Owner
                Text(
                    text = "방장: ${room.ownerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RoomCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Title skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category chips skeleton
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}
