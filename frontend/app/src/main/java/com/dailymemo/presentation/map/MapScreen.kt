package com.dailymemo.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.ui.text.input.ImeAction
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dailymemo.R
import com.dailymemo.domain.models.PlaceCategory
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToCreateMemo: () -> Unit,
    onNavigateToCreateMemoWithPlace: (String, String, Double, Double, String?, String?) -> Unit,
    onNavigateToDetail: (Long) -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val currentLocation by viewModel.currentLocation.collectAsState()
    val memos by viewModel.memos.collectAsState()
    val filteredMemos by viewModel.filteredMemos.collectAsState()
    val sortByDistance by viewModel.sortByDistance.collectAsState()
    val distanceFilter by viewModel.distanceFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMemoId by viewModel.selectedMemoId.collectAsState()
    val showPopupCard by viewModel.showPopupCard.collectAsState()
    val showSearchResults by viewModel.showSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val showJoinRoomDialog by viewModel.showJoinRoomDialog.collectAsState()
    val joinRoomState by viewModel.joinRoomState.collectAsState()
    val wishlistFilter by viewModel.wishlistFilter.collectAsState()
    val selectedSearchPlace by viewModel.selectedSearchPlace.collectAsState()
    val canCreateMemo by viewModel.canCreateMemo.collectAsState()
    var showPlaceDialog by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<com.dailymemo.domain.models.Place?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showMemoList by remember { mutableStateOf(true) } // 메모 목록 표시 상태
    var showFilterOptions by remember { mutableStateOf(false) } // 필터 옵션 표시 상태

    var kakaoMap: KakaoMap? by remember { mutableStateOf(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        Log.d("MapScreen", "Permission result: $hasLocationPermission")
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Reload memos when screen comes back to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshMemos()
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
        if (hasLocationPermission) {
            // Kakao Map View
            var mapView: MapView? by remember { mutableStateOf(null) }

            DisposableEffect(Unit) {
                onDispose {
                    Log.d("MapScreen", "Cleaning up MapView")
                    mapView?.finish()
                    kakaoMap = null
                }
            }

            AndroidView(
                factory = { ctx ->
                    Log.d("MapScreen", "MapView factory called")
                    MapView(ctx).apply {
                        mapView = this
                        start(object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {
                                Log.d("MapScreen", "onMapDestroy")
                            }

                            override fun onMapError(error: Exception) {
                                Log.e("MapScreen", "onMapError: ${error.message}", error)
                            }
                        }, object : KakaoMapReadyCallback() {
                            override fun onMapReady(map: KakaoMap) {
                                Log.d("MapScreen", "onMapReady - Map is ready!")
                                kakaoMap = map
                                try {
                                    // 현재 위치로 바로 이동
                                    currentLocation?.let { loc ->
                                        map.moveCamera(
                                            com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(
                                                LatLng.from(loc.latitude, loc.longitude)
                                            )
                                        )
                                        Log.d("MapScreen", "Camera moved to current location: ${loc.latitude}, ${loc.longitude}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("MapScreen", "Error moving camera: ${e.message}", e)
                                }
                            }
                        })
                    }
                },
                update = { view ->
                    Log.d("MapScreen", "MapView update called")
                },
                modifier = Modifier.fillMaxSize()
            )

            // Move camera to current location only once (on initial load)
            LaunchedEffect(Unit) {
                currentLocation?.let { location ->
                    kakaoMap?.let { map ->
                        try {
                            map.moveCamera(
                                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(
                                    LatLng.from(location.latitude, location.longitude)
                                )
                            )
                            Log.d("MapScreen", "Camera moved to initial location: ${location.latitude}, ${location.longitude}")
                        } catch (e: Exception) {
                            Log.e("MapScreen", "Error moving camera to initial location: ${e.message}", e)
                        }
                    }
                }
            }

            // Add all markers (current location, selected search place, and memos)
            LaunchedEffect(kakaoMap, memos, selectedSearchPlace) {
                kakaoMap?.let { map ->
                    try {
                        val labelManager = map.labelManager
                        val layer = labelManager?.layer

                        // Remove all markers first, then re-add them in correct order
                        layer?.removeAll()

                        // 1. Add current location marker first (red circle)
                        currentLocation?.let { location ->
                            val position = LatLng.from(location.latitude, location.longitude)

                            // Create red circle marker bitmap
                            val redCircleBitmap = MarkerBitmapHelper.createCircleMarker(
                                size = 40,
                                color = android.graphics.Color.parseColor("#F44336"), // Red
                                strokeColor = android.graphics.Color.WHITE,
                                strokeWidth = 4f
                            )

                            val styles = LabelStyles.from(LabelStyle.from(redCircleBitmap))

                            val options = LabelOptions.from(position)
                                .setStyles(styles)
                                .setTag("current_location")

                            layer?.addLabel(options)
                            Log.d("MapScreen", "Added current location marker (red circle 40px) at ${location.latitude}, ${location.longitude}")
                        }

                        // 2. Add markers for saved memos with speech bubble style
                        memos.filter { it.latitude != null && it.longitude != null }
                            .forEach { memo ->
                                val position = LatLng.from(memo.latitude!!, memo.longitude!!)

                                // Category별 색상 가져오기
                                val markerColor = MarkerBitmapHelper.getCategoryColor(memo.category.name)

                                // 말풍선 마커 비트맵 생성
                                val markerBitmap = MarkerBitmapHelper.createSpeechBubbleMarker(
                                    context = context,
                                    title = memo.title,
                                    rating = memo.rating,
                                    color = markerColor
                                )

                                // 커스텀 비트맵으로 마커 스타일 생성
                                val styles = LabelStyles.from(LabelStyle.from(markerBitmap))

                                val options = LabelOptions.from(position)
                                    .setStyles(styles)
                                    .setTag("memo_${memo.id}")

                                layer?.addLabel(options)

                                Log.d("MapScreen", "Added speech bubble marker: ${memo.title} with color ${memo.category.name}")
                            }

                        // 3. Add selected search place marker (if any)
                        selectedSearchPlace?.let { place ->
                            val position = LatLng.from(place.latitude, place.longitude)

                            // Create green pin marker for searched place (40px)
                            val pinBitmap = MarkerBitmapHelper.createCircleMarker(
                                size = 40,
                                color = android.graphics.Color.parseColor("#4CAF50"), // Green
                                strokeColor = android.graphics.Color.WHITE,
                                strokeWidth = 4f
                            )

                            val styles = LabelStyles.from(LabelStyle.from(pinBitmap))

                            val options = LabelOptions.from(position)
                                .setStyles(styles)
                                .setTag("search_place")

                            layer?.addLabel(options)
                            Log.d("MapScreen", "Added search place marker (green 40px): ${place.name} at ${place.latitude}, ${place.longitude}")
                        }

                        // Set label click listener for memo markers - show popup card
                        map.setOnLabelClickListener { _, _, label ->
                            val tag = label.tag.toString()
                            if (tag.startsWith("memo_")) {
                                val memoId = tag.removePrefix("memo_").toLongOrNull()
                                if (memoId != null) {
                                    viewModel.showMemoPopup(memoId)
                                }
                            }
                        }

                        Log.d("MapScreen", "Added ${memos.filter { it.latitude != null && it.longitude != null }.size} memo markers")
                    } catch (e: Exception) {
                        Log.e("MapScreen", "Error adding markers: ${e.message}", e)
                    }
                }
            }

            // Search UI - No category filters, just search bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Search Bar with Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newQuery ->
                            viewModel.updateSearchQuery(newQuery)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(28.dp)),
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear))
                                }
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.length >= 2) {
                                    focusManager.clearFocus()
                                    viewModel.searchPlaces(searchQuery)
                                }
                            }
                        )
                    )

                    // Search Button
                    Button(
                        onClick = {
                            if (searchQuery.length >= 2) {
                                focusManager.clearFocus()
                                viewModel.searchPlaces(searchQuery)
                            }
                        },
                        enabled = searchQuery.length >= 2 && !isSearching,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.search))
                        }
                    }
                }

                // Wishlist Filter Chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = wishlistFilter == com.dailymemo.presentation.map.WishlistFilter.ALL,
                        onClick = { viewModel.setWishlistFilter(com.dailymemo.presentation.map.WishlistFilter.ALL) },
                        label = { Text(stringResource(R.string.wishlist_filter_all)) }
                    )
                    FilterChip(
                        selected = wishlistFilter == com.dailymemo.presentation.map.WishlistFilter.WISHLIST_ONLY,
                        onClick = { viewModel.setWishlistFilter(com.dailymemo.presentation.map.WishlistFilter.WISHLIST_ONLY) },
                        label = { Text(stringResource(R.string.wishlist_filter_wishlist)) }
                    )
                    FilterChip(
                        selected = wishlistFilter == com.dailymemo.presentation.map.WishlistFilter.VISITED_ONLY,
                        onClick = { viewModel.setWishlistFilter(com.dailymemo.presentation.map.WishlistFilter.VISITED_ONLY) },
                        label = { Text(stringResource(R.string.wishlist_filter_visited)) }
                    )
                }

                // Search Results List (below search bar) - only show if showSearchResults is true
                if (searchResults.isNotEmpty() && showSearchResults) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            searchResults.take(5).forEach { place ->
                                Surface(
                                    onClick = {
                                        // 검색 결과 목록 닫기
                                        viewModel.hideSearchResults()

                                        // 선택한 장소를 ViewModel에 저장 (마커 표시용)
                                        viewModel.selectSearchPlace(place)

                                        // 지도를 해당 장소로 이동
                                        kakaoMap?.moveCamera(
                                            com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(
                                                LatLng.from(place.latitude, place.longitude),
                                                15 // zoom level
                                            )
                                        )
                                        Log.d("MapScreen", "Moving to place: ${place.name} at ${place.latitude}, ${place.longitude}")

                                        // 다이얼로그 표시
                                        selectedPlace = place
                                        showPlaceDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = place.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = place.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                if (place != searchResults.take(5).last()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }

            // Memo List at Bottom with Filter Controls
            if (filteredMemos.isNotEmpty() && showMemoList) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 88.dp) // FAB 공간 확보
                ) {
                    // Filter Controls (토글 가능)
                    if (showFilterOptions) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Sort toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "가까운 순 정렬",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Switch(
                                        checked = sortByDistance,
                                        onCheckedChange = { viewModel.toggleSortByDistance() }
                                    )
                                }

                                // Distance filter
                                Text(
                                    "거리 필터",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DistanceFilterChip(
                                        label = "전체",
                                        selected = distanceFilter == DistanceFilter.ALL,
                                        onClick = { viewModel.setDistanceFilter(DistanceFilter.ALL) }
                                    )
                                    DistanceFilterChip(
                                        label = "5km",
                                        selected = distanceFilter == DistanceFilter.WITHIN_5KM,
                                        onClick = { viewModel.setDistanceFilter(DistanceFilter.WITHIN_5KM) }
                                    )
                                    DistanceFilterChip(
                                        label = "10km",
                                        selected = distanceFilter == DistanceFilter.WITHIN_10KM,
                                        onClick = { viewModel.setDistanceFilter(DistanceFilter.WITHIN_10KM) }
                                    )
                                    DistanceFilterChip(
                                        label = "20km",
                                        selected = distanceFilter == DistanceFilter.WITHIN_20KM,
                                        onClick = { viewModel.setDistanceFilter(DistanceFilter.WITHIN_20KM) }
                                    )
                                }
                            }
                        }
                    }

                    // Memo List
                    MemoListBarWithDistance(
                        memosWithDistance = filteredMemos,
                        selectedMemoId = selectedMemoId,
                        showFilterOptions = showFilterOptions,
                        onToggleFilterOptions = { showFilterOptions = !showFilterOptions },
                        formatDistance = viewModel::formatDistance,
                        onMemoClick = { memo ->
                            // 선택된 메모 표시
                            viewModel.showMemoPopup(memo.id)

                            // 해당 위치로 카메라 이동
                            memo.latitude?.let { lat ->
                                memo.longitude?.let { lon ->
                                    kakaoMap?.moveCamera(
                                        com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(
                                            LatLng.from(lat, lon),
                                            15 // zoom level
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Floating Action Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toggle Memo List Button
                FloatingActionButton(
                    onClick = {
                        showMemoList = !showMemoList
                    },
                    containerColor = if (showMemoList) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (showMemoList) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.List,
                        contentDescription = "메모 목록",
                        modifier = Modifier.size(22.dp)
                    )
                }

                // My Location Button
                FloatingActionButton(
                    onClick = {
                        currentLocation?.let { location ->
                            kakaoMap?.moveCamera(
                                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(
                                    LatLng.from(location.latitude, location.longitude),
                                    15 // zoom level
                                )
                            )
                            Log.d("MapScreen", "Move to current location button clicked")
                        } ?: run {
                            // If no location yet, request it
                            viewModel.getCurrentLocation()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.MyLocation,
                        contentDescription = "내 위치",
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Create Memo Button
                FloatingActionButton(
                    onClick = {
                        if (canCreateMemo) {
                            onNavigateToCreateMemo()
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "읽기 전용 권한으로 메모를 작성할 수 없습니다",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    containerColor = if (canCreateMemo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "메모 추가",
                        tint = if (canCreateMemo) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        } else {
            // Permission Not Granted
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
                    ),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📍",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "위치 권한이 필요합니다",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "지도를 사용하려면 위치 권한을 허용해주세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("권한 허용")
                        }
                    }
                }
            }
        }

        // Place Selection Dialog
        if (showPlaceDialog && selectedPlace != null) {
            PlaceSelectionDialog(
                place = selectedPlace!!,
                onDismiss = {
                    showPlaceDialog = false
                    selectedPlace = null
                },
                onConfirm = {
                    selectedPlace?.let { place ->
                        onNavigateToCreateMemoWithPlace(
                            place.name,
                            place.address,
                            place.latitude,
                            place.longitude,
                            place.toPlaceCategory().name,
                            place.placeUrl
                        )
                    }
                    showPlaceDialog = false
                    selectedPlace = null
                    viewModel.clearSearch()
                }
            )
        }

        // Memo Popup Card (마커 클릭 시 표시)
        if (showPopupCard && selectedMemoId != null) {
            val selectedMemo = memos.find { it.id == selectedMemoId }
            selectedMemo?.let { memo ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp), // FAB 위에 표시
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header with close button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${memo.category.icon} ${memo.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.dismissPopupCard() }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "닫기",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Content preview
                            if (memo.content.isNotEmpty()) {
                                Text(
                                    text = memo.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Location and rating
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Location
                                memo.locationName?.let { location ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Place,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                    }
                                }

                                // Rating
                                if (memo.rating > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = "평점",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFFFFB800)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = String.format("%.1f", memo.rating),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFB800)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // View details button
                            Button(
                                onClick = {
                                    viewModel.dismissPopupCard()
                                    onNavigateToDetail(memo.id)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("자세히 보기")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // JoinRoom Dialog
    if (showJoinRoomDialog) {
        com.dailymemo.presentation.map.components.JoinRoomDialog(
            joinRoomState = joinRoomState,
            onJoinRoom = { roomId, password ->
                viewModel.joinRoom(roomId, password)
            },
            onDismiss = {
                viewModel.dismissJoinRoomDialog()
            }
        )
    }
}

@Composable
fun PlaceSelectionDialog(
    place: com.dailymemo.domain.models.Place,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // 하단 시트 스타일로 변경 - 지도가 위에 보이도록
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // 반투명 배경 (지도가 잘 보이도록 살짝만 투명)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
                .clickable(onClick = onDismiss)
        )

        // 하단 카드
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // 제목
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "이 장소로 메모를 작성하시겠습니까?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 장소 정보
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = place.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (place.phone != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📞 ${place.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("메모 작성")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoListBar(
    memos: List<com.dailymemo.domain.models.Memo>,
    selectedMemoId: Long?,
    onMemoClick: (com.dailymemo.domain.models.Memo) -> Unit
) {
    val listState = rememberLazyListState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 메모 목록",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "${memos.size}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Horizontal scrollable list
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(memos) { memo ->
                    MemoCardCompact(
                        memo = memo,
                        isSelected = memo.id == selectedMemoId,
                        onClick = { onMemoClick(memo) }
                    )
                }
            }
        }
    }
}

@Composable
fun MemoCardCompact(
    memo: com.dailymemo.domain.models.Memo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 0.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Category icon + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = memo.category.icon,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = memo.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Location (if exists)
                memo.locationName?.let { location ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Filled.Place,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                // Rating (if > 0)
                if (memo.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "평점",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFB800)
                        )
                        Text(
                            text = String.format("%.1f", memo.rating),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB800)
                        )
                    }
                }
            }

            // Right side: Thumbnail image (if exists)
            if (!memo.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = memo.imageUrl,
                    contentDescription = "메모 이미지",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun DistanceFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun MemoListBarWithDistance(
    memosWithDistance: List<MemoWithDistance>,
    selectedMemoId: Long?,
    showFilterOptions: Boolean,
    onToggleFilterOptions: () -> Unit,
    formatDistance: (Double?) -> String,
    onMemoClick: (com.dailymemo.domain.models.Memo) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column {
            // Header with filter toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "메모 목록 (${memosWithDistance.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onToggleFilterOptions) {
                    Icon(
                        if (showFilterOptions) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (showFilterOptions) "필터 숨기기" else "필터 보기"
                    )
                }
            }

            // Horizontal scrollable list
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = memosWithDistance,
                    key = { it.memo.id }
                ) { memoWithDistance ->
                    MemoCardWithDistance(
                        memo = memoWithDistance.memo,
                        distance = memoWithDistance.distance,
                        formatDistance = formatDistance,
                        isSelected = memoWithDistance.memo.id == selectedMemoId,
                        onClick = { onMemoClick(memoWithDistance.memo) }
                    )
                }
            }
        }
    }
}

@Composable
fun MemoCardWithDistance(
    memo: com.dailymemo.domain.models.Memo,
    distance: Double?,
    formatDistance: (Double?) -> String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Left side: Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Title
                    Text(
                        text = memo.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Distance
                    if (distance != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDistance(distance),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Rating
                if (memo.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = memo.rating.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Right side: Thumbnail image (if exists)
            if (!memo.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = memo.imageUrl,
                    contentDescription = "메모 이미지",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
