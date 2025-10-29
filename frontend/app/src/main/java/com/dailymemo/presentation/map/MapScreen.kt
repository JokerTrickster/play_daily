package com.dailymemo.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
    val currentLocation by viewModel.currentLocation.collectAsState()
    val memos by viewModel.memos.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showPlaceDialog by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<com.dailymemo.domain.models.Place?>(null) }

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

            // Move camera when current location changes
            LaunchedEffect(currentLocation) {
                currentLocation?.let { location ->
                    kakaoMap?.let { map ->
                        try {
                            map.moveCamera(
                                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(
                                    LatLng.from(location.latitude, location.longitude)
                                )
                            )
                            Log.d("MapScreen", "Camera moved to current location: ${location.latitude}, ${location.longitude}")
                        } catch (e: Exception) {
                            Log.e("MapScreen", "Error moving camera to current location: ${e.message}", e)
                        }
                    }
                }
            }

            // Add all markers (current location and memos)
            LaunchedEffect(kakaoMap, memos) {
                kakaoMap?.let { map ->
                    try {
                        val labelManager = map.labelManager
                        val layer = labelManager?.layer

                        // Remove all markers first, then re-add them in correct order
                        layer?.removeAll()

                        // 1. Add current location marker first (use current value from state)
                        currentLocation?.let { location ->
                            val position = LatLng.from(location.latitude, location.longitude)
                            val styles = LabelStyles.from(
                                LabelStyle.from(android.R.drawable.ic_menu_mylocation)
                                    .setTextStyles(32, android.graphics.Color.parseColor("#2196F3"), 2, android.graphics.Color.WHITE)
                            )

                            val options = LabelOptions.from(position)
                                .setStyles(styles)
                                .setTag("current_location")
                                .setTexts("내 위치")

                            layer?.addLabel(options)
                            Log.d("MapScreen", "Added current location marker at ${location.latitude}, ${location.longitude}")
                        }

                        // 2. Add markers for saved memos
                        memos.filter { it.latitude != null && it.longitude != null }
                            .forEach { memo ->
                                val position = LatLng.from(memo.latitude!!, memo.longitude!!)

                                val styles = LabelStyles.from(
                                    LabelStyle.from(android.R.drawable.star_on)
                                        .setTextStyles(30, android.graphics.Color.parseColor("#FFC107"), 2, android.graphics.Color.WHITE)
                                )

                                val labelText = "${memo.category.icon} ${memo.title}"

                                val options = LabelOptions.from(position)
                                    .setStyles(styles)
                                    .setTag("memo_${memo.id}")
                                    .setTexts(labelText)

                                layer?.addLabel(options)

                                Log.d("MapScreen", "Added memo marker: ${memo.title}")
                            }

                        // Set label click listener for memo markers
                        map.setOnLabelClickListener { _, _, label ->
                            val tag = label.tag.toString()
                            if (tag.startsWith("memo_")) {
                                val memoId = tag.removePrefix("memo_").toLongOrNull()
                                if (memoId != null) {
                                    onNavigateToDetail(memoId)
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
                        placeholder = { Text("장소 검색...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "검색")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "지우기")
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
                                    viewModel.searchPlaces(searchQuery)
                                }
                            }
                        )
                    )

                    // Search Button
                    Button(
                        onClick = {
                            if (searchQuery.length >= 2) {
                                viewModel.searchPlaces(searchQuery)
                            }
                        },
                        enabled = searchQuery.length >= 2,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("검색")
                    }
                }

                // Search Results List (below search bar)
                if (searchResults.isNotEmpty()) {
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

            // Floating Action Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.MyLocation,
                        contentDescription = "내 위치"
                    )
                }

                // Create Memo Button
                FloatingActionButton(
                    onClick = onNavigateToCreateMemo,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "메모 추가",
                        modifier = Modifier.size(32.dp)
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
    }
}

@Composable
fun PlaceSelectionDialog(
    place: com.dailymemo.domain.models.Place,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "이 장소로 메모를 작성하시겠습니까?")
        },
        text = {
            Column {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = place.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (place.phone != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "전화: ${place.phone}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("메모 작성")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
