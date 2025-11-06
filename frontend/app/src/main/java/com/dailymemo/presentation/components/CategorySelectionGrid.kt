package com.dailymemo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var showCustomInput by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    val groupedCategories = categories.groupBy { it.sentiment }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 긍정 카테고리
        groupedCategories[CategorySentiment.POSITIVE]?.let { positiveCategories ->
            if (positiveCategories.isNotEmpty()) {
                CategorySection(
                    title = "😊 긍정",
                    titleColor = Color(0xFF10B981),
                    backgroundColor = Color(0xFFD1FAE5),
                    categories = positiveCategories,
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

        // 부정 카테고리
        groupedCategories[CategorySentiment.NEGATIVE]?.let { negativeCategories ->
            if (negativeCategories.isNotEmpty()) {
                CategorySection(
                    title = "😞 부정",
                    titleColor = Color(0xFFEF4444),
                    backgroundColor = Color(0xFFFEE2E2),
                    categories = negativeCategories,
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

        // 중립 카테고리
        groupedCategories[CategorySentiment.NEUTRAL]?.let { neutralCategories ->
            if (neutralCategories.isNotEmpty()) {
                CategorySection(
                    title = "😐 중립",
                    titleColor = Color(0xFF6B7280),
                    backgroundColor = Color(0xFFF3F4F6),
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

        // 직접 작성 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✏️ 직접 작성",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF92400E),
                    fontWeight = FontWeight.Bold
                )
                FilterChip(
                    selected = showCustomInput,
                    onClick = { showCustomInput = !showCustomInput },
                    label = { Text(if (showCustomInput) "닫기" else "추가") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (showCustomInput) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFBBF24),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White
                    )
                )
            }

            if (showCustomInput) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customCategoryText,
                    onValueChange = { customCategoryText = it },
                    placeholder = { Text("나만의 카테고리를 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        if (customCategoryText.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "추가",
                                modifier = Modifier
                                    .clickable {
                                        // TODO: 커스텀 카테고리 추가 로직
                                        // 현재는 UI만 구현
                                        customCategoryText = ""
                                    }
                                    .padding(8.dp),
                                tint = Color(0xFF10B981)
                            )
                        }
                    }
                )
                Text(
                    text = "💡 직접 작성한 카테고리는 이 메모에만 적용됩니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF92400E).copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
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
 * @param backgroundColor 배경 색상
 * @param categories 표시할 카테고리 목록
 * @param selectedIds 선택된 카테고리 ID 집합
 * @param onToggle 카테고리 토글 시 호출되는 콜백
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    title: String,
    titleColor: Color,
    backgroundColor: Color,
    categories: List<MemoCategory>,
    selectedIds: Set<Int>,
    onToggle: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = titleColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                CategoryChip(
                    category = category,
                    isSelected = category.id in selectedIds,
                    onToggle = { onToggle(category.id) },
                    accentColor = titleColor
                )
            }
        }
    }
}

/**
 * 카테고리 칩 컴포넌트
 *
 * 개별 카테고리를 FilterChip 스타일로 표시합니다.
 *
 * @param category 표시할 카테고리
 * @param isSelected 선택 여부
 * @param onToggle 토글 시 호출되는 콜백
 * @param accentColor 강조 색상
 */
@Composable
private fun CategoryChip(
    category: MemoCategory,
    isSelected: Boolean,
    onToggle: () -> Unit,
    accentColor: Color
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "선택됨",
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accentColor,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
            containerColor = Color.White,
            labelColor = accentColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = accentColor.copy(alpha = 0.3f),
            selectedBorderColor = accentColor,
            borderWidth = 1.dp,
            selectedBorderWidth = 2.dp
        )
    )
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
