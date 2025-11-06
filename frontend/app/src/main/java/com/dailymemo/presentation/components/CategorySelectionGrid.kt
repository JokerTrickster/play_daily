package com.dailymemo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dailymemo.domain.models.CategorySentiment
import com.dailymemo.domain.models.MemoCategory

/**
 * 카테고리 선택 그리드 컴포넌트
 *
 * 감정별로 그룹화된 카테고리를 표시하고 다중 선택을 지원합니다.
 *
 * @param categories 표시할 카테고리 목록
 * @param selectedCategoryIds 선택된 카테고리 ID 집합
 * @param onSelectionChange 선택 변경 시 호출되는 콜백
 * @param modifier Modifier
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelectionGrid(
    categories: List<MemoCategory>,
    selectedCategoryIds: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedCategories = categories.groupBy { it.sentiment }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 긍정 카테고리
        groupedCategories[CategorySentiment.POSITIVE]?.let { positiveCategories ->
            if (positiveCategories.isNotEmpty()) {
                CategorySection(
                    title = "긍정",
                    titleColor = Color(0xFF10B981),
                    categories = positiveCategories,
                    selectedIds = selectedCategoryIds,
                    onToggle = { id ->
                        val newSelection = selectedCategoryIds.toMutableSet()
                        if (id in newSelection) newSelection.remove(id)
                        else newSelection.add(id)
                        onSelectionChange(newSelection)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 부정 카테고리
        groupedCategories[CategorySentiment.NEGATIVE]?.let { negativeCategories ->
            if (negativeCategories.isNotEmpty()) {
                CategorySection(
                    title = "부정",
                    titleColor = Color(0xFFEF4444),
                    categories = negativeCategories,
                    selectedIds = selectedCategoryIds,
                    onToggle = { id ->
                        val newSelection = selectedCategoryIds.toMutableSet()
                        if (id in newSelection) newSelection.remove(id)
                        else newSelection.add(id)
                        onSelectionChange(newSelection)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 중립 카테고리
        groupedCategories[CategorySentiment.NEUTRAL]?.let { neutralCategories ->
            if (neutralCategories.isNotEmpty()) {
                CategorySection(
                    title = "중립",
                    titleColor = Color(0xFF6B7280),
                    categories = neutralCategories,
                    selectedIds = selectedCategoryIds,
                    onToggle = { id ->
                        val newSelection = selectedCategoryIds.toMutableSet()
                        if (id in newSelection) newSelection.remove(id)
                        else newSelection.add(id)
                        onSelectionChange(newSelection)
                    }
                )
            }
        }
    }
}

/**
 * 카테고리 섹션 컴포넌트
 *
 * 감정별로 그룹화된 카테고리를 헤더와 함께 표시합니다.
 *
 * @param title 섹션 제목
 * @param titleColor 제목 색상
 * @param categories 표시할 카테고리 목록
 * @param selectedIds 선택된 카테고리 ID 집합
 * @param onToggle 카테고리 토글 시 호출되는 콜백
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    title: String,
    titleColor: Color,
    categories: List<MemoCategory>,
    selectedIds: Set<Int>,
    onToggle: (Int) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                CategoryCheckbox(
                    category = category,
                    isSelected = category.id in selectedIds,
                    onToggle = { onToggle(category.id) }
                )
            }
        }
    }
}

/**
 * 카테고리 체크박스 컴포넌트
 *
 * 개별 카테고리를 체크박스와 함께 표시합니다.
 * 44x44dp 최소 터치 영역을 준수합니다.
 *
 * @param category 표시할 카테고리
 * @param isSelected 선택 여부
 * @param onToggle 토글 시 호출되는 콜백
 */
@Composable
private fun CategoryCheckbox(
    category: MemoCategory,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFFE0F2FE)
                else Color(0xFFF3F4F6)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .heightIn(min = 44.dp), // 접근성: 최소 44dp 터치 영역
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * CategorySelectionGrid 프리뷰
 */
@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, name = "Category Selection Grid")
@Composable
private fun CategorySelectionGridPreview() {
    val sampleCategories = listOf(
        MemoCategory(1, "너무 좋았다", CategorySentiment.POSITIVE, "#10B981", 1),
        MemoCategory(2, "다음에 또 방문할 의향이 있다", CategorySentiment.POSITIVE, "#10B981", 2),
        MemoCategory(3, "가성비 좋다", CategorySentiment.POSITIVE, "#10B981", 3),
        MemoCategory(4, "분위기가 좋다", CategorySentiment.POSITIVE, "#10B981", 4),
        MemoCategory(5, "음식/서비스가 훌륭하다", CategorySentiment.POSITIVE, "#10B981", 5),
        MemoCategory(6, "완전 최악, 가성비 최악", CategorySentiment.NEGATIVE, "#EF4444", 6),
        MemoCategory(7, "다시는 안 갈 것 같다", CategorySentiment.NEGATIVE, "#EF4444", 7),
        MemoCategory(8, "기대 이하였다", CategorySentiment.NEGATIVE, "#EF4444", 8),
        MemoCategory(9, "그냥 무난했다", CategorySentiment.NEUTRAL, "#6B7280", 9),
        MemoCategory(10, "특별한 점이 없었다", CategorySentiment.NEUTRAL, "#6B7280", 10)
    )

    MaterialTheme {
        CategorySelectionGrid(
            categories = sampleCategories,
            selectedCategoryIds = setOf(1, 6, 9),
            onSelectionChange = {}
        )
    }
}

/**
 * 선택 없음 프리뷰
 */
@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, name = "No Selection")
@Composable
private fun CategorySelectionGridNoSelectionPreview() {
    val sampleCategories = listOf(
        MemoCategory(1, "너무 좋았다", CategorySentiment.POSITIVE, "#10B981", 1),
        MemoCategory(2, "다음에 또 방문할 의향이 있다", CategorySentiment.POSITIVE, "#10B981", 2),
        MemoCategory(6, "완전 최악", CategorySentiment.NEGATIVE, "#EF4444", 6),
        MemoCategory(9, "그냥 무난했다", CategorySentiment.NEUTRAL, "#6B7280", 9)
    )

    MaterialTheme {
        CategorySelectionGrid(
            categories = sampleCategories,
            selectedCategoryIds = emptySet(),
            onSelectionChange = {}
        )
    }
}
