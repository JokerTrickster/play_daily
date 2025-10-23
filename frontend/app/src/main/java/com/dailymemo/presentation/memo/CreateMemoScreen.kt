package com.dailymemo.presentation.memo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.presentation.components.BusinessInfoSection
import com.dailymemo.presentation.components.InterestLevelPicker
import com.dailymemo.presentation.components.PlaceSearchDialog
import com.dailymemo.presentation.memo.components.HalfStarRating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMemoScreen(
    onNavigateBack: () -> Unit,
    onMemoCreated: () -> Unit,
    placeName: String? = null,
    address: String? = null,
    latitude: Double? = null,
    longitude: Double? = null,
    categoryName: String? = null,
    isWishlist: Boolean = false,
    naverPlaceUrl: String? = null,
    viewModel: CreateMemoViewModel = hiltViewModel()
) {
    // Initialize place data if provided
    LaunchedEffect(placeName, address, latitude, longitude, categoryName, isWishlist, naverPlaceUrl) {
        // 카카오맵에서 검색한 장소 정보를 장소 검색 필드에 자동 입력
        if (placeName != null) {
            viewModel.onLocationNameChange(placeName)
        }
        if (address != null) {
            viewModel.onBusinessAddressChange(address)
        }
        if (latitude != null && longitude != null) {
            viewModel.setPlaceLocation(latitude, longitude)
        }
        if (categoryName != null) {
            PlaceCategory.values().find { it.name == categoryName }?.let {
                viewModel.onCategoryChange(it)
            }
        }
        if (naverPlaceUrl != null) {
            viewModel.onNaverPlaceUrlChange(naverPlaceUrl)
        }
        viewModel.setWishlistMode(isWishlist)
    }
    val uiState by viewModel.uiState.collectAsState()
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val isPinned by viewModel.isPinned.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val category by viewModel.category.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val businessPhone by viewModel.businessPhone.collectAsState()
    val businessAddress by viewModel.businessAddress.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val showSearchDialog by viewModel.showSearchDialog.collectAsState()

    val scrollState = rememberScrollState()

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageUriChange(uri)
    }

    LaunchedEffect(uiState) {
        if (uiState is CreateMemoUiState.Success) {
            onMemoCreated()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        "새 메모",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    // Pin Toggle
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "고정 해제" else "고정",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("제목") },
                    placeholder = { Text("메모 제목을 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = viewModel::onContentChange,
                    label = { Text("내용") },
                    placeholder = { Text("메모 내용을 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    minLines = 8,
                    maxLines = 15,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Image Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                text = "이미지 (선택)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "이미지 선택",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("선택")
                            }
                        }

                        // Image preview
                        imageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "선택된 이미지",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                // Remove image button
                                IconButton(
                                    onClick = {
                                        viewModel.onImageUriChange(null)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "이미지 제거",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Section - 주요 카테고리만 표시
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                text = "장소 카테고리",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (category != null && category != PlaceCategory.OTHER) {
                                Text(
                                    text = "${category?.icon} ${category?.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // 주요 카테고리만 표시 (자주 사용되는 8개)
                        val mainCategories = listOf(
                            PlaceCategory.RESTAURANT,
                            PlaceCategory.CAFE,
                            PlaceCategory.SHOPPING,
                            PlaceCategory.CULTURAL,
                            PlaceCategory.ENTERTAINMENT,
                            PlaceCategory.ACCOMMODATION,
                            PlaceCategory.SPORTS,
                            PlaceCategory.OTHER
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mainCategories.take(4).forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = {
                                        viewModel.onCategoryChange(if (category == cat) null else cat)
                                    },
                                    label = {
                                        Text("${cat.icon} ${cat.displayName}")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mainCategories.drop(4).forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = {
                                        viewModel.onCategoryChange(if (category == cat) null else cat)
                                    },
                                    label = {
                                        Text("${cat.icon} ${cat.displayName}")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Location Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                text = "위치",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentLocation != null) {
                                Text(
                                    text = "📍 ${currentLocation?.latitude?.let { "%.4f".format(it) }}, ${currentLocation?.longitude?.let { "%.4f".format(it) }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.getCurrentLocation() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("현재 위치")
                            }
                            Button(
                                onClick = { viewModel.openSearchDialog() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("장소 검색")
                            }
                        }
                        if (currentLocation != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = locationName,
                                onValueChange = viewModel::onLocationNameChange,
                                label = { Text("위치 이름") },
                                placeholder = { Text("예: 우리집, 카페") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Rating Section (normal mode) or Interest Level (wishlist mode)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    if (isWishlist) {
                        InterestLevelPicker(
                            currentLevel = rating,
                            onLevelChange = viewModel::onRatingChange,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "별점 (0.5 단위)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HalfStarRating(
                                rating = rating,
                                onRatingChange = viewModel::onRatingChange,
                                starSize = 36.dp,
                                showLabel = true
                            )
                        }
                    }
                }

                // Business Info Section (only for wishlist mode)
                if (isWishlist) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        BusinessInfoSection(
                            businessName = businessName,
                            businessPhone = businessPhone,
                            businessAddress = businessAddress,
                            onBusinessNameChange = viewModel::onBusinessNameChange,
                            onBusinessPhoneChange = viewModel::onBusinessPhoneChange,
                            onBusinessAddressChange = viewModel::onBusinessAddressChange,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Error Message
                if (uiState is CreateMemoUiState.Error) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (uiState as CreateMemoUiState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Save Button
                Button(
                    onClick = { viewModel.createMemo() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState !is CreateMemoUiState.Loading && title.isNotBlank() && content.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is CreateMemoUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "저장",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Place Search Dialog
        if (showSearchDialog) {
            PlaceSearchDialog(
                searchQuery = searchQuery,
                searchResults = searchResults,
                isSearching = isSearching,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearch = viewModel::searchPlaces,
                onSelectPlace = viewModel::selectPlace,
                onDismiss = viewModel::closeSearchDialog
            )
        }
    }
}
