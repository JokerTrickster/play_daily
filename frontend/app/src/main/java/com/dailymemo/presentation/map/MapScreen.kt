package com.dailymemo.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dailymemo.R
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.presentation.map.components.CreateMemoButton
import com.dailymemo.presentation.map.components.DistanceFilterChips
import com.dailymemo.presentation.map.components.MapSearchBar
import com.dailymemo.presentation.map.components.MemoListBarSimplified
import com.dailymemo.presentation.map.components.MyLocationButton
import com.dailymemo.presentation.map.components.PlaceSelectionDialog
import com.dailymemo.presentation.map.components.ToggleMemoListButton
import com.dailymemo.presentation.map.components.WishlistFilterChips
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import kotlinx.coroutines.launch

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
    val distanceFilter by viewModel.distanceFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMemoId by viewModel.selectedMemoId.collectAsState()
    val showPopupCard by viewModel.showPopupCard.collectAsState()
    val showSearchResults by viewModel.showSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val wishlistFilter by viewModel.wishlistFilter.collectAsState()
    val selectedSearchPlace by viewModel.selectedSearchPlace.collectAsState()
    val canCreateMemo by viewModel.canCreateMemo.collectAsState()
    var showPlaceDialog by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<com.dailymemo.domain.models.Place?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showMemoList by remember { mutableStateOf(true) } // 메모 목록 표시 상태

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

    // 검색 결과가 표시될 때 메모 목록 닫기
    LaunchedEffect(showSearchResults) {
        if (showSearchResults && searchResults.isNotEmpty()) {
            showMemoList = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Map Screen" }
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

            // Search UI and Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Search Bar
                MapSearchBar(
                    searchQuery = searchQuery,
                    isSearching = isSearching,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.searchPlaces(searchQuery)
                    },
                    onClearSearch = { viewModel.clearSearch() }
                )

                // Wishlist Filter Chips
                Spacer(modifier = Modifier.height(8.dp))
                WishlistFilterChips(
                    selectedFilter = wishlistFilter,
                    onFilterSelected = { viewModel.setWishlistFilter(it) }
                )

                // Floating Action Buttons with Distance Filter - Below Filter Chips
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle Memo List Button
                    ToggleMemoListButton(
                        showMemoList = showMemoList,
                        onClick = { showMemoList = !showMemoList }
                    )

                    // Distance filter chips (only visible when memo list is active)
                    if (showMemoList) {
                        DistanceFilterChips(
                            selectedFilter = distanceFilter,
                            onFilterSelected = { viewModel.setDistanceFilter(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Other action buttons below
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // My Location Button
                    MyLocationButton(
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
                        }
                    )

                    // Create Memo Button
                    CreateMemoButton(
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
                        canCreateMemo = canCreateMemo
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

            // Memo List at Bottom (simplified - filters moved to top)
            if (filteredMemos.isNotEmpty() && showMemoList) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    MemoListBarSimplified(
                        memosWithDistance = filteredMemos,
                        selectedMemoId = selectedMemoId,
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
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Filled.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Rating
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${memo.rating}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Detail Button
                            Button(
                                onClick = { onNavigateToDetail(memo.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("상세 보기")
                                Spacer(modifier = Modifier.width(8.dp))
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

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
