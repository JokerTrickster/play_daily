package com.dailymemo.presentation.memo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dailymemo.domain.models.CategorySentiment
import com.dailymemo.domain.models.MemoCategory
import com.dailymemo.presentation.components.CategorySelectionGrid

/**
 * 카테고리 필터 바텀시트
 *
 * 사용자가 카테고리를 선택하여 메모를 필터링할 수 있는 바텀시트입니다.
 * CategorySelectionGrid를 재사용하여 다중 선택을 지원합니다.
 *
 * @param categories 표시할 카테고리 목록
 * @param selectedCategoryIds 현재 선택된 카테고리 ID 집합
 * @param onApplyFilter 필터 적용 시 호출되는 콜백
 * @param onClearFilter 필터 초기화 시 호출되는 콜백
 * @param onDismiss 바텀시트 닫기 시 호출되는 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterBottomSheet(
    categories: List<MemoCategory>,
    selectedCategoryIds: Set<Int>,
    onApplyFilter: (Set<Int>) -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelection by remember(selectedCategoryIds) { mutableStateOf(selectedCategoryIds) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 16.dp) // Extra padding for bottom sheet
        ) {
            Text(
                text = "카테고리 필터",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "하나 이상의 카테고리를 선택하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reuse CategorySelectionGrid for consistency
            CategorySelectionGrid(
                categories = categories,
                selectedCategoryIds = tempSelection,
                onSelectionChange = { tempSelection = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        tempSelection = emptySet()
                        onClearFilter()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("초기화")
                }
                Button(
                    onClick = {
                        onApplyFilter(tempSelection)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = tempSelection.isNotEmpty()
                ) {
                    Text(
                        if (tempSelection.isEmpty()) "카테고리 선택"
                        else "적용 (${tempSelection.size})"
                    )
                }
            }
        }
    }
}

/**
 * CategoryFilterBottomSheet 프리뷰
 */
@Preview(showBackground = true)
@Composable
private fun CategoryFilterBottomSheetPreview() {
    val sampleCategories = listOf(
        MemoCategory(1, "너무 좋았다", CategorySentiment.POSITIVE, "#10B981", 1),
        MemoCategory(2, "다음에 또 방문", CategorySentiment.POSITIVE, "#10B981", 2),
        MemoCategory(3, "가성비 좋다", CategorySentiment.POSITIVE, "#10B981", 3),
        MemoCategory(6, "완전 최악", CategorySentiment.NEGATIVE, "#EF4444", 6),
        MemoCategory(7, "다시는 안 갈 것 같다", CategorySentiment.NEGATIVE, "#EF4444", 7),
        MemoCategory(9, "그냥 무난했다", CategorySentiment.NEUTRAL, "#6B7280", 9)
    )

    MaterialTheme {
        CategoryFilterBottomSheet(
            categories = sampleCategories,
            selectedCategoryIds = setOf(1, 6),
            onApplyFilter = {},
            onClearFilter = {},
            onDismiss = {}
        )
    }
}
