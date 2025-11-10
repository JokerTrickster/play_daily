package com.dailymemo.presentation.memo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dailymemo.presentation.components.CategorySelectionGrid
import com.dailymemo.presentation.memo.components.HalfStarRating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemoScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditMemoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val existingImageUrl by viewModel.existingImageUrl.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val isPinned by viewModel.isPinned.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val businessPhone by viewModel.businessPhone.collectAsState()
    val businessAddress by viewModel.businessAddress.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()
    val hasEditPermission by viewModel.hasEditPermission.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }

    // Handle save success
    LaunchedEffect(uiState) {
        if (uiState is EditMemoUiState.Success) {
            onSaveSuccess()
        }
    }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("편집 취소") },
            text = { Text("변경사항이 저장되지 않습니다. 정말 취소하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("취소")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("계속 편집")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "메모 편집",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (viewModel.hasChanges()) {
                                showCancelDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    // Pin Toggle
                    IconButton(
                        onClick = { viewModel.togglePin() },
                        enabled = hasEditPermission
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "고정 해제" else "고정",
                            tint = if (isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
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
                .padding(paddingValues)
        ) {
            when (uiState) {
                is EditMemoUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is EditMemoUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as EditMemoUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("돌아가기")
                        }
                    }
                }
                is EditMemoUiState.Editing, is EditMemoUiState.Saving -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Existing Image
                        if (existingImageUrl != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                AsyncImage(
                                    model = existingImageUrl,
                                    contentDescription = "메모 이미지",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = viewModel::onTitleChange,
                            label = { Text("제목") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = hasEditPermission,
                            singleLine = true
                        )

                        // Content
                        OutlinedTextField(
                            value = content,
                            onValueChange = viewModel::onContentChange,
                            label = { Text("내용") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            enabled = hasEditPermission,
                            maxLines = 5
                        )

                        // Rating
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "평점",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                HalfStarRating(
                                    rating = rating,
                                    onRatingChange = { newRating ->
                                        if (hasEditPermission) {
                                            viewModel.onRatingChange(newRating)
                                        }
                                    }
                                )
                            }
                        }

                        // Categories
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "카테고리",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (selectedCategoryIds.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${selectedCategoryIds.size}개 선택됨",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                CategorySelectionGrid(
                                    categories = categories,
                                    selectedCategoryIds = selectedCategoryIds,
                                    onSelectionChange = { newSelection ->
                                        if (hasEditPermission) {
                                            viewModel.onCategorySelectionChange(newSelection)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Location Name
                        OutlinedTextField(
                            value = locationName,
                            onValueChange = viewModel::onLocationNameChange,
                            label = { Text("장소명") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = hasEditPermission
                        )

                        // Business Info
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "업소 정보",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = businessName,
                                    onValueChange = viewModel::onBusinessNameChange,
                                    label = { Text("업소명") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = hasEditPermission
                                )

                                OutlinedTextField(
                                    value = businessPhone,
                                    onValueChange = viewModel::onBusinessPhoneChange,
                                    label = { Text("전화번호") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = hasEditPermission
                                )

                                OutlinedTextField(
                                    value = businessAddress,
                                    onValueChange = viewModel::onBusinessAddressChange,
                                    label = { Text("주소") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = hasEditPermission
                                )
                            }
                        }

                        // Save Button
                        Button(
                            onClick = { viewModel.updateMemo() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = uiState !is EditMemoUiState.Saving &&
                                    hasEditPermission &&
                                    title.isNotBlank() &&
                                    selectedCategoryIds.isNotEmpty()
                        ) {
                            if (uiState is EditMemoUiState.Saving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("저장")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                is EditMemoUiState.Success -> {
                    // Navigation handled by LaunchedEffect
                }
            }
        }
    }
}
