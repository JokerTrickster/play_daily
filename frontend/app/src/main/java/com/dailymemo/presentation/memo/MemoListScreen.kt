package com.dailymemo.presentation.memo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.dailymemo.domain.models.Memo
import com.dailymemo.presentation.components.MemoListSkeletonItem
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MemoListScreen(
    viewModel: MemoListViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit = {},
    onNavigateToCreateWishlist: () -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val minRating by viewModel.minRating.collectAsState()
    val showFilters by viewModel.showFilters.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reload memos when screen comes back to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadMemos()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "내 메모",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(
                    selectedTabIndex = if (currentTab == MemoTab.VISITED) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = currentTab == MemoTab.VISITED,
                        onClick = { viewModel.switchTab(MemoTab.VISITED) },
                        text = { Text("방문한 곳") }
                    )
                    Tab(
                        selected = currentTab == MemoTab.WISHLIST,
                        onClick = { viewModel.switchTab(MemoTab.WISHLIST) },
                        text = { Text("가고싶은 곳") }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentTab == MemoTab.WISHLIST) {
                        onNavigateToCreateWishlist()
                    } else {
                        onNavigateToCreate()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "새 메모 작성")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            // 검색바
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("제목, 내용, 장소명 검색...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "검색")
                },
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "검색어 지우기")
                            }
                        }
                        IconButton(onClick = { viewModel.toggleFilters() }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = "필터",
                                tint = if (selectedCategory != null || minRating > 0f) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
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

            // 필터 섹션
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "필터",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text("초기화")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 카테고리 필터
                        Text(
                            text = "카테고리",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val categories = listOf(
                            com.dailymemo.domain.models.PlaceCategory.RESTAURANT,
                            com.dailymemo.domain.models.PlaceCategory.CAFE,
                            com.dailymemo.domain.models.PlaceCategory.SHOPPING,
                            com.dailymemo.domain.models.PlaceCategory.CULTURAL,
                            com.dailymemo.domain.models.PlaceCategory.ENTERTAINMENT,
                            com.dailymemo.domain.models.PlaceCategory.ACCOMMODATION
                        )

                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { viewModel.onCategoryFilterChange(cat) },
                                    label = { Text("${cat.icon} ${cat.displayName}") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 평점 필터
                        Text(
                            text = "최소 평점: ${minRating.toInt()}⭐",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = minRating,
                            onValueChange = { viewModel.onRatingFilterChange(it) },
                            valueRange = 0f..5f,
                            steps = 4
                        )
                    }
                }
            }

            // 메모 목록
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (val state = uiState) {
                    is MemoListUiState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(5) {
                            MemoListSkeletonItem()
                        }
                    }
                }
                is MemoListUiState.Success -> {
                    if (state.memos.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📝",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "아직 메모가 없습니다",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "첫 메모를 작성해보세요!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.memos) { memo ->
                                MemoListItem(
                                    memo = memo,
                                    onItemClick = { onNavigateToDetail(memo.id) },
                                    onDeleteClick = { viewModel.deleteMemo(memo.id) }
                                )
                            }
                        }
                    }
                }
                is MemoListUiState.Error -> {
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
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadMemos() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoListItem(
    memo: Memo,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (memo.isPinned)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 헤더 (제목 + 핀/별점)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (memo.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "고정됨",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = memo.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 내용
            Text(
                text = memo.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 푸터 (날짜 + 별점)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = memo.createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (memo.rating > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "별점",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = memo.rating.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
