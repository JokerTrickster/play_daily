package com.dailymemo.presentation.memo

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dailymemo.domain.models.Comment
import com.dailymemo.presentation.memo.components.CommentsSection
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

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
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val isPinned by viewModel.isPinned.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val commentInput by viewModel.commentInput.collectAsState()
    val commentRating by viewModel.commentRating.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val businessPhone by viewModel.businessPhone.collectAsState()
    val businessAddress by viewModel.businessAddress.collectAsState()
    val naverPlaceUrl by viewModel.naverPlaceUrl.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Detect if scrolled past image
    val isScrolledPastHeader = scrollState.value > 200

    // Handle delete success
    LaunchedEffect(uiState) {
        if (uiState is MemoDetailUiState.Deleted) {
            onMemoDeleted()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("메모 삭제") },
            text = { Text("이 메모를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteMemo()
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            // Sticky header that appears on scroll
            AnimatedVisibility(
                visible = isScrolledPastHeader && !isEditing,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
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
            if (!isEditing) {
                // Floating action buttons group
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Edit button
                    FloatingActionButton(
                        onClick = {
                            val currentMemoId = viewModel.memoIdFlow.value
                            onNavigateToEdit(currentMemoId)
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "수정",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Delete button
                    FloatingActionButton(
                        onClick = { showDeleteDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Like button with animation
                    val scale = remember { Animatable(1f) }
                    var likeAnimationTrigger by remember { mutableStateOf(false) }

                    LaunchedEffect(isLiked) {
                        if (isLiked) {
                            likeAnimationTrigger = true
                            // Scale up and down animation
                            scale.animateTo(
                                targetValue = 1.3f,
                                animationSpec = tween(durationMillis = 100)
                            )
                            scale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = { viewModel.toggleLike() },
                        containerColor = if (isLiked)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Heart animation particles
                            if (likeAnimationTrigger && isLiked) {
                                repeat(6) { index ->
                                    val particleScale = remember { Animatable(0f) }
                                    val particleAlpha = remember { Animatable(1f) }
                                    val angle = (360f / 6) * index

                                    LaunchedEffect(isLiked) {
                                        particleScale.animateTo(
                                            targetValue = 2f,
                                            animationSpec = tween(durationMillis = 400)
                                        )
                                    }

                                    LaunchedEffect(isLiked) {
                                        particleAlpha.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 400)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Filled.FavoriteBorder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = particleAlpha.value),
                                        modifier = Modifier
                                            .size((8 * particleScale.value).dp)
                                            .offset(
                                                x = (20 * particleScale.value * kotlin.math.cos(Math.toRadians(angle.toDouble()))).dp,
                                                y = (20 * particleScale.value * kotlin.math.sin(Math.toRadians(angle.toDouble()))).dp
                                            )
                                    )
                                }
                            }

                            // Main heart icon
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isLiked) "좋아요 취소" else "좋아요",
                                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale.value
                                    scaleY = scale.value
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is MemoDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MemoDetailUiState.Error -> {
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
                            text = (uiState as MemoDetailUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateBack) {
                            Text("돌아가기")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        // Hero Image Section
                        if (!imageUrl.isNullOrBlank()) {
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

                                // Back button overlay on image
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

                        // Main Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Title Section
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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

                            // Categories
                            if (categories.isNotEmpty()) {
                                androidx.compose.foundation.layout.FlowRow(
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

                            // Location Card
                            if (!locationName.isNullOrBlank() || !businessName.isNullOrBlank()) {
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

                                        if (!locationName.isNullOrBlank()) {
                                            LocationInfoRow(
                                                icon = Icons.Outlined.LocationOn,
                                                text = locationName!!
                                            )
                                        }

                                        if (!businessName.isNullOrBlank()) {
                                            LocationInfoRow(
                                                icon = Icons.Outlined.Store,
                                                text = businessName!!
                                            )
                                        }

                                        if (!businessPhone.isNullOrBlank()) {
                                            LocationInfoRow(
                                                icon = Icons.Outlined.Phone,
                                                text = businessPhone!!
                                            )
                                        }

                                        if (!businessAddress.isNullOrBlank()) {
                                            LocationInfoRow(
                                                icon = Icons.Outlined.Home,
                                                text = businessAddress!!
                                            )
                                        }

                                        if (!naverPlaceUrl.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(naverPlaceUrl))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("MemoDetailScreen", "Failed to open URL: ${naverPlaceUrl}", e)
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
                                onCommentInputChange = viewModel::onCommentInputChange,
                                onCommentRatingChange = viewModel::onCommentRatingChange,
                                onPostComment = viewModel::postComment,
                                onDeleteComment = viewModel::deleteComment,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Bottom spacing for FAB
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
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
