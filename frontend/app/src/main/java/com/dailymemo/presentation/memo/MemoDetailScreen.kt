package com.dailymemo.presentation.memo

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dailymemo.presentation.memo.components.CommentsSection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MemoDetailScreen(
    onNavigateBack: () -> Unit,
    onMemoDeleted: () -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    viewModel: MemoDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val memoData by viewModel.memoData.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val commentInput by viewModel.commentInput.collectAsState()
    val commentRating by viewModel.commentRating.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val canEditMemo by viewModel.canEditMemo.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val isScrolledPastHeader = scrollState.value > 200

    // Handle navigation after deletion
    LaunchedEffect(uiState) {
        if (uiState is MemoDetailUiState.Deleted) {
            onMemoDeleted()
        }
    }

    // Refresh memo when screen appears (including when returning from edit)
    DisposableEffect(Unit) {
        android.util.Log.d("MemoDetailScreen", "화면 진입/재진입 - refresh 호출")
        viewModel.refresh()
        onDispose {
            android.util.Log.d("MemoDetailScreen", "화면 나감")
        }
    }

    DeleteConfirmationDialog(
        show = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            showDeleteDialog = false
            viewModel.deleteMemo()
        }
    )

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isScrolledPastHeader,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = memoData?.title ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로가기")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    modifier = Modifier.shadow(4.dp)
                )
            }
        },
        floatingActionButton = {
            ActionButtons(
                canEditMemo = canEditMemo,
                isLiked = isLiked,
                memoId = memoData?.memoId,
                onEdit = onNavigateToEdit,
                onDelete = { showDeleteDialog = true },
                onToggleLike = viewModel::toggleLike
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is MemoDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MemoDetailUiState.Error -> {
                    ErrorView(
                        message = (uiState as MemoDetailUiState.Error).message,
                        onBack = onNavigateBack
                    )
                }
                else -> {
                    memoData?.let { data ->
                        MemoDetailContent(
                            data = data,
                            comments = comments,
                            commentInput = commentInput,
                            commentRating = commentRating,
                            scrollState = scrollState,
                            isScrolledPastHeader = isScrolledPastHeader,
                            onNavigateBack = onNavigateBack,
                            onCommentInputChange = viewModel::onCommentInputChange,
                            onCommentRatingChange = viewModel::onCommentRatingChange,
                            onPostComment = viewModel::postComment,
                            onDeleteComment = viewModel::deleteComment
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoDetailContent(
    data: MemoDisplayData,
    comments: List<com.dailymemo.domain.models.Comment>,
    commentInput: String,
    commentRating: Int,
    scrollState: ScrollState,
    isScrolledPastHeader: Boolean,
    onNavigateBack: () -> Unit,
    onCommentInputChange: (String) -> Unit,
    onCommentRatingChange: (Int) -> Unit,
    onPostComment: () -> Unit,
    onDeleteComment: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Hero Image Section - only show if images exist
            if (data.imageUrls.isNotEmpty()) {
                HeroImageSection(
                    imageUrl = data.imageUrls.first(),
                    isPinned = data.isPinned,
                    isScrolledPastHeader = isScrolledPastHeader,
                    onNavigateBack = onNavigateBack
                )
            } else {
                // Add spacing when no image
                Spacer(modifier = Modifier.height(56.dp))
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Title and Rating
            TitleSection(
                title = data.title,
                rating = data.rating
            )

            // Categories
            if (data.categories.isNotEmpty()) {
                CategoriesSection(categories = data.categories)
            }

            // Content Section - THE MISSING PIECE!
            ContentSection(content = data.content)

            // Location Card
            if (data.locationName != null || data.businessName != null) {
                LocationCard(
                    locationName = data.locationName,
                    businessName = data.businessName,
                    businessPhone = data.businessPhone,
                    businessAddress = data.businessAddress,
                    naverPlaceUrl = data.naverPlaceUrl
                )
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Comments Section
            CommentsSection(
                comments = comments,
                commentInput = commentInput,
                commentRating = commentRating,
                onCommentInputChange = onCommentInputChange,
                onCommentRatingChange = onCommentRatingChange,
                onPostComment = onPostComment,
                onDeleteComment = onDeleteComment,
                modifier = Modifier.fillMaxWidth()
            )

            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

        // Back button overlay - always visible when not scrolled past header
        if (!isScrolledPastHeader) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun HeroImageSection(
    imageUrl: String,
    isPinned: Boolean,
    isScrolledPastHeader: Boolean,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "메모 이미지",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            contentScale = ContentScale.Fit
        )

        // Back button overlay
        if (!isScrolledPastHeader) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(16.dp)
                    .statusBarsPadding()
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
        }

        // Pin badge
        if (isPinned) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .offset(y = 56.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "고정됨",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "고정됨",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleSection(
    title: String,
    rating: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Rating stars
        if (rating > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (index < rating.toInt()) Color(0xFFFFB800) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = rating.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ContentSection(content: String) {
    if (content.isNotBlank()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesSection(categories: List<com.dailymemo.domain.models.MemoCategory>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val categoryColor = try {
                Color(android.graphics.Color.parseColor(category.color))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = categoryColor.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = category.name,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = categoryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    locationName: String?,
    businessName: String?,
    businessPhone: String?,
    businessAddress: String?,
    naverPlaceUrl: String?
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "위치 정보",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            locationName?.let {
                LocationInfoRow(icon = Icons.Outlined.LocationOn, text = it)
            }

            businessName?.let {
                LocationInfoRow(icon = Icons.Outlined.Store, text = it)
            }

            businessPhone?.let {
                LocationInfoRow(icon = Icons.Outlined.Phone, text = it)
            }

            businessAddress?.let {
                LocationInfoRow(icon = Icons.Outlined.Home, text = it)
            }

            naverPlaceUrl?.let { url ->
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("MemoDetailScreen", "Failed to open URL: $url", e)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("카카오맵에서 보기")
                }
            }
        }
    }
}

@Composable
private fun LocationInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtons(
    canEditMemo: Boolean,
    isLiked: Boolean,
    memoId: Long?,
    onEdit: (Long) -> Unit,
    onDelete: () -> Unit,
    onToggleLike: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Edit button - only if user has permission
        if (canEditMemo && memoId != null) {
            FloatingActionButton(
                onClick = { onEdit(memoId) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "수정",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Delete button - only if user has permission
        if (canEditMemo) {
            FloatingActionButton(
                onClick = onDelete,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Like button with simple animation
        LikeButton(isLiked = isLiked, onToggleLike = onToggleLike)
    }
}

@Composable
private fun LikeButton(
    isLiked: Boolean,
    onToggleLike: () -> Unit
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isLiked) {
        if (isLiked) {
            scale.animateTo(1.3f, animationSpec = tween(100))
            scale.animateTo(1f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ))
        }
    }

    FloatingActionButton(
        onClick = onToggleLike,
        containerColor = if (isLiked)
            MaterialTheme.colorScheme.errorContainer
        else
            MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isLiked) "좋아요 취소" else "좋아요",
            tint = if (isLiked)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("메모 삭제") },
            text = { Text("이 메모를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun ErrorView(
    message: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("돌아가기")
        }
    }
}
