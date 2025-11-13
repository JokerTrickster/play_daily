package com.dailymemo.presentation.memo

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dailymemo.presentation.components.CategorySelectionGrid
import com.dailymemo.presentation.memo.components.HalfStarRating
import java.io.File

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
    val imageUri by viewModel.imageUri.collectAsState()
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

    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // Create temp file for camera
    val tempImageFile = remember {
        File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
    }

    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }

    // Gallery picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onImageSelected(tempImageUri)
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempImageUri)
        }
    }

    // Handle save success
    LaunchedEffect(uiState) {
        android.util.Log.d("EditMemoScreen", "uiState 변경: $uiState")
        if (uiState is EditMemoUiState.Success) {
            android.util.Log.d("EditMemoScreen", "저장 성공! 화면 닫기 시작...")
            onSaveSuccess()
            android.util.Log.d("EditMemoScreen", "onSaveSuccess() 호출 완료")
        } else if (uiState is EditMemoUiState.Error) {
            android.util.Log.e("EditMemoScreen", "저장 실패: ${(uiState as EditMemoUiState.Error).message}")
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
                    // Add Image Button (only show when no image)
                    val displayImageUri = imageUri ?: existingImageUrl?.let { Uri.parse(it) }
                    if (displayImageUri == null && hasEditPermission) {
                        IconButton(
                            onClick = { showImagePickerDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "이미지 추가",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

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
                            placeholder = { Text("메모 내용을 입력하세요") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = hasEditPermission,
                            minLines = 4,
                            maxLines = 8
                        )

                        // Image Section - Only show when image exists
                        val displayImageUri = imageUri ?: existingImageUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

                        if (displayImageUri != null && displayImageUri.toString().isNotBlank()) {
                            // Show image with controls when image exists
                            Card(
                                modifier = Modifier.fillMaxWidth()
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
                                            text = "이미지",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (hasEditPermission) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Remove button
                                                FilledTonalButton(
                                                    onClick = {
                                                        viewModel.onImageSelected(null)
                                                        viewModel.clearExistingImage()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.filledTonalButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "이미지 제거",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("제거", style = MaterialTheme.typography.labelMedium)
                                                }

                                                // Change button
                                                Button(
                                                    onClick = { showImagePickerDialog = true },
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AddPhotoAlternate,
                                                        contentDescription = "이미지 변경",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("변경", style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = displayImageUri,
                                            contentDescription = "메모 이미지",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }

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

        // Image Picker Dialog
        if (showImagePickerDialog) {
            ImagePickerDialog(
                onDismiss = { showImagePickerDialog = false },
                onGalleryClick = {
                    showImagePickerDialog = false
                    imagePickerLauncher.launch("image/*")
                },
                onCameraClick = {
                    showImagePickerDialog = false
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) -> {
                            cameraLauncher.launch(tempImageUri)
                        }
                        else -> {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "이미지 선택",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gallery Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGalleryClick() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "갤러리",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "갤러리에서 선택",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Camera Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCameraClick() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "카메라",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "카메라로 촬영",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
