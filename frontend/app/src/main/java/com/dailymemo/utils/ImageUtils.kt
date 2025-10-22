package com.dailymemo.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size

/**
 * 최적화된 썸네일 이미지 로더
 *
 * - 메모리 효율적인 다운샘플링
 * - 디스크/메모리 캐싱 자동 활성화
 * - Placeholder/Error 이미지 지원
 *
 * @param imageUrl 이미지 URL
 * @param contentDescription 접근성 설명
 * @param modifier Modifier
 * @param contentScale 이미지 스케일
 * @param thumbnailSize 썸네일 크기 (기본: 400px)
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    thumbnailSize: Int = 400
) {
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            // 이미지 크기 제한 (메모리 절약)
            .size(Size(thumbnailSize, thumbnailSize))
            .scale(Scale.FILL)
            // 캐싱 활성화
            .memoryCacheKey(imageUrl)
            .diskCacheKey(imageUrl)
            // 크로스페이드 애니메이션
            .crossfade(true)
            .crossfade(300)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * 전체 크기 이미지 로더 (상세 화면용)
 *
 * @param imageUrl 이미지 URL
 * @param contentDescription 접근성 설명
 * @param modifier Modifier
 * @param contentScale 이미지 스케일
 */
@Composable
fun FullSizeAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            // 원본 크기 사용
            .size(Size.ORIGINAL)
            // 캐싱 활성화
            .memoryCacheKey(imageUrl)
            .diskCacheKey(imageUrl)
            // 크로스페이드 애니메이션
            .crossfade(true)
            .crossfade(300)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * 이미지 프리로딩 (백그라운드에서 미리 로드)
 *
 * LaunchedEffect에서 사용하여 화면 진입 전에 이미지를 미리 로드합니다.
 *
 * @param context Context
 * @param imageUrls 프리로드할 이미지 URL 리스트
 */
suspend fun preloadImages(
    context: android.content.Context,
    imageUrls: List<String>
) {
    val imageLoader = coil.ImageLoader(context)

    imageUrls.forEach { url ->
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(Size(400, 400)) // 썸네일 크기로 프리로드
            .build()

        // 백그라운드에서 로드 (화면에 표시하지 않고 캐시만)
        imageLoader.execute(request)
    }
}
